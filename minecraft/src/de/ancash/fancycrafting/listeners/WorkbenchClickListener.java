package de.ancash.fancycrafting.listeners;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import com.earth2me.essentials.Essentials;

import de.ancash.ILibrary;
import de.ancash.datastructures.tuples.Triplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.handler.RecipeResultSupplier;
import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.fancycrafting.recipe.complex.IComplexRecipe;
import de.ancash.fancycrafting.recipe.crafting.RecipeMatcherCallable;
import de.ancash.minecraft.cryptomorin.xseries.XMaterial;
import de.ancash.nbtnexus.InventoryUtils;
import de.ancash.nbtnexus.serde.SerializedItem;
import de.ancash.nbtnexus.serde.access.SerializedMetaAccess;

public class WorkbenchClickListener implements Listener {

	private final FancyCrafting pl;
	private final Map<UUID, Triplet<RecipeMatcherCallable, Integer, IRecipe>> dataByUUID = new ConcurrentHashMap<>();
	private final RecipeResultSupplier resultSupplier = new RecipeResultSupplier();
	private final boolean useCustom;
	private final boolean support3x3;
	private final boolean support2x2;
	@SuppressWarnings("nls")
	private final boolean checkForEssentials = Bukkit.getPluginManager().getPlugin("Essentials") != null;

	public WorkbenchClickListener(FancyCrafting pl, boolean useCustom, boolean support3x3, boolean support2x2) {
		this.pl = pl;
		this.useCustom = useCustom;
		this.support2x2 = support2x2;
		this.support3x3 = support3x3;
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		dataByUUID.remove(e.getPlayer().getUniqueId());
	}

	@EventHandler(ignoreCancelled = true)
	public void onClick(InventoryClickEvent event) {
		InventoryType type = event.getInventory().getType();
		if (useCustom && type == InventoryType.WORKBENCH)
			return;

		if (type != InventoryType.CRAFTING && type != InventoryType.WORKBENCH)
			return;

		if (type == InventoryType.CRAFTING && !support2x2)
			return;

		if (type == InventoryType.WORKBENCH && !support3x3)
			return;

		if (checkForEssentials
				&& Essentials.getPlugin(Essentials.class).getUser((Player) event.getView().getPlayer()).isRecipeSee())
			return;

		if (!event.getInventory().equals(event.getClickedInventory()) || event.getSlot() != 0)
			return;

		event.setCancelled(true);
		Triplet<RecipeMatcherCallable, Integer, IRecipe> triplet = dataByUUID.get(event.getWhoClicked().getUniqueId());
		if (triplet == null || triplet.getThird() == null)
			matchRecipe((CraftingInventory) event.getInventory(), (Player) event.getWhoClicked());
		else
			craftItem(event, triplet);
	}

	@EventHandler
	public void onRecipePrepare(PrepareItemCraftEvent event) throws Exception {
		InventoryType type = event.getInventory().getType();
		if (useCustom && type == InventoryType.WORKBENCH)
			return;

		if (type == InventoryType.CRAFTING && !support2x2)
			return;

		if (type == InventoryType.WORKBENCH && !support3x3)
			return;

		if (checkForEssentials
				&& Essentials.getPlugin(Essentials.class).getUser((Player) event.getView().getPlayer()).isRecipeSee())
			return;

		UUID id = event.getView().getPlayer().getUniqueId();
		dataByUUID.computeIfAbsent(id,
				p -> Tuple.of(pl.newDefaultRecipeMatcher((Player) event.getView().getPlayer()), 0, null));
		Triplet<RecipeMatcherCallable, Integer, IRecipe> triplet = dataByUUID.get(id);

		if (ILibrary.getTick() <= triplet.getSecond() + 1)
			return;

		event.getInventory().setResult(null);
		triplet.setSecond(ILibrary.getTick());
		Bukkit.getScheduler().runTaskLater(pl,
				() -> matchRecipe(event.getInventory(), (Player) event.getView().getPlayer()), 1);
	}

	private void matchRecipe(CraftingInventory inv, Player player) {
		Triplet<RecipeMatcherCallable, Integer, IRecipe> triplet = dataByUUID.computeIfAbsent(player.getUniqueId(),
				k -> Triplet.of(pl.newDefaultRecipeMatcher(player), ILibrary.getTick(), null));
		int size = (int) Math.sqrt(inv.getType().getDefaultSize() - 1);
		IMatrix<SerializedItem> matrix = new IMatrix<>(Stream.of(inv.getMatrix())
				.map(i -> i != null && i.getType() != Material.AIR ? SerializedItem.of(i) : null)
				.toArray(SerializedItem[]::new), size, size);
		matrix.optimize();
		triplet.getFirst().setMatrix(matrix);
		IRecipe match;
		match = triplet.getFirst().call();
		triplet.setThird(match);
		if (match != null)
			inv.setResult(match.getResult());
		else
			inv.setResult(null);
		player.updateInventory();
	}

