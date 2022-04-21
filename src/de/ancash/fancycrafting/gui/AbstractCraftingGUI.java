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
import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.fancycrafting.recipe.VanillaRecipeMatcher;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.inventory.Clickable;
import de.ancash.minecraft.inventory.IGUI;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;

public abstract class AbstractCraftingGUI extends IGUI{

	protected final Player player;
	protected final FancyCrafting pl;
	protected final AbstractCraftingGUI instance = this;
	protected IRecipe currentRecipe;
	protected final CraftingTemplate template;
	protected IMatrix<ItemStack> matrix;
	
	public AbstractCraftingGUI(FancyCrafting pl, Player player, CraftingTemplate template) {
		super(player.getUniqueId(), template.getSize(), template.getTitle());
		this.template = template;
		this.player = player;
		this.pl = pl;
		for(int i = 0; i<template.getSize(); i++)
			setItem(pl.getBackgroundItem(), i);
		for(int i : template.getCraftingSlots())
			setItem(null, i);
		addInventoryItem(new InventoryItem(this, pl.getCloseItem(), template.getCloseSlot(), new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				if(topInventory)
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
	
	public IMatrix<ItemStack> getCraftingItems() {
		ItemStack[] ings = new ItemStack[template.getCraftingSlots().length];
		for(int i = 0; i<ings.length; i++)
			ings[i] = getItem(template.getCraftingSlots()[i]);
		matrix = new IMatrix<>(ings, template.getWidth(), template.getHeight());
		matrix.optimize();
		return matrix;
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getClickedInventory() == null) {
			event.setCancelled(true);
			return;
		}
		InventoryAction a = event.getAction();
		if(event.getClick().equals(ClickType.DOUBLE_CLICK) || (a.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && !event.getInventory().equals(event.getClickedInventory()))) {
			event.setCancelled(true);
			return;
		}
		boolean workbenchInv = event.getClickedInventory() != null && event.getClickedInventory().equals(event.getInventory());
		boolean craftingSlot = isCraftingSlot(event.getSlot());
		
		if(workbenchInv && !craftingSlot && event.getSlot() != template.getResultSlot()) {
			event.setCancelled(true);
			return;
		}
		
		if(!workbenchInv && event.isShiftClick()) {
			ItemStack is2 = event.getView().getBottomInventory().getItem(event.getSlot());
			ItemStack is1 = getItem(template.getResultSlot());
			if(is2 != null && is1 != null) {
				if(new IItemStack(is1).isSimilar(new IItemStack(is2))) {
					event.setCancelled(true);
					return;
				}
			}
		}
		
		if(workbenchInv && craftingSlot) {
			event.setCancelled(false);
		} else if(workbenchInv && event.getSlot() == template.getResultSlot()) {
			event.setCancelled(true);
			if(getCurrentRecipe() != null) {
				craftItem(event, currentRecipe, new int[] {matrix.getLeftMoves(), matrix.getUpMoves()});
			}
		}
		checkRecipe();
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
		IGUIManager.remove(getId());
		for(int i : template.getCraftingSlots()) {
			ItemStack item = getItem(i);
			if(item == null || item.getType().equals(XMaterial.AIR.parseMaterial())) continue;
			if(InventoryUtils.getFreeSpaceExact(getInventoryContents(player.getInventory()), item) >= item.getAmount())
				player.getInventory().addItem(item);
			else
				player.getWorld().dropItem(player.getLocation(), item);
		}
	}

	@Override
	public void onInventoryDrag(InventoryDragEvent event) {
		if(event.getInventory() == null || event.getInventorySlots().contains(template.getResultSlot())) {
			event.setCancelled(true);
			return;
		}
		checkRecipe();
	}
	
	public boolean isCraftingSlot(int s) {
		for(int i = 0; i<template.getCraftingSlots().length; i++)
			if(template.getCraftingSlots()[i] == s) return true;
		return false;
	}
	
	public void checkRecipe() {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				ItemStack[] ings = getCraftingItems().getArray();
				IRecipe recipe = pl.getRecipeManager().matchRecipe(ings, matrix.getWidth(), matrix.getHeight(), instance);
				if(recipe == null) {
					currentRecipe = null;
					onNoRecipeMatch();
				}else {
					currentRecipe = recipe;
					if(recipe.isVanilla())
						if(!FancyCrafting.permsForVanillaRecipes())
							onRecipeMatch();
						else
							if(canCraftRecipe(player))
								onRecipeMatch();
							else
								onNoPermission();
					else
						if(!FancyCrafting.permsForCustomRecipes())
							onRecipeMatch();
						else
							if(canCraftRecipe(player))
								onRecipeMatch();
							else
								onNoPermission();
				}
				player.updateInventory();
			}
		}.runTaskLater(pl, 1);
	}
	
	private boolean canCraftRecipe(Player player) {
		return player.isOp() || player.hasPermission("fancycrafting.craft." + getCurrentRecipe().getName().replace(" ", "-"));
	}
	
	public IRecipe getCurrentRecipe() {
		return currentRecipe;
	}
	
	public int getResultSlot() {
		return template.getResultSlot();
	}
	
	public VanillaRecipeMatcher getVanillaRecipeMatcher() {
		return FancyCrafting.getVanillaRecipeMatcher(player);
	}
	
