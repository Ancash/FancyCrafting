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

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.simpleyaml.configuration.file.YamlFile;

import de.ancash.minecraft.ItemStackUtils;
import de.ancash.nbtnexus.serde.SerializedItem;
import de.ancash.nbtnexus.serde.access.SerializedMetaAccess;

public class IShapedRecipe extends IRecipe {

	private final IMatrix<SerializedItem> matrix;
	private final Map<Integer, Integer> hashCodes = new HashMap<>();
	private final List<Integer> hashMatrix = new ArrayList<>();

	public IShapedRecipe(ItemStack[] ings, int width, int height, ItemStack result, String name, UUID uuid,
			RecipeCategory category) {
		super(result, name, uuid, category);
		this.matrix = new IMatrix<>(toSerializedItemArray(ings), width, height);
		matrix.optimize();
		calcHashs();
	}

	public IShapedRecipe(ItemStack[] ings, int width, int height, ItemStack result, String name, boolean vanilla,
			boolean suitableForAutoMatching, RecipeCategory category) {
		super(result, name, vanilla, suitableForAutoMatching, category);
		this.matrix = new IMatrix<>(toSerializedItemArray(ings), width, height);
		matrix.optimize();
		calcHashs();
	}

	private void calcHashs() {
		for (int i = 0; i < matrix.getArray().length; i++)
			hashMatrix.add(matrix.getArray()[i] == null ? null : matrix.getArray()[i].hashCode());

		for (SerializedItem ii : matrix.getArray()) {
			if (ii == null)
				continue;
			hashCodes.computeIfAbsent(ii.hashCode(), key -> 0);
			hashCodes.put(ii.hashCode(),
					hashCodes.get(ii.hashCode()) + SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(ii.getMap()));
		}
	}

	private static SerializedItem[] toSerializedItemArray(ItemStack[] from) {
		return Arrays.asList(from).stream()
				.map(item -> item == null || item.getType() == Material.AIR ? null : SerializedItem.of(item))
				.toArray(SerializedItem[]::new);
	}

	public SerializedItem[] getIngredientsArray() {
		return matrix.getArray();
	}

	public SerializedItem[] asArray() {
		return matrix.getArray();
	}

	public SerializedItem[] getInMatrix(int width, int height) {
		matrix.cut(width, height);
		SerializedItem[] temp = matrix.getArray();
		matrix.optimize();
		return temp;
	}

	public int getWidth() {
		return matrix.getWidth();
	}

	public int getHeight() {
		return matrix.getHeight();
	}

	@Override
	public boolean matches(IMatrix<SerializedItem> m) {
		m = m.clone();
		m.cut(getWidth(), getHeight());
		if (m.getHeight() != getHeight() || m.getWidth() != getWidth())
			return false;
		for (int i = 0; i < m.getArray().length; i++) {
			if ((m.getArray()[i] == null) != (matrix.getArray()[i] == null))
				return false;
			if (m.getArray()[i] == null)
				continue;
			if (m.getArray()[i].hashCode() != matrix.getArray()[i].hashCode())
				return false;
			if (SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(m.getArray()[i]
					.getMap()) < SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(matrix.getArray()[i].getMap()))
				return false;
		}
		return true;
	}

	@Override
	public List<ItemStack> getIngredients() {
		return Arrays.asList(matrix.getArray()).stream().map(i -> i == null ? null : i.toItem())
				.collect(Collectors.toList());
	}

	@Override
	public List<SerializedItem> getSerializedIngredients() {
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
			ItemStackUtils.setItemStack(file, uuid + ".result", result.toItem());
		file.set(path + ".shaped", true);
		file.set(path + ".width", matrix.getWidth());
		file.set(path + ".height", matrix.getHeight());
		file.set(path + ".uuid", uuid.toString());
		file.set(path + ".random", false);
		file.set(path + ".category", category.getName());
		for (int i = 0; i < getIngredientsArray().length; i++)
			if (getIngredientsArray()[i] != null)
				ItemStackUtils.setItemStack(file, path + ".ingredients." + i, getIngredientsArray()[i].toItem());
	}

	@Override
	public String saveToString() throws IOException {
		YamlFile temp = new YamlFile();
		temp.set("recipe.name", recipeName);
		temp.set("recipe.result", result.toYaml());
		temp.set("recipe.shaped", true);
		temp.set("recipe.width", matrix.getWidth());
		temp.set("recipe.height", matrix.getHeight());
		temp.set("recipe.uuid", uuid.toString());
		temp.set("recipe.random", false);
		temp.set("recipe.category", category.getName());
		for (int i = 0; i < getIngredientsArray().length; i++)
			if (getIngredientsArray()[i] != null)
				temp.set("recipe.ingredients." + i,
						getIngredientsArray()[i].toYaml());
		return temp.saveToString();
	}

	@Override
	public List<Integer> getHashMatrix() {
		return hashMatrix;
	}
}