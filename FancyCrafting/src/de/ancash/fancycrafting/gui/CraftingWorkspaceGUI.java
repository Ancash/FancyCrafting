package de.ancash.fancycrafting.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IRandomRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;

public class CraftingWorkspaceGUI extends AbstractCraftingWorkspace {

	protected Set<Integer> quickCraftingResultHashCodes = new HashSet<>();
	protected int quickCraftingPage;
	protected List<IRecipe> quickCraftingRecipes = new ArrayList<>();

	public CraftingWorkspaceGUI(FancyCrafting pl, Player player, WorkspaceTemplate template) {
		super(pl, player, template);

		for (int i = 0; i < template.getDimension().getSize(); i++)
			setItem(pl.getWorkspaceObjects().getBackgroundItem().getOriginal(), i);
		for (int i : template.getSlots().getCraftingSlots())
			setItem(null, i);
		for (int i : template.getSlots().getAutoCraftingSlots())
			setItem(pl.getWorkspaceObjects().getQuickCraftingItem().getOriginal(), i);

		addInventoryItem(new InventoryItem(this, pl.getWorkspaceObjects().getCloseItem().getOriginal(),
				template.getSlots().getCloseSlot(),
				(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(CraftingWorkspaceGUI::closeAll)));
		IGUIManager.register(this, getId());
		Bukkit.getScheduler().runTask(pl, () -> {
			getRecipeMatchCompletionHandler().onNoRecipeMatch();
			updateQuickCrafting();
			open();
			player.updateInventory();
		});
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getClickedInventory() == null) {
			event.setCancelled(true);
			getAutoRecipeHandler().onAutoRecipesChangePage(event);
			return;
		}
		InventoryAction a = event.getAction();
		if (event.getClick().equals(ClickType.DOUBLE_CLICK) || a.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)
				&& !event.getInventory().equals(event.getClickedInventory())) {
			if (event.isShiftClick()) {

				if (event.getView().getBottomInventory().getItem(event.getSlot()) != null) {
					if (getItem(template.getSlots().getResultSlot()) != null) {
						IItemStack clicked = new IItemStack(
								event.getView().getBottomInventory().getItem(event.getSlot()));
						if (new IItemStack(getItem(template.getSlots().getResultSlot())).hashCode() == clicked
								.hashCode()
								|| pl.getWorkspaceObjects().getBackgroundItem().hashCode() == clicked.hashCode()
								|| pl.getWorkspaceObjects().getValidItem().hashCode() == clicked.hashCode()
								|| pl.getWorkspaceObjects().getInvalidItem().hashCode() == clicked.hashCode()
								|| pl.getWorkspaceObjects().getCloseItem().hashCode() == clicked.hashCode()) {
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

		if (workbenchInv && !craftingSlot && event.getSlot() != template.getSlots().getResultSlot()) {
			event.setCancelled(true);
			return;
		}

		if (workbenchInv && craftingSlot) {
			event.setCancelled(false);
		} else if (workbenchInv && event.getSlot() == template.getSlots().getResultSlot()) {
			event.setCancelled(true);
			if (getCurrentRecipe() != null) {
				craftItem(event, currentRecipe, new int[] { matrix.getLeftMoves(), matrix.getUpMoves() });
				return;
			}
		}
		if (!event.isCancelled()) {
			if (((workbenchInv && onTopInventoryClick(event)))) {
				event.setCancelled(true);
				updateAll();
				return;
			}
			if (!workbenchInv && onBottomInventoryClick(event)) {
				event.setCancelled(true);
				updateAll();
				return;
			}
		}
		checkDelayed();
	}

	private boolean onBottomInventoryClick(InventoryClickEvent event) {
		if (!event.isShiftClick()) {
			return false;
		}
		ItemStack shift = event.getClickedInventory().getItem(event.getSlot());
		if (shift == null || shift.getType() == Material.AIR)
			return false;

		IItemStack iShift = new IItemStack(shift);
		if (iShift.hashCode() == pl.getWorkspaceObjects().getCloseItem().hashCode()
				|| iShift.hashCode() == pl.getWorkspaceObjects().getBackgroundItem().hashCode()
				|| iShift.hashCode() == pl.getWorkspaceObjects().getValidItem().hashCode()
				|| iShift.hashCode() == pl.getWorkspaceObjects().getInvalidItem().hashCode()
				|| quickCraftingResultHashCodes.contains(iShift.hashCode())) {
			event.setCancelled(true);
			return false;
		}

		return false;
	}

	private boolean onTopInventoryClick(InventoryClickEvent event) {
		ItemStack slotItem = event.getCurrentItem();
		ItemStack cursor = event.getCursor();
		boolean isCursorNull = cursor == null || cursor.getType() == Material.AIR;
		boolean isSlotNull = slotItem == null || slotItem.getType() == Material.AIR;

		if (isSlotNull && isCursorNull && event.getAction() != InventoryAction.HOTBAR_SWAP)
			return true;
		if (event.getAction() == InventoryAction.HOTBAR_SWAP
				|| event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
			onTopHotbawSwap(event, slotItem);
			return true;
		}
		if (!isSlotNull && event.isShiftClick()) {
			onTopSlotNotNullShift(event, slotItem);
			return true;
		} else if (isCursorNull && !isSlotNull) {
			onTopCursorNullSlotNotNull(event, slotItem);
			return true;
		} else if (!isCursorNull && isSlotNull) {
			onTopCursorNotNullSlotNull(event, cursor);
			return true;
		} else if (!isCursorNull && !isSlotNull) {
			onTopCursorNotNullSlotNotNull(event, slotItem, cursor);
			return true;
		}
		return false;
	}

	private void onTopHotbawSwap(InventoryClickEvent event, ItemStack slotItem) {
		event.getInventory().setItem(event.getSlot(),
				event.getWhoClicked().getInventory().getItem(event.getHotbarButton()));
		event.getWhoClicked().getInventory().setItem(event.getHotbarButton(), slotItem);
	}

	private void onTopSlotNotNullShift(InventoryClickEvent event, ItemStack slotItem) {
		ItemStack toAdd = slotItem.clone();
		toAdd.setAmount(1);
		int free = InventoryUtils.getFreeSpaceExact(getPlayerInventoryContents(event.getWhoClicked().getInventory()),
				toAdd);
		int adding = free < slotItem.getAmount() ? free : slotItem.getAmount();
		InventoryUtils.addItemAmount(adding, toAdd, player);
		setAmount(slotItem.getAmount(), adding, event.getSlot(), event.getInventory());
	}

	private void onTopCursorNullSlotNotNull(InventoryClickEvent event, ItemStack slotItem) {
		if (event.isLeftClick()) {
			event.getWhoClicked().setItemOnCursor(event.getInventory().getItem(event.getSlot()));
			event.getInventory().setItem(event.getSlot(), null);
		} else {
			int taking = (int) Math.ceil(slotItem.getAmount() / 2);
			if (slotItem.getAmount() == 1)
				taking = 1;
			ItemStack clone = slotItem.clone();
			clone.setAmount(taking);
			event.getWhoClicked().setItemOnCursor(clone);
			setAmount(slotItem.getAmount(), taking, event.getSlot(), event.getInventory());
		}
	}

	private void onTopCursorNotNullSlotNull(InventoryClickEvent event, ItemStack cursor) {
		if (event.isLeftClick()) {
			event.getInventory().setItem(event.getSlot(), cursor);
			event.getWhoClicked().setItemOnCursor(null);
		} else {
			ItemStack clone = cursor.clone();
			clone.setAmount(1);
			event.getInventory().setItem(event.getSlot(), clone);
			if (cursor.getAmount() - 1 > 0)
				cursor.setAmount(cursor.getAmount() - 1);
			else
				event.getWhoClicked().setItemOnCursor(null);
		}
	}

	private void onTopCursorNotNullSlotNotNull(InventoryClickEvent event, ItemStack slotItem, ItemStack cursor) {
		IItemStack iSlotItem = new IItemStack(slotItem);
		IItemStack iCursor = new IItemStack(cursor);
		if (iSlotItem.hashCode() == iCursor.hashCode() && slotItem.getAmount() < slotItem.getMaxStackSize()) {
			if (event.isLeftClick()) {
				int free = slotItem.getMaxStackSize() - slotItem.getAmount();
				int adding = free > cursor.getAmount() ? cursor.getAmount() : free;
				slotItem.setAmount(slotItem.getAmount() + adding);
				if (cursor.getAmount() - adding > 0)
					cursor.setAmount(cursor.getAmount() - adding);
				else
					event.getWhoClicked().setItemOnCursor(null);
			} else {
				slotItem.setAmount(slotItem.getAmount() + 1);
				if (cursor.getAmount() - 1 > 0)
					cursor.setAmount(cursor.getAmount() - 1);
				else
					event.getWhoClicked().setItemOnCursor(null);
			}
		} else {
			event.getWhoClicked().setItemOnCursor(slotItem);
			event.getInventory().setItem(event.getSlot(), cursor);
		}
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
		IGUIManager.remove(getId());
		for (int i : template.getSlots().getCraftingSlots()) {
			ItemStack item = getItem(i);
			if (item == null || item.getType().equals(XMaterial.AIR.parseMaterial()))
				continue;
			if (InventoryUtils.getFreeSpaceExact(getPlayerInventoryContents(player.getInventory()), item) >= item
					.getAmount())
				player.getInventory().addItem(item);
			else
				player.getWorld().dropItem(player.getLocation(), item);
		}
	}

	@Override
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getRawSlots().stream().filter(i -> i < template.getDimension().getSize())
				.filter(i -> !isCraftingSlot(i)).findAny().isPresent()) {
			event.setCancelled(true);
			return;
		}
		checkDelayed();
	}

	private void craftItem(InventoryClickEvent event, IRecipe recipe, int[] moves) {
		if (recipe == null) {
			updateAll();
			return;
		}
		ItemStack cursor = event.getCursor();
		if (event.getAction().equals(InventoryAction.PICKUP_ALL)) {
			if (cursor != null && cursor.getType() == Material.AIR) {
				collectIngredients(event.getInventory(), recipe);
				event.getWhoClicked()
						.setItemOnCursor(getRecipeMatchCompletionHandler().getSingleRecipeCraft(recipe, player));
				updateAll();
				return;
			}
		} else if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
			if (recipe.isShiftCollectable()) {
				InventoryUtils.addItemAmount(
						shiftCollectIngredients(event.getInventory(), recipe) * recipe.getResult().getAmount(),
						recipe.getResult(), player);
				updateAll();
			} else {
				ItemStack result = getRecipeMatchCompletionHandler().getSingleRecipeCraft(recipe, player);
				if (InventoryUtils.getFreeSpaceExact(getPlayerInventoryContents(event.getWhoClicked().getInventory()),
						result) >= result.getAmount()) {
					collectIngredients(event.getInventory(), recipe);
					event.getWhoClicked().getInventory().addItem(result);
				}
				updateAll();
			}
			return;
		} else if ((event.getAction().equals(InventoryAction.PLACE_ONE)
				|| event.getAction().equals(InventoryAction.PLACE_ALL))
				&& new IItemStack(cursor).hashCode() == recipe.getResultAsIItemStack().hashCode()
				&& cursor.getAmount() + recipe.getResult().getAmount() <= cursor.getMaxStackSize()
				&& !(recipe instanceof IRandomRecipe)) {
			cursor.setAmount(cursor.getAmount() + recipe.getResult().getAmount());
			collectIngredients(event.getInventory(), recipe);
			updateAll();
			return;
		}
		updateAll();
	}

	private int shiftCollectIngredients(Inventory inventory, IRecipe recipe) {
		int space = InventoryUtils.getFreeSpaceExact(getPlayerInventoryContents(player.getInventory()),
				recipe.getResult());
		if (space <= 0) {
			return 0;
		}
		int shiftSize = space / recipe.getResult().getAmount();
		if (recipe instanceof IShapedRecipe) {
			for (int i = 0; i < matrix.getArray().length; ++i) {
				if (matrix.getArray()[i] == null)
					continue;
				if (recipe.isVanilla()) {
					shiftSize = Math.min(shiftSize, matrix.getArray()[i].getOriginal().getAmount());
				} else {
					IItemStack iIngredient = ((IShapedRecipe) recipe).getIngredientsArray()[i];
					IItemStack iCompare = matrix.getArray()[i];
					if (!iIngredient.isSimilar(iCompare))
						continue;
					shiftSize = Math.min(shiftSize,
							(int) (iCompare.getOriginal().getAmount() / iIngredient.getOriginal().getAmount()));
				}
			}
		}
		if (recipe instanceof IShapelessRecipe) {
			for (ItemStack ingredient : ((IShapelessRecipe) recipe).getIngredients()) {
				if (ingredient == null)
					continue;
				for (IItemStack currentItem : matrix.getArray()) {
					if (currentItem == null)
						continue;
					if (recipe.isVanilla())
						shiftSize = Math.min(shiftSize, currentItem.getOriginal().getAmount());
					else if (new IItemStack(ingredient).isSimilar(currentItem))
						shiftSize = Math.min(shiftSize,
								(int) (currentItem.getOriginal().getAmount() / ingredient.getAmount()));
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

	private void collectShapeless(Inventory inventory, IShapelessRecipe shapeless, int multiplicator) {
		Set<Integer> done = new HashSet<>();
		for (IItemStack ingredient : shapeless.getIIngredients()) {
			for (int craftSlot : template.getSlots().getCraftingSlots()) {
				if (done.contains(craftSlot))
					continue;
				if (inventory.getItem(craftSlot) == null)
					continue;
				if (ingredient.hashCode() != new IItemStack(inventory.getItem(craftSlot)).hashCode()
						&& !shapeless.isVanilla())
					continue;
				setAmount(inventory.getItem(craftSlot).getAmount(),
						ingredient.getOriginal().getAmount() * multiplicator, craftSlot, inventory);
				done.add(craftSlot);
			}
		}
	}

	private void collectShaped(Inventory inventory, IShapedRecipe shaped, int multiplicator) {
		synchronized (super.lock) {
			int base = template.getDimension().getWidth() * matrix.getUpMoves() + matrix.getLeftMoves();
			for (int i = 0; i < shaped.getIngredientsArray().length; i++) {
				if (shaped.getIngredientsArray()[i] == null)
					continue;
				IItemStack ing = matrix.getArray()[i];
				int amount = ing.getOriginal().getAmount()
						- shaped.getIngredientsArray()[i].getOriginal().getAmount() * multiplicator;
				int slot = template.getSlots().getCraftingSlots()[base
						+ i / shaped.getWidth() * template.getDimension().getWidth() + i % shaped.getWidth()];
				if (amount > 0)
					getItem(slot).setAmount(amount);
				else
					setItem(null, slot);
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