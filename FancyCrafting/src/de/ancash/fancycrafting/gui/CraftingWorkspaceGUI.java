package de.ancash.fancycrafting.gui;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.fancycrafting.CraftingTemplate;
import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.inventory.Clickable;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;

public class CraftingWorkspaceGUI extends AbstractCraftingWorkspace {

	public CraftingWorkspaceGUI(FancyCrafting pl, Player player, CraftingTemplate template) {
		super(pl, player, template);
		for (int i = 0; i < template.getSize(); i++)
			setItem(pl.getBackgroundItem(), i);
		for (int i : template.getCraftingSlots())
			setItem(null, i);

		addInventoryItem(new InventoryItem(this, pl.getCloseItem(), template.getCloseSlot(), new Clickable() {

			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				if (topInventory)
					closeAll();
			}
		}));
		onNoRecipeMatch();
		IGUIManager.register(this, getId());
		player.openInventory(getInventory());
		new BukkitRunnable() {

			@Override
			public void run() {
				player.updateInventory();
			}
		}.runTask(pl);
	}

	@Override
	public boolean canCraftRecipe(IRecipe recipe, Player pl) {
		return player.isOp() || player.hasPermission("fancycrafting.craft." + recipe.getName().replace(" ", "-"));
	}

	@Override
	public ItemStack[] getIngredients() {
		ItemStack[] ings = new ItemStack[template.getCraftingSlots().length];
		for (int i = 0; i < ings.length; i++)
			ings[i] = getItem(template.getCraftingSlots()[i]);
		return ings;
	}

	@Override
	public void onRecipeMatch() {
		new BukkitRunnable() {

			@Override
			public void run() {
				synchronized (lock) {
					setItem(getCurrentRecipe().getResult(), template.getResultSlot());
					for (int i : template.getCraftStateSlots())
						setItem(pl.getValidItem(), i);
				}
			}
		}.runTask(pl);
	}

	@Override
	public void onNoRecipeMatch() {
		new BukkitRunnable() {

			@Override
			public void run() {
				synchronized (lock) {
					setItem(pl.getInvalidItem(), template.getResultSlot());
					for (int i : template.getCraftStateSlots())
						setItem(pl.getInvalidItem(), i);
				}
			}
		}.runTask(pl);
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getClickedInventory() == null) {
			event.setCancelled(true);
			return;
		}
		InventoryAction a = event.getAction();
		if (event.getClick().equals(ClickType.DOUBLE_CLICK) || (a.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)
				&& !event.getInventory().equals(event.getClickedInventory()))) {
			if (event.isShiftClick()) {

				if (event.getView().getBottomInventory().getItem(event.getSlot()) != null) {
					if (getItem(template.getResultSlot()) != null) {
						IItemStack clicked = new IItemStack(
								event.getView().getBottomInventory().getItem(event.getSlot()));
						if (new IItemStack(getItem(template.getResultSlot())).isSimilar(clicked)
								|| new IItemStack(pl.getBackgroundItem()).isSimilar(clicked)
								|| new IItemStack(pl.getValidItem()).isSimilar(clicked)
								|| new IItemStack(pl.getInvalidItem()).isSimilar(clicked)
								|| new IItemStack(pl.getCloseItem()).isSimilar(clicked)) {
							event.setCancelled(true);
							return;
						}
					}
				}
			} else {
				event.setCancelled(true);
				return;
			}
		}
		boolean workbenchInv = event.getClickedInventory() != null
				&& event.getClickedInventory().equals(event.getInventory());
		boolean craftingSlot = isCraftingSlot(event.getSlot());

		if (workbenchInv && !craftingSlot && event.getSlot() != template.getResultSlot()) {
			event.setCancelled(true);
			return;
		}

		if (workbenchInv && craftingSlot) {
			event.setCancelled(false);
		} else if (workbenchInv && event.getSlot() == template.getResultSlot()) {
			event.setCancelled(true);
			if (getCurrentRecipe() != null) {
				craftItem(event, currentRecipe, new int[] { matrix.getLeftMoves(), matrix.getUpMoves() });
				return;
			}
		}
		checkRecipeDelayed();
	}

	public boolean isCraftingSlot(int s) {
		for (int i = 0; i < template.getCraftingSlots().length; i++)
			if (template.getCraftingSlots()[i] == s)
				return true;
		return false;
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
		IGUIManager.remove(getId());
		for (int i : template.getCraftingSlots()) {
			ItemStack item = getItem(i);
			if (item == null || item.getType().equals(XMaterial.AIR.parseMaterial()))
				continue;
			if (InventoryUtils.getFreeSpaceExact(getInventoryContents(player.getInventory()), item) >= item.getAmount())
				player.getInventory().addItem(item);
			else
				player.getWorld().dropItem(player.getLocation(), item);
		}
	}

	private ItemStack[] getInventoryContents(PlayerInventory inv) {
		try {
			return player.getInventory().getStorageContents();
		} catch (NoSuchMethodError e) {
			return player.getInventory().getContents();
		}
	}

	@Override
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getInventory() == null || event.getInventorySlots().contains(template.getResultSlot())) {
			event.setCancelled(true);
			return;
		}
		checkRecipeDelayed();
	}

	@Override
	public void onNoPermission(IRecipe recipe, Player p) {
		onNoRecipeMatch();
	}

	private void checkRecipe() {
		updateMatrix();

		if (pl.checkRecipesAsync())
			matchRecipeAsync();
		else
			matchRecipe();
	}

	private void checkRecipeDelayed() {

		new BukkitRunnable() {

			@Override
			public void run() {
				updateMatrix();

				if (pl.checkRecipesAsync())
					matchRecipeAsync();
				else
					matchRecipe();
			}
		}.runTaskLater(pl, 1);
	}

	@SuppressWarnings("deprecation")
	private void craftItem(InventoryClickEvent event, IRecipe recipe, int[] moves) {

		if (recipe == null) {
			checkRecipe();
			return;
		}
		ItemStack cursor = event.getCursor();
		if (event.getAction().equals(InventoryAction.PICKUP_ALL)) {
			if (cursor != null && cursor.getType().equals(XMaterial.AIR.parseMaterial())) {
				collectIngredients(event.getInventory(), recipe);
				event.setCursor(recipe.getResult());
				checkRecipe();
				return;
			}
		} else if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
			InventoryUtils.addItemAmount(shiftCollectIngredients(event.getInventory(), recipe), recipe.getResult(),
					player);
			checkRecipe();
			return;
		} else if (event.getAction().equals(InventoryAction.PLACE_ONE)
				|| event.getAction().equals(InventoryAction.PLACE_ALL)) {
			if (new IItemStack(cursor).isSimilar(recipe.getResult())) {
				if (cursor.getAmount() + recipe.getResult().getAmount() <= cursor.getMaxStackSize()) {
					cursor.setAmount(cursor.getAmount() + recipe.getResult().getAmount());
					collectIngredients(event.getInventory(), recipe);
					checkRecipe();
					return;
				}
			}
		}
		checkRecipe();
		player.updateInventory();
	}

	private int shiftCollectIngredients(Inventory inventory, IRecipe recipe) {
		int space = InventoryUtils.getFreeSpaceExact(getInventoryContents(player.getInventory()), recipe.getResult());
		if (space <= 0) {
			return 0;
		}
		int shiftSize = space / recipe.getResult().getAmount();
		if (recipe instanceof IShapedRecipe) {
			for (int i = 0; i < matrix.getArray().length; ++i) {
				if (matrix.getArray()[i] == null)
					continue;
				if (recipe.isVanilla()) {
					shiftSize = Math.min(shiftSize, matrix.getArray()[i].getAmount());
				} else {
					IItemStack serializedIngredient = new IItemStack(((IShapedRecipe) recipe).getIngredientsArray()[i]);
					IItemStack serializedCompareTo = new IItemStack(matrix.getArray()[i]);
					if (!serializedIngredient.isSimilar(serializedCompareTo))
						continue;
					shiftSize = Math.min(shiftSize, (int) (serializedCompareTo.getOriginal().getAmount()
							/ serializedIngredient.getOriginal().getAmount()));
				}
			}
		}
		if (recipe instanceof IShapelessRecipe) {
			for (ItemStack ingredient : ((IShapelessRecipe) recipe).getIngredients()) {
				if (ingredient == null)
					continue;
				for (ItemStack currentItem : matrix.getArray()) {
					if (currentItem == null)
						continue;
					if (recipe.isVanilla())
						shiftSize = Math.min(shiftSize, currentItem.getAmount());
					else if (new IItemStack(ingredient).isSimilar(currentItem))
						shiftSize = Math.min(shiftSize, (int) (currentItem.getAmount() / ingredient.getAmount()));
				}
			}
		}
		if (recipe instanceof IShapedRecipe) {
			collectShaped(inventory, (IShapedRecipe) recipe, shiftSize);
		} else if (recipe instanceof IShapelessRecipe) {
			collectShapeless(inventory, (IShapelessRecipe) recipe, shiftSize);
		}
		return shiftSize;
	}

	private void setAmount(int a, int b, int slot, Inventory inventory) {
		ItemStack is = inventory.getItem(slot);
		if (a - b <= 0) {
			inventory.setItem(slot, null);
		} else {
			is.setAmount(a - b);
		}
	}

	private void collectShapeless(Inventory inventory, IShapelessRecipe shapeless, int multiplicator) {
		Set<Integer> done = new HashSet<>();
		for (ItemStack ingredient : shapeless.getIngredients()) {
			for (int craftSlot : template.getCraftingSlots()) {
				if (done.contains(craftSlot))
					continue;
				if (inventory.getItem(craftSlot) == null)
					continue;
				if (!new IItemStack(ingredient).isSimilar(new IItemStack(inventory.getItem(craftSlot)))
						&& !shapeless.isVanilla())
					continue;
				setAmount(inventory.getItem(craftSlot).getAmount(), ingredient.getAmount() * multiplicator, craftSlot,
						inventory);
				done.add(craftSlot);
			}
		}
	}

	private void collectShaped(Inventory inventory, IShapedRecipe shaped, int multiplicator) {
		synchronized (super.lock) {
			int base = template.getWidth() * matrix.getUpMoves() + matrix.getLeftMoves();
			for (int i = 0; i < shaped.getIngredientsArray().length; i++) {
				if (shaped.getIngredientsArray()[i] == null)
					continue;
				ItemStack ing = matrix.getArray()[i];
				ItemStack orig = shaped.getIngredientsArray()[i];
				int amount = ing.getAmount() - orig.getAmount() * multiplicator;
				int slot = template.getCraftingSlots()[base + i / shaped.getWidth() * template.getWidth()
						+ i % shaped.getWidth()];
				if (amount > 0)
					ing.setAmount(amount);
				else {
					setItem(null, slot);
				}
			}
		}
	}

	private void collectIngredients(Inventory inventory, IRecipe recipe) {
		if (recipe instanceof IShapedRecipe)
			collectShaped(inventory, (IShapedRecipe) recipe, 1);
		if (recipe instanceof IShapelessRecipe)
			collectShapeless(inventory, (IShapelessRecipe) recipe, 1);
	}
}