	private void craftItem(InventoryClickEvent event, Triplet<RecipeMatcherCallable, Integer, IRecipe> triplet) {
		if (triplet.getThird() == null || ILibrary.getTick() - pl.getCraftingCooldown() <= triplet.getSecond()) {
			event.getWhoClicked().sendMessage(pl.getResponse().CRAFTING_COOLDOWN_MESSAGE);
			return;
		}

		int size = (int) Math.sqrt(event.getInventory().getType().getDefaultSize() - 1);
		IRecipe recipe = triplet.getThird();
		if (event.getAction().equals(InventoryAction.PICKUP_ALL)) {
			craftToCursor(size, event, triplet);
		} else if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
			if (recipe.isShiftCollectable()) {
				InventoryUtils.addItemStack((Player) event.getWhoClicked(), recipe.getResult(),
						shiftCollectIngredients(size, (CraftingInventory) event.getInventory(),
								(Player) event.getWhoClicked(), triplet.getFirst().getMatrix(), recipe)
								* recipe.getResult().getAmount());
			} else {
				ItemStack result = resultSupplier.getSingleRecipeCraft(recipe, (Player) event.getWhoClicked());
				if (InventoryUtils.getFreeSpaceExact((Player) event.getWhoClicked(), result) >= result.getAmount()) {
					collectIngredients(size, (CraftingInventory) event.getInventory(), triplet.getFirst().getMatrix(),
							recipe);
					event.getWhoClicked().getInventory().addItem(result);
				}
			}
		}
		matchRecipe((CraftingInventory) event.getInventory(), (Player) event.getWhoClicked());
	}

	private void craftToCursor(int size, InventoryClickEvent event,
			Triplet<RecipeMatcherCallable, Integer, IRecipe> triplet) {
		ItemStack cursor = event.getCursor();
		if (cursor != null && cursor.getType() == Material.AIR) {
			collectIngredients(size, (CraftingInventory) event.getInventory(), triplet.getFirst().getMatrix(),
					triplet.getThird());
			event.getWhoClicked().setItemOnCursor(
					resultSupplier.getSingleRecipeCraft(triplet.getThird(), (Player) event.getWhoClicked()));
			return;
		} else {
			if (triplet.getThird().getResultAsSerializedItem().areEqualIgnoreAmount(SerializedItem.of(cursor))
					&& cursor.getMaxStackSize() >= cursor.getAmount() + triplet.getThird().getResult().getAmount()) {
				if (cursor.getMaxStackSize() >= cursor.getAmount() + triplet.getThird().getResult().getAmount()) {
					collectIngredients(size, (CraftingInventory) event.getInventory(), triplet.getFirst().getMatrix(),
							triplet.getThird());
					cursor.setAmount(cursor.getAmount() + triplet.getThird().getResult().getAmount());
				}
			}
		}
	}

	private int shiftCollectIngredients(int size, CraftingInventory inv, Player player, IMatrix<SerializedItem> matrix,
			IRecipe recipe) {
		int space = InventoryUtils.getFreeSpaceExact(player, recipe.getResult());
		if (space <= 0) {
			return 0;
		}
		int shiftSize = space / recipe.getResult().getAmount();
		if (recipe instanceof IShapedRecipe) {
			for (int i = 0; i < matrix.getArray().length; ++i) {
				if (matrix.getArray()[i] == null)
					continue;
				if (recipe.isVanilla()) {
					shiftSize = Math.min(shiftSize,
							SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(matrix.getArray()[i].getMap()));
				} else {
					SerializedItem iIngredient = ((IShapedRecipe) recipe).getIngredientsArray()[i];
					SerializedItem iCompare = matrix.getArray()[i];
					if (!iIngredient.areEqualIgnoreAmount(iCompare))
						continue;
					shiftSize = Math.min(shiftSize,
							(int) (SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(iCompare.getMap())
									/ SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(iIngredient.getMap())));
				}
			}
		}
		if (recipe instanceof IShapelessRecipe) {
			for (ItemStack ingredient : ((IShapelessRecipe) recipe).getIngredients()) {
				if (ingredient == null)
					continue;
				for (SerializedItem currentItem : matrix.getArray()) {
					if (currentItem == null)
						continue;
					if (recipe.isVanilla())
						shiftSize = Math.min(shiftSize,
								SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(currentItem.getMap()));
					else if (currentItem.areEqualIgnoreAmount(SerializedItem.of(ingredient)))
						shiftSize = Math.min(shiftSize,
								(int) (SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(currentItem.getMap())
										/ ingredient.getAmount()));
				}
			}
		}
		if (recipe instanceof IShapedRecipe) {
			collectShaped(size, inv, matrix, (IShapedRecipe) recipe, shiftSize);
		} else if (recipe instanceof IShapelessRecipe) {
			collectShapeless(size, inv, (IShapelessRecipe) recipe, shiftSize);
		}
		return shiftSize;
	}

	private void collectShapeless(int size, CraftingInventory inv, IShapelessRecipe shapeless, int multiplicator) {
		Set<Integer> done = new HashSet<>();
		Set<XMaterial> ignoredMaterials = new HashSet<>();

		if (shapeless instanceof IComplexRecipe)
			ignoredMaterials = ((IComplexRecipe) shapeless).getIgnoredMaterials();

		for (SerializedItem ingredient : shapeless.getSerializedIngredients()) {
			for (int craftSlot = 0; craftSlot < size * size; craftSlot++) {
				if (done.contains(craftSlot))
					continue;
				if (inv.getItem(craftSlot + 1) == null)
					continue;
				if (ingredient.hashCode() != SerializedItem.of(inv.getItem(craftSlot + 1)).hashCode()
						&& !shapeless.isVanilla())
					continue;

				if (ignoredMaterials.contains(XMaterial.matchXMaterial(inv.getItem(craftSlot + 1))))
					continue;

				int amt = SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(ingredient.getMap()) * multiplicator;
				if (inv.getItem(craftSlot + 1).getAmount() <= amt)
					inv.setItem(craftSlot + 1, null);
				else
					inv.getItem(craftSlot + 1).setAmount(inv.getItem(craftSlot + 1).getAmount() - amt);
				done.add(craftSlot);
				break;
			}
		}
	}

	private void collectShaped(int size, CraftingInventory inv, IMatrix<SerializedItem> matrix, IShapedRecipe shaped,
			int multiplicator) {
		int base = 1 + size * matrix.getUpMoves() + matrix.getLeftMoves();
		boolean isMirrored = isMirrored(matrix, shaped);
		for (int y = 0; y < shaped.getHeight(); y++) {
			for (int x = 0; x < shaped.getWidth(); x++) {
				int i = y * shaped.getWidth() + x;
				int j = mirror(shaped.getWidth(), y, x, isMirrored);
				SerializedItem fromInv = matrix.getArray()[j];
				SerializedItem fromRec = shaped.getIngredientsArray()[i];
				if (fromInv == null || fromRec == null)
					continue;
				int amount = SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(fromInv.getMap())
						- SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(fromRec.getMap()) * multiplicator;
				int slot;
				if (isMirrored)
					slot = base + y * size + (matrix.getWidth() - x - 1);
				else
					slot = base + y * size + x;
				if (amount > 0)
					inv.getItem(slot).setAmount(amount);
				else
					inv.setItem(slot, null);
			}
		}
	}

	private int mirror(int width, int y, int x, boolean really) {
		return really ? y * width + (width - x - 1) : y * width + x;
	}

	/**
	 * Only checks whether the items are null or not Since all ingredients in a
	 * vanilla recipe amount to one a proper check is not required
	 * 
	 * @param recipe
	 * @return
	 */
	private boolean isMirrored(IMatrix<SerializedItem> matrix, IShapedRecipe recipe) {
		if (!recipe.isVanilla())
			return false;
		for (int y = 0; y < recipe.getHeight(); y++)
			for (int x = 0; x < recipe.getWidth(); x++)
				if ((recipe.getIngredientsArray()[y * recipe.getWidth() + x] != null) != (matrix
						.getArray()[y * recipe.getWidth() + (recipe.getWidth() - x - 1)] != null))
					return false;
		return true;
	}

	private void collectIngredients(int size, CraftingInventory inventory, IMatrix<SerializedItem> matrix,
			IRecipe recipe) {
		if (recipe instanceof IShapedRecipe)
			collectShaped(size, inventory, matrix, (IShapedRecipe) recipe, 1);
		if (recipe instanceof IShapelessRecipe)
			collectShapeless(size, inventory, (IShapelessRecipe) recipe, 1);
	}
}