package de.ancash.fancycrafting.gui;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.inventory.Clickable;
import de.ancash.minecraft.inventory.IGUI;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;

public class PagedRecipesViewGUI extends IGUI{

	private final List<IRecipe> recipes;
	private int currentPage = 1;
	private final int maxPages;
	private final Player player;
	private final FancyCrafting pl;
	
	public PagedRecipesViewGUI(FancyCrafting pl, Player player, List<IRecipe> recipes) {
		super(player.getUniqueId(), 45, pl.getCustomRecipesTitle());
		this.recipes = recipes;
		int mp = 0;
		while(mp * 36 < recipes.size()) mp++;
		this.maxPages = mp;
		this.player = player;
		this.pl = pl;
		addInventoryItem(new InventoryItem(this, pl.getNextItem(), 44, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				if(topInventory)
					openNextPage();
			}
		}));
		
		addInventoryItem(new InventoryItem(this, pl.getBackItem(), 36, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				if(topInventory)
					openPrevPage();
			}
		}));
		IGUIManager.register(this, getId());
		open(currentPage);
		player.openInventory(getInventory());
	}
	
	public void openNextPage() {
		currentPage++;
		if(currentPage > maxPages) currentPage = 1;
		open(currentPage);
	}
	
	public void openPrevPage() {
		currentPage--;
		if(currentPage < 1) currentPage = maxPages;
		open(currentPage);
	}
	
	public void open(int page) {
		for(int i = 0; i<36; i++) {
			setItem(null, i);
			removeInventoryItem(i);
		}
		
		List<IRecipe> viewing = recipes.subList((page - 1) * 36, Math.min(page * 36, recipes.size()));
		int i = 0;
		for(IRecipe recipe : viewing) {
			addInventoryItem(new InventoryItem(this, recipe.getResult(), i, new Clickable() {
				
				@Override
				public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
					if(topInventory) {
						player.closeInventory();
						RecipeViewGUI.viewRecipe(pl, recipes.get((page - 1) * 36 + slot), player);
					}
				}
			}));
			i++;
		}
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
		IGUIManager.remove(getId());
	}

	@Override
	public void onInventoryDrag(InventoryDragEvent event) {
		event.setCancelled(true);
	}
}
