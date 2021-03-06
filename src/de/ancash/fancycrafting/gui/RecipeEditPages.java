package de.ancash.fancycrafting.gui;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.nbt.NBTItem;

public class RecipeEditPages {

	private static final ItemStack next = XMaterial.ARROW.parseItem().clone();
	private static final ItemStack back = XMaterial.ARROW.parseItem().clone();
	private static final ItemStack background = XMaterial.GRAY_STAINED_GLASS_PANE.parseItem().clone();
	
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
	
	public RecipeEditPages(IRecipe[] allRecipes, int inventorySize) {
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
			ItemStack wId = recipes[i - 9].getResult().clone();
			ItemMeta im = wId.getItemMeta();
			im.setDisplayName(recipes[i - 9].getName().replace("-", " "));
			wId.setItemMeta(im);
			NBTItem nbt = new NBTItem(wId);
			nbt.setString("fancycrafting.id", recipes[i - 9].getName().replace("-", " "));
			items[i] = nbt.getItem();
		}
		return items;
	}
	
	public ItemStack[] getFirstPage() {
		return toItems(recipesPerPage[0]);
	}
}
