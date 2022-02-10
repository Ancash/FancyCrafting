package de.ancash.fancycrafting.recipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public class IShapedRecipe extends IRecipe{

	private final ItemStack[] cutIngs;
	private final int size;
	private final int matrix;
	
	public IShapedRecipe(ItemStack[] ings, ItemStack result, String name, UUID uuid) {
		super(result, name, uuid);
		IMatrix.optimize(ings);
		this.cutIngs = IMatrix.cutMatrix(ings, 1);
		this.size = (int) Arrays.asList(ings).stream().filter(i -> i != null).count();
		this.matrix = (int) Math.sqrt(cutIngs.length);
	}

	public IShapedRecipe(ItemStack[] ings, ItemStack result, String name, boolean vanilla) {
		super(result, name, vanilla);
		IMatrix.optimize(ings);
		this.cutIngs = IMatrix.cutMatrix(ings, 1);
		this.size = (int) Arrays.asList(ings).stream().filter(i -> i != null).count();
		this.matrix = (int) Math.sqrt(cutIngs.length);
	}

	public List<ItemStack> getIngredients() {
		return Collections.unmodifiableList(Arrays.asList(cutIngs));
	}
	
	public ItemStack[] getIngredientsArray() {
		return cutIngs;
	}

	public ItemStack[] getInMatrix(int m) {
		return IMatrix.cutMatrix(cutIngs, m);
	}
	
	@Override
	public int getIngredientsSize() {
		return size;
	}

	public int getMatrix() {
		return matrix;
	}
}