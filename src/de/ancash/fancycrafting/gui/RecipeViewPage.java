package de.ancash.fancycrafting.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.ancash.fancycrafting.utils.IRecipe;

public class RecipeViewPage {

	private static final ItemStack next = new ItemStack(Material.ARROW);
	private static final ItemStack back = new ItemStack(Material.ARROW);
	private static final ItemStack background = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
	
	static {
		ItemMeta im = next.getItemMeta();
		im.setDisplayName("§aNext");
		next.setItemMeta(im);
		im = back.getItemMeta();
		im.setDisplayName("§aBack");
		back.setItemMeta(im);
		im = background.getItemMeta();
		im.setDisplayName(" ");
		background.setItemMeta(im);
	}
	
	private IRecipe[][] recipesPerPage;
	private int space;
	private int currentPage = 1;
	
	public RecipeViewPage(IRecipe[] allRecipes, int inventorySize) {
		this.space = inventorySize;
		update(allRecipes);
	}
	
	public void update(IRecipe[] recipes) {
		setRecipesPerPage(recipes.length, space);
		int page = 1;
		int slot = 1;;
		for(int r = 0; r<recipes.length; r++) {
			if(slot > space) {
				page++;
				slot = 0;
			}
			recipesPerPage[page - 1][slot - 1] = recipes[r]; 
			slot++;
		}
	}

	private void setRecipesPerPage(int recipes, int inventorySize) {
		int pages = 0;
		pages = (int) (1D + (((double) recipes + 1) / (double) inventorySize- (1D / (double) inventorySize)));
		recipesPerPage = new IRecipe[pages][inventorySize];
	}
	
	public ItemStack[] getNextPage() {
		currentPage++;
		if(currentPage > recipesPerPage.length) 
			currentPage = 1;
		return toItems(recipesPerPage[currentPage - 1]);
	}
	
	public ItemStack[] getPreviousPage() {
		currentPage--;
		if(currentPage <= 0) {
			currentPage = recipesPerPage.length;
		}
		return toItems(recipesPerPage[currentPage - 1]);
	}
	
	public ItemStack[] getLastPage() {
		return toItems(recipesPerPage[recipesPerPage.length - 1]);
	}

	private ItemStack[] toItems(IRecipe[] recipes) {
		ItemStack[] items = new ItemStack[space];
		for(int i = 0; i<9; i++) items[i] = background;
		items[3] = back;
		items[5] = next;
		for(int i = 9; i<recipes.length + 9; i++) {
			if(recipes[i - 9] == null) break;
			items[i] = recipes[i - 9].getResult();
		}
		return items;
	}
	
	public ItemStack[] getFirstPage() {
		return toItems(recipesPerPage[0]);
	}
}
