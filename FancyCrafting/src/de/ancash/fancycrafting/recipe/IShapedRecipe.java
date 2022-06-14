package de.ancash.fancycrafting.recipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;

import de.ancash.minecraft.IItemStack;

public class IShapedRecipe extends IRecipe {

	private final IMatrix<IItemStack> matrix;
	private final int size;
	private final Map<Integer, Integer> hashCodes = new HashMap<>();
	
	public IShapedRecipe(ItemStack[] ings, int width, int height, ItemStack result, String name, UUID uuid) {
		super(result, name, uuid);
		this.matrix = new IMatrix<>(toIItemStackArray(ings), width, height);
		matrix.optimize();
		this.size = (int) Arrays.asList(ings).stream().filter(i -> i != null).count();
		for(IItemStack ii : matrix.getArray()) {
			if(ii == null)
				continue;
			hashCodes.computeIfAbsent(ii.hashCode(), key -> 0);
			hashCodes.put(ii.hashCode(), hashCodes.get(ii.hashCode()) + ii.getOriginal().getAmount());
		}
	}

	public IShapedRecipe(ItemStack[] ings, int width, int height, ItemStack result, String name, boolean vanilla, boolean suitableForAutoMatching) {
		super(result, name, vanilla, suitableForAutoMatching);
		this.matrix = new IMatrix<>(toIItemStackArray(ings), width, height);
		matrix.optimize();
		this.size = (int) Arrays.asList(ings).stream().filter(i -> i != null).count();
		for(IItemStack ii : matrix.getArray()) {
			if(ii == null)
				continue;
			hashCodes.computeIfAbsent(ii.hashCode(), key -> 0);
			hashCodes.put(ii.hashCode(), hashCodes.get(ii.hashCode()) + ii.getOriginal().getAmount());
		}
	}

	public IItemStack[] getIngredientsArray() {
		return matrix.getArray();
	}
	
	public IItemStack[] asArray() {
		return matrix.getArray();
	}

	public IItemStack[] getInMatrix(int width, int height) {
		matrix.cut(width, height);
		IItemStack[] temp = matrix.getArray();
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

	@Override
	public List<ItemStack> getIngredients() {
		return Arrays.asList(matrix.getArray()).stream().map(i -> i == null ? null : i.getOriginal()).collect(Collectors.toList());
	}

	@Override
	public Map<Integer, Integer> getIngredientsHashCodes() {
		return Collections.unmodifiableMap(hashCodes);
	}
}