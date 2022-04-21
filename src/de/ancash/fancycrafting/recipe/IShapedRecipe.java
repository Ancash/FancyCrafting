package de.ancash.fancycrafting.recipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public class IShapedRecipe extends IRecipe{

	private final IMatrix<ItemStack> matrix;
	private final int size;
	
	public IShapedRecipe(ItemStack[] ings, int width, int height, ItemStack result, String name, UUID uuid) {
		super(result, name, uuid);
		this.matrix = new IMatrix<>(ings, width, height);
		matrix.optimize();
		this.size = (int) Arrays.asList(ings).stream().filter(i -> i != null).count();
	}

	public IShapedRecipe(ItemStack[] ings, int width, int height, ItemStack result, String name, boolean vanilla) {
		super(result, name, vanilla);
		this.matrix = new IMatrix<>(ings, width, height);
		matrix.optimize();
		this.size = (int) Arrays.asList(matrix.getArray()).stream().filter(i -> i != null).count();
	}

	public List<ItemStack> getIngredients() {
		return Collections.unmodifiableList(Arrays.asList(matrix.getArray()));
	}
	
	public ItemStack[] getIngredientsArray() {
		return matrix.getArray();
	}

	public ItemStack[] getInMatrix(int width, int height) {
		matrix.cut(width, height);
		ItemStack[] temp = matrix.getArray();
		matrix.optimize();
		return temp;
	}
	
	@Override
	public int getIngredientsSize() {
		return size;
	}

	public int getWidth() {
		return matrix.getWidth();
	}
	
	public int getHeight() {
		return matrix.getHeight();
	}
}