package de.ancash.fancycrafting.recipe;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.simpleyaml.configuration.file.YamlFile;

import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.nbtnexus.serde.SerializedItem;

public class IRandomShapedRecipe extends IShapedRecipe implements IRandomRecipe {

	private final List<Duplet<SerializedItem, Integer>> probList;
	private final int probSum;
	private final Random random = new Random();
	private final Map<ItemStack, Integer> probMap;

	public IRandomShapedRecipe(ItemStack[] ings, int width, int height, ItemStack result, String name, UUID uuid,
			Map<ItemStack, Integer> probMap, RecipeCategory category) {
		super(ings, width, height, result, name, uuid, category);
		probList = Collections.unmodifiableList(
				probMap.entrySet().stream().map(entry -> Tuple.of(SerializedItem.of(entry.getKey()), entry.getValue()))
						.collect(Collectors.toList()));
		probSum = probList.stream().map(Duplet::getSecond).mapToInt(Integer::valueOf).sum();
		this.probMap = probMap;
	}

	@Override
	public List<Duplet<SerializedItem, Integer>> getProbabilityList() {
		return probList;
	}

	@Override
	public boolean isShiftCollectable() {
		return false;
	}

	@Override
	public int getProbabilitySum() {
		return probSum;
	}

	@Override
	public void saveToFile(FileConfiguration file, String path) {
		super.saveToFile(file, path);
		file.set(path + ".random", true); //$NON-NLS-1$
		file.set(path + ".random-map", null); //$NON-NLS-1$
		for (int i = 0; i < probList.size(); i++) {
			Duplet<SerializedItem, Integer> d = probList.get(i);
			ItemStackUtils.setItemStack(file, path + ".random-map." + i + ".item", d.getFirst().toItem()); //$NON-NLS-1$ //$NON-NLS-2$
			file.set(path + ".random-map." + i + ".prob", d.getSecond()); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	@SuppressWarnings("nls")
	@Override
	public String saveToString() throws IOException {
		YamlFile temp = new YamlFile();
		temp.set("recipe.name", recipeName);
		temp.set("recipe.result", ItemStackUtils.itemStackToString(result.toItem()));
		temp.set("recipe.shaped", true);
		temp.set("recipe.width", getWidth());
		temp.set("recipe.height", getHeight());
		temp.set("recipe.uuid", uuid.toString());
		temp.set("recipe.random", true);
		temp.set("recipe.category", category.getName());
		for (int i = 0; i < getIngredientsArray().length; i++)
			if (getIngredientsArray()[i] != null)
				temp.set("recipe.ingredients." + i,
						ItemStackUtils.itemStackToString(getIngredientsArray()[i].toItem()));
		for (int i = 0; i < probList.size(); i++) {
			Duplet<SerializedItem, Integer> d = probList.get(i);
			temp.set("recipe.random-map." + i + ".item", ItemStackUtils.itemStackToString(d.getFirst().toItem())); //$NON-NLS-1$ //$NON-NLS-2$
			temp.set("recipe.random-map." + i + ".prob", d.getSecond()); //$NON-NLS-1$//$NON-NLS-2$
		}
		return temp.saveToString();
	}

	@Override
	public SerializedItem getRandom() {
		int r = random.nextInt(probSum) + 1;
		Iterator<Duplet<SerializedItem, Integer>> iter = probList.iterator();
		Duplet<SerializedItem, Integer> curr;
		while (iter.hasNext()) {
			curr = iter.next();
			r -= curr.getSecond();
			if (r <= 0)
				return curr.getFirst();
		}
		throw new IllegalStateException("Invalid probability"); //$NON-NLS-1$
	}

	@Override
	public Map<ItemStack, Integer> getProbabilityMap() {
		return probMap;
	}
}