	private void craftItem(InventoryClickEvent event, IRecipe recipe, int[] moves) {
		
		if(recipe == null) {
			checkRecipe();
			return;
		}
		ItemStack cursor = event.getCursor();
		if(event.getAction().equals(InventoryAction.PICKUP_ALL)) {
			if(cursor != null && cursor.getType().equals(XMaterial.AIR.parseMaterial())) {
				event.setCancelled(false);
				collectIngredients(event.getInventory(), recipe);
			}
		} else if(event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
			int toAdd = shiftCollectIngredients(event.getInventory(), recipe);
			ItemStack results = recipe.getResult().clone();
			for(int i = 0; i<toAdd; i++)
				event.getWhoClicked().getInventory().addItem(results);
		} else if(event.getAction().equals(InventoryAction.PLACE_ONE) || event.getAction().equals(InventoryAction.PLACE_ALL)) {
			if(new IItemStack(cursor).isSimilar(recipe.getResult())) {
				if(cursor.getAmount() + recipe.getResult().getAmount() <= cursor.getMaxStackSize()) {
					cursor.setAmount(cursor.getAmount() + recipe.getResult().getAmount());
					collectIngredients(event.getInventory(), recipe);
				}
			}
		}
		checkRecipe();
		player.updateInventory();
	}
	
	private int shiftCollectIngredients(Inventory inventory, IRecipe recipe) {
		int space = InventoryUtils.getFreeSpaceExact(getInventoryContents(player.getInventory()), recipe.getResult());
		if(space <= 0) {
			return 0;
		}
		int shiftSize = space / recipe.getResult().getAmount();
		if(recipe instanceof IShapedRecipe) {
			for (int i = 0; i < matrix.getArray().length; ++i) {
				if(matrix.getArray()[i] == null) continue;
				if(recipe.isVanilla()) {
					shiftSize = Math.min(shiftSize, matrix.getArray()[i].getAmount());
				} else {
					IItemStack serializedIngredient = new IItemStack(((IShapedRecipe)recipe).getIngredientsArray()[i]);
					IItemStack serializedCompareTo = new IItemStack(matrix.getArray()[i]);
					if (!serializedIngredient.isSimilar(serializedCompareTo)) continue;
		            shiftSize = Math.min(shiftSize, (int)(serializedCompareTo.getOriginal().getAmount() / serializedIngredient.getOriginal().getAmount()));
				}
			}
		}
		if(recipe instanceof IShapelessRecipe) {
			for(ItemStack ingredient : ((IShapelessRecipe) recipe).getIngredients()) {
				if(ingredient == null) continue;
				for(ItemStack currentItem : matrix.getArray()) {
					if(currentItem == null) continue;
					if(recipe.isVanilla())
						shiftSize = Math.min(shiftSize, currentItem.getAmount());
					else
						if(new IItemStack(ingredient).isSimilar(currentItem)) 
							shiftSize = Math.min(shiftSize, (int)(currentItem.getAmount() / ingredient.getAmount()));
				}
			}
		}
		if(recipe instanceof IShapedRecipe) {
			collectShaped(inventory, (IShapedRecipe) recipe, shiftSize);
		} else if(recipe instanceof IShapelessRecipe) {
			collectShapeless(inventory, (IShapelessRecipe) recipe, shiftSize);
		}
		return shiftSize;
	}
	
	private void setAmount(int a, int b, int slot, Inventory inventory) {
		ItemStack is = inventory.getItem(slot);
		if(a - b <= 0) {
			inventory.setItem(slot, null);
		} else {
			is.setAmount(a - b);
		}
	}
	
	private void collectShapeless(Inventory inventory, IShapelessRecipe shapeless, int multiplicator) {
		Set<Integer> done = new HashSet<>();
		for(ItemStack ingredient : shapeless.getIngredients()) {
			for(int craftSlot : template.getCraftingSlots()) {
				if(done.contains(craftSlot)) continue;
				if(inventory.getItem(craftSlot) == null) continue;
				if(!new IItemStack(ingredient).isSimilar(new IItemStack(inventory.getItem(craftSlot))) && !shapeless.isVanilla()) continue;
				setAmount(inventory.getItem(craftSlot).getAmount(), ingredient.getAmount() * multiplicator, craftSlot, inventory);
				done.add(craftSlot);
			}
		}
	}
	
	private void collectShaped(Inventory inventory, IShapedRecipe shaped, int multiplicator) {
		int base = template.getWidth() * matrix.getUpMoves() + matrix.getLeftMoves();
		for(int i = 0; i<shaped.getIngredientsArray().length; i++) {
			if(shaped.getIngredientsArray()[i] == null) continue;
			ItemStack ing = matrix.getArray()[i];
			ItemStack orig = shaped.getIngredientsArray()[i];
			int amount = ing.getAmount() - orig.getAmount() * multiplicator;
			int slot = template.getCraftingSlots()[base + i / shaped.getWidth() * template.getWidth() + i % shaped.getWidth()];
			if(amount > 0)
				ing.setAmount(amount);
			else {
				setItem(null, slot);
			}
		}
	}
	
	private void collectIngredients(Inventory inventory, IRecipe recipe) {
		if(recipe instanceof IShapedRecipe) 
			collectShaped(inventory, (IShapedRecipe) recipe, 1);
		if(recipe instanceof IShapelessRecipe) 
			collectShapeless(inventory, (IShapelessRecipe) recipe, 1);
	}
	
	private ItemStack[] getInventoryContents(PlayerInventory inv) {
		try {
			return player.getInventory().getStorageContents();
		} catch(NoSuchMethodError e) {
			return player.getInventory().getContents();
		}
	}
	
	public abstract void onNoPermission();
	
	public abstract void onRecipeMatch();
	
	public abstract void onNoRecipeMatch();
}