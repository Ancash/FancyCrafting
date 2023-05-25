package de.ancash.fancycrafting.recipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import org.simpleyaml.configuration.file.YamlFile;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.ItemStackUtils;

public class IShapelessRecipe extends IRecipe {

	private final List<ItemStack> ings;
	private final List<IItemStack> iings;
	private final Map<Integer, Integer> hashCodes = new HashMap<>();
	private final List<Integer> hashMatrix;
	protected final int size;

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
		this.ings = Collections.unmodifiableList(ings.stream().filter(i -> i != null && i.getType() != Material.AIR)
				.collect(Collectors.toCollection(() -> new ArrayList<>())));
		this.iings = Collections.unmodifiableList(this.ings.stream().map(IItemStack::new).collect(Collectors.toList()));
		for (IItemStack ii : iings) {
			hashCodes.computeIfAbsent(ii.hashCode(), key -> 0);
			hashCodes.put(ii.hashCode(), hashCodes.get(ii.hashCode()) + ii.getOriginal().getAmount());
		}
		this.size = (int) Math.ceil(Math.sqrt(this.ings.size()));
		hashMatrix = iings.stream().map(IItemStack::hashCode).sorted().collect(Collectors.toList());
	}

	@Override
	public List<ItemStack> getIngredients() {
		return ings.stream().map(ItemStack::clone).collect(Collectors.toList());
	}

	@Override
	public List<IItemStack> getIIngredients() {
		return iings;
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
	public boolean matches(IMatrix<IItemStack> m) {
		List<IItemStack> items = Stream.of(m.getArray()).filter(i -> i != null).collect(Collectors.toList());
		List<Integer> hashMatrix = items.stream().map(IItemStack::hashCode).sorted().collect(Collectors.toList());
		if (!hashMatrix.equals(this.hashMatrix))
			return false;
		if (isVanilla())
			return true;

		Set<Integer> done = new HashSet<>();

		for (int a = 0; a < items.size(); a++) {
			for (int b = 0; b < items.size(); b++) {
				if (done.contains(b))
					continue;
				if (items.get(a).hashCode() != iings.get(b).hashCode())
					continue;
				if (items.get(a).getOriginal().getAmount() < iings.get(b).getOriginal().getAmount())
					continue;
				done.add(b);
				break;
			}
		}

		return done.size() == items.size();
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
		file.set(path + ".shaped", false);
		file.set(path + ".width", size);
		file.set(path + ".height", size);
		file.set(path + ".uuid", uuid.toString());
		file.set(path + ".random", false);
		for (int i = 0; i < getIngredients().size(); i++)
			if (getIngredients().get(i) != null)
				ItemStackUtils.setItemStack(file, path + ".ingredients." + i, getIngredients().get(i));
	}

	@SuppressWarnings("nls")
	@Override
	public String saveToString() throws IOException {
		YamlFile temp = new YamlFile();
		temp.set("recipe.name", recipeName);
		temp.set("recipe.result", ItemStackUtils.itemStackToString(result.getOriginal()));
		temp.set("recipe.shaped", true);
		temp.set("recipe.width", size);
		temp.set("recipe.height", size);
		temp.set("recipe.uuid", uuid.toString());
		temp.set("recipe.random", false);
		for (int i = 0; i < getIIngredients().size(); i++)
			temp.set("recipe.ingredients." + i,
					ItemStackUtils.itemStackToString(getIIngredients().get(i).getOriginal()));
		return temp.saveToString();
	}

	@Override
	public List<Integer> getHashMatrix() {
		return hashMatrix;
	}
}