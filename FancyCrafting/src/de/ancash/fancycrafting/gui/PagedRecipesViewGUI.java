package de.ancash.fancycrafting.gui;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.inventory.IGUI;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;

public class PagedRecipesViewGUI extends IGUI {

	private final List<IRecipe> recipes;
	private int currentPage = 1;
	private final int maxPages;
	private final Player player;
	private final FancyCrafting pl;

	public PagedRecipesViewGUI(FancyCrafting pl, Player player, List<IRecipe> recipes) {
		super(player.getUniqueId(), 45, pl.getWorkspaceObjects().getCustomRecipesTitle());
		Collections.sort(recipes,
				(a, b) -> pl.sortRecipesByRecipeName() ? a.getRecipeName().compareTo(b.getRecipeName())
						: a.getResultName().compareTo(b.getResultName()));
		this.recipes = recipes;
		int mp = 0;
		while (mp * 36 < recipes.size())
			mp++;
		this.maxPages = mp;
		this.player = player;
		this.pl = pl;
		addInventoryItem(new InventoryItem(this, pl.getWorkspaceObjects().getNextItem().getOriginal(), 44,
				(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(PagedRecipesViewGUI::openNextPage)));

		addInventoryItem(new InventoryItem(this, pl.getWorkspaceObjects().getBackItem().getOriginal(), 36,
				(a, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(PagedRecipesViewGUI::openPrevPage)));
		IGUIManager.register(this, getId());
		open(currentPage);
		open();
	}

	public void openNextPage() {
		currentPage++;
		if (currentPage > maxPages)
			currentPage = 1;
		open(currentPage);
	}

	public void openPrevPage() {
		currentPage--;
		if (currentPage < 1)
			currentPage = maxPages;
		open(currentPage);
	}

	public void open(int page) {
		for (int i = 0; i < 36; i++) {
			setItem(null, i);
			removeInventoryItem(i);
		}

		List<IRecipe> viewing = recipes.subList((page - 1) * 36, Math.min(page * 36, recipes.size()));
		int i = 0;
		for (IRecipe recipe : viewing) {
			addInventoryItem(new InventoryItem(this,
					ItemStackUtils.setDisplayname(recipe.getResult(),
							pl.sortRecipesByRecipeName() ? recipe.getRecipeName() : recipe.getResultName()),
					i, (slot, b, c, top) -> Optional.ofNullable(top ? this : null).ifPresent(
							self -> RecipeViewGUI.viewRecipe(pl, recipes.get((page - 1) * 36 + slot), player))));
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
