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

public abstract class AbstractRecipeCollectionPagedViewGUI extends IGUI {

	protected final List<IRecipe> recipes;
	protected int currentPage = 1;
	protected final int maxPages;
	protected final Player player;
	protected final FancyCrafting pl;

	public AbstractRecipeCollectionPagedViewGUI(FancyCrafting pl, Player player, List<IRecipe> recipes) {
		this(pl, player, recipes, pl.getWorkspaceObjects().getCustomRecipesTitle());
	}

	public AbstractRecipeCollectionPagedViewGUI(FancyCrafting pl, Player player, List<IRecipe> recipes,
			String title) {
		super(player.getUniqueId(), 54, title);
		Collections.sort(recipes,
				(a, b) -> pl.sortRecipesByRecipeName() ? a.getRecipeName().compareTo(b.getRecipeName())
						: a.getResultName().compareTo(b.getResultName()));
		this.recipes = recipes;
		int mp = 0;
		while (mp * 45 < recipes.size())
			mp++;
		this.maxPages = mp;
		this.player = player;
		this.pl = pl;
		addInventoryItem(new InventoryItem(this, pl.getWorkspaceObjects().getNextItem().getOriginal(), 53,
				(a, b, c, top) -> Optional.ofNullable(top ? this : null)
						.ifPresent(AbstractRecipeCollectionPagedViewGUI::openNextPage)));

		addInventoryItem(new InventoryItem(this, pl.getWorkspaceObjects().getBackItem().getOriginal(), 45,
				(a, b, c, top) -> Optional.ofNullable(top ? this : null)
						.ifPresent(AbstractRecipeCollectionPagedViewGUI::openPrevPage)));
		IGUIManager.register(this, getId());
		open(currentPage);
		open();
	}

	protected void openNextPage() {
		if (recipes.size() == 0)
			return;
		currentPage++;
		if (currentPage > maxPages)
			currentPage = 1;
		open(currentPage);
	}

	protected void openPrevPage() {
		if (recipes.size() == 0)
			return;
		currentPage--;
		if (currentPage < 1)
			currentPage = maxPages;
		open(currentPage);
	}

	protected void open(int page) {
		for (int i = 0; i < 45; i++) {
			setItem(null, i);
			removeInventoryItem(i);
		}

		List<IRecipe> viewing = recipes.subList((page - 1) * 45, Math.min(page * 45, recipes.size()));
		int i = 0;
		for (IRecipe recipe : viewing) {
			addInventoryItem(new InventoryItem(this,
					ItemStackUtils.setDisplayname(recipe.getResult(),
							pl.sortRecipesByRecipeName() ? recipe.getRecipeName() : recipe.getResultName()),
					i, (slot, b, c, top) -> Optional.ofNullable(top ? this : null)
							.ifPresent(self -> onRecipeClick(recipes.get((page - 1) * 45 + slot)))));
			i++;
		}
	}

	protected abstract void onRecipeClick(IRecipe recipe);

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
