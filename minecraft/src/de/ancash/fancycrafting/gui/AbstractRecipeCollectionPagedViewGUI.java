package de.ancash.fancycrafting.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.RecipeCategory;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.cryptomorin.xseries.XMaterial;
import de.ancash.minecraft.inventory.IGUI;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;

public abstract class AbstractRecipeCollectionPagedViewGUI extends IGUI {

	protected final List<IRecipe> recipes;
	protected final List<IRecipe> filteredRecipes;
	protected int currentPage = 1;
	protected final int maxPages;
	protected final Player player;
	protected final FancyCrafting pl;
	protected int categoryPos = 0;

	public AbstractRecipeCollectionPagedViewGUI(FancyCrafting pl, Player player, List<IRecipe> recipes) {
		this(pl, player, recipes, pl.getWorkspaceObjects().getCustomRecipesTitle());
	}

	public AbstractRecipeCollectionPagedViewGUI(FancyCrafting pl, Player player, List<IRecipe> recipes, String title) {
		super(player.getUniqueId(), 54, title);
		Collections.sort(recipes,
				(a, b) -> pl.sortRecipesByRecipeName() ? a.getRecipeName().compareTo(b.getRecipeName())
						: a.getResultName().compareTo(b.getResultName()));
		this.recipes = recipes;
		this.filteredRecipes = new ArrayList<>(recipes);
		int mp = 0;
		while (mp * 45 < filteredRecipes.size())
			mp++;
		this.maxPages = mp;
		this.player = player;
		this.pl = pl;
		addInventoryItem(new InventoryItem(this, pl.getWorkspaceObjects().getNextItem().getOriginal(), 53,
				(a, b, c, top) -> Optional.ofNullable(top ? this : null)
						.ifPresent(AbstractRecipeCollectionPagedViewGUI::openNextPage)));

		addInventoryItem(new InventoryItem(this, pl.getWorkspaceObjects().getPrevItem().getOriginal(), 45,
				(a, b, c, top) -> Optional.ofNullable(top ? this : null)
						.ifPresent(AbstractRecipeCollectionPagedViewGUI::openPrevPage)));
		addCategoryFilter();
		IGUIManager.register(this, getId());
		open(currentPage);
		open();
	}

	protected void calcFilteredRecipes() {
		filteredRecipes.clear();
		if (categoryPos == 0) {
			filteredRecipes.addAll(recipes);
			return;
		}
		RecipeCategory cat = RecipeCategory
				.getCategory(new ArrayList<>(RecipeCategory.getCategories()).get(categoryPos - 1));
		recipes.stream().filter(r -> r.getCategory().equals(cat)).forEach(filteredRecipes::add);
		;
	}

	@SuppressWarnings("nls")
	protected void addCategoryFilter() {
		ItemStack item = XMaterial.CHEST.parseItem();
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§aFilter Categories");
		List<String> lore = new ArrayList<>();
		List<String> cats = new ArrayList<>(RecipeCategory.getCategories());
		Collections.sort(cats);
		if (categoryPos > cats.size())
			categoryPos = 0;

		lore.add("§aClick to switch");

		if (categoryPos == 0)
			lore.add("§eAll");
		else
			lore.add("§7All");
		for (int i = 0; i < cats.size(); i++)
			if (i + 1 == categoryPos)
				lore.add("§e" + cats.get(i));
			else
				lore.add("§7" + cats.get(i));
		meta.setLore(lore);
		item.setItemMeta(meta);
		addInventoryItem(new InventoryItem(this, item, 52, (a, b, c, top) -> {
			if (!top)
				return;
			categoryPos++;
			currentPage = 1;
			addCategoryFilter();
			calcFilteredRecipes();
			open(currentPage);
		}));
	}

	protected void openNextPage() {
		if (filteredRecipes.size() == 0)
			return;
		currentPage++;
		if (currentPage > maxPages)
			currentPage = 1;
		open(currentPage);
	}

	protected void openPrevPage() {
		if (filteredRecipes.size() == 0)
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

		List<IRecipe> viewing = filteredRecipes.subList((page - 1) * 45, Math.min(page * 45, filteredRecipes.size()));
		int i = 0;
		for (IRecipe recipe : viewing) {
			addInventoryItem(new InventoryItem(this,
					ItemStackUtils.setDisplayname(recipe.getResult(),
							pl.sortRecipesByRecipeName() ? recipe.getRecipeName() : recipe.getResultName()),
					i, (slot, b, c, top) -> Optional.ofNullable(top ? this : null)
							.ifPresent(self -> onRecipeClick(filteredRecipes.get((page - 1) * 45 + slot)))));
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
