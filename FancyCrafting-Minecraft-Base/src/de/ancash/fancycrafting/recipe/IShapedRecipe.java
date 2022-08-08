package de.ancash.fancycrafting.recipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import de.ancash.libs.org.simpleyaml.configuration.file.YamlFile;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.ItemStackUtils;

public class IShapedRecipe extends IRecipe {

	private final IMatrix<IItemStack> matrix;
	private final int size;
	private final Map<Integer, Integer> hashCodes = new HashMap<>();
	private final List<Integer> hashMatrix = new ArrayList<>();

	public IShapedRecipe(ItemStack[] ings, int width, int height, ItemStack result, String name, UUID uuid) {
		super(result, name, uuid);
		this.matrix = new IMatrix<>(toIItemStackArray(ings), width, height);
		matrix.optimize();
		this.size = (int) Arrays.asList(ings).stream().filter(i -> i != null).count();
		addHashs();
	}

	public IShapedRecipe(ItemStack[] ings, int width, int height, ItemStack result, String name, boolean vanilla,
			boolean suitableForAutoMatching) {
		super(result, name, vanilla, suitableForAutoMatching);
		this.matrix = new IMatrix<>(toIItemStackArray(ings), width, height);
		matrix.optimize();
		this.size = (int) Arrays.asList(ings).stream().filter(i -> i != null).count();
		addHashs();
	}

	private void addHashs() {
		for (int i = 0; i < matrix.getArray().length; i++)
			hashMatrix.add(matrix.getArray()[i] == null ? null : matrix.getArray()[i].hashCode());
		for (IItemStack ii : matrix.getArray()) {
			if (ii == null)
				continue;
			hashCodes.computeIfAbsent(ii.hashCode(), key -> 0);
			hashCodes.put(ii.hashCode(), hashCodes.get(ii.hashCode()) + ii.getOriginal().getAmount());
		}
	}

	private static IItemStack[] toIItemStackArray(ItemStack[] from) {
		return Arrays.asList(from).stream().map(item -> item == null ? null : new IItemStack(item))
				.toArray(IItemStack[]::new);
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
		return Arrays.asList(matrix.getArray()).stream().map(i -> i == null ? null : i.getOriginal())
				.collect(Collectors.toList());
	}

	@Override
	public List<IItemStack> getIIngredients() {
		return Arrays.asList(matrix.getArray());
	}

	@Override
	public Map<Integer, Integer> getIngredientsHashCodes() {
		return Collections.unmodifiableMap(hashCodes);
	}

	@Override
	public boolean isShiftCollectable() {
		return true;
	}

	@SuppressWarnings("nls")
	@Override
	public void saveToFile(FileConfiguration file, String path) {
		file.set(path, null);
		file.set(path + ".name", recipeName);
		if (result != null)
			ItemStackUtils.setItemStack(file, uuid + ".result", result.getOriginal());
		file.set(path + ".shaped", true);
		file.set(path + ".width", matrix.getWidth());
		file.set(path + ".height", matrix.getHeight());
		file.set(path + ".uuid", uuid.toString());
		file.set(path + ".random", false);
		for (int i = 0; i < getIngredientsArray().length; i++)
			if (getIngredientsArray()[i] != null)
				ItemStackUtils.setItemStack(file, path + ".ingredients." + i, getIngredientsArray()[i].getOriginal());
	}

	@SuppressWarnings("nls")
	@Override
	public String saveToString() throws IOException {
		YamlFile temp = new YamlFile();
		temp.set("recipe.name", recipeName);
		temp.set("recipe.result", ItemStackUtils.itemStackToString(result.getOriginal()));
		temp.set("recipe.shaped", true);
		temp.set("recipe.width", matrix.getWidth());
		temp.set("recipe.height", matrix.getHeight());
		temp.set("recipe.uuid", uuid.toString());
		temp.set("recipe.random", false);
		for (int i = 0; i < getIngredientsArray().length; i++)
			if (getIngredientsArray()[i] != null)
				temp.set("recipe.ingredients." + i,
						ItemStackUtils.itemStackToString(getIngredientsArray()[i].getOriginal()));
		return temp.saveToString();
	}

	@Override
	public List<Integer> getHashMatrix() {
		return hashMatrix;
	}
}