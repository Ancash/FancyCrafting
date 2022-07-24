package de.ancash.fancycrafting.recipe;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.ItemStackUtils;

public class IShapelessRecipe extends IRecipe {

	private final List<ItemStack> ings;
	private final List<IItemStack> iings;
	private final Map<Integer, Integer> hashCodes = new HashMap<>();
	private final int size;

	public IShapelessRecipe(Collection<ItemStack> ings, ItemStack result, String name, UUID uuid) {
		this(ings, result, name, false, true, uuid);
	}

	public IShapelessRecipe(Collection<ItemStack> ings, ItemStack result, String name, boolean vanilla,
			boolean suitableForAutoMatching) {
		this(ings, result, name, vanilla, suitableForAutoMatching, null);
	}

	IShapelessRecipe(Collection<ItemStack> ings, ItemStack result, String name, boolean vanilla,
			boolean suitableForAutoMatching, UUID uuid) {
		super(result, name, vanilla, suitableForAutoMatching, uuid);
		this.ings = Collections.unmodifiableList(ings.stream().filter(i -> i != null).collect(Collectors.toList()));
		this.iings = Collections.unmodifiableList(this.ings.stream().map(IItemStack::new).collect(Collectors.toList()));
		for (IItemStack ii : iings) {
			hashCodes.computeIfAbsent(ii.hashCode(), key -> 0);
			hashCodes.put(ii.hashCode(), hashCodes.get(ii.hashCode()) + ii.getOriginal().getAmount());
		}
		this.size = (int) Math.ceil(Math.sqrt(this.ings.size()));
	}

	@Override
	public List<ItemStack> getIngredients() {
		return ings;
	}

	@Override
	public List<IItemStack> getIIngredients() {
		return iings;
	}

	@Override
	public int getIngredientsSize() {
		return ings.size();
	}

	@Override
	public int getHeight() {
		return size;
	}

	@Override
	public int getWidth() {
		return size;
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
		ItemStackUtils.setItemStack(file, uuid + ".result", result.getOriginal());
		file.set(path + ".shaped", false);
		file.set(path + ".width", size);
		file.set(path + ".height", size);
		file.set(path + ".uuid", uuid.toString());
		file.set(path + ".random", false);
		for (int i = 0; i < getIngredients().size(); i++)
			if (getIngredients().get(i) != null)
				ItemStackUtils.setItemStack(file, path + ".ingredients." + i, getIngredients().get(i));
	}
}