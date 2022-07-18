package de.ancash.fancycrafting.recipe;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.ItemStackUtils;

public class IRandomShapedRecipe extends IShapedRecipe implements IRandomRecipe {

	private final List<Duplet<IItemStack, Integer>> probList;
	private final int probSum;
	private final Random random = new Random();
	private final Map<ItemStack, Integer> probMap;

	public IRandomShapedRecipe(ItemStack[] ings, int width, int height, ItemStack result, String name, UUID uuid,
			Map<ItemStack, Integer> probMap) {
		super(ings, width, height, result, name, uuid);
		probList = Collections.unmodifiableList(probMap.entrySet().stream()
				.map(entry -> Tuple.of(new IItemStack(entry.getKey()), entry.getValue())).collect(Collectors.toList()));
		probSum = probList.stream().map(Duplet::getSecond).mapToInt(Integer::valueOf).sum();
		this.probMap = probMap;
	}

	@Override
	public List<Duplet<IItemStack, Integer>> getProbabilityList() {
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
			Duplet<IItemStack, Integer> d = probList.get(i);
			ItemStackUtils.setItemStack(file, path + ".random-map." + i + ".item", d.getFirst().getOriginal()); //$NON-NLS-1$ //$NON-NLS-2$
			file.set(path + ".random-map." + i + ".prob", d.getSecond());  //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	@Override
	public IItemStack getRandom() {
		int r = random.nextInt(probSum);
		Iterator<Duplet<IItemStack, Integer>> iter = probList.iterator();
		Duplet<IItemStack, Integer> duplet;
		while (iter.hasNext()) {
			duplet = iter.next();
			r -= duplet.getSecond();
			if (r <= 0)
				return duplet.getFirst();
		}
		throw new IllegalStateException("Invalid probability"); //$NON-NLS-1$
	}

	@Override
	public Map<ItemStack, Integer> getProbabilityMap() {
		return probMap;
	}
}