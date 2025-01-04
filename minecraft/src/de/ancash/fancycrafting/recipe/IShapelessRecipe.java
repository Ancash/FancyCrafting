package de.ancash.fancycrafting.recipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.simpleyaml.configuration.file.YamlFile;

import de.ancash.minecraft.ItemStackUtils;
import de.ancash.nbtnexus.serde.SerializedItem;
import de.ancash.nbtnexus.serde.access.SerializedMetaAccess;

public class IShapelessRecipe extends IRecipe {

	private final List<ItemStack> ings;
	private final List<SerializedItem> iings;
	private final Map<Integer, Integer> hashCodes = new HashMap<>();
	private final List<Integer> hashMatrix;
	protected final int size;

	public IShapelessRecipe(Collection<ItemStack> ings, ItemStack result, String name, UUID uuid,
			RecipeCategory category) {
		this(ings, result, name, false, true, uuid, category);
	}

	public IShapelessRecipe(Collection<ItemStack> ings, ItemStack result, String name, boolean vanilla,
			boolean suitableForAutoMatching, RecipeCategory category) {
		this(ings, result, name, vanilla, suitableForAutoMatching, null, category);
	}

	IShapelessRecipe(Collection<ItemStack> ings, ItemStack result, String name, boolean vanilla,
			boolean suitableForAutoMatching, UUID uuid, RecipeCategory category) {
		super(result, name, vanilla, suitableForAutoMatching, uuid, category);
		this.ings = Collections.unmodifiableList(ings.stream().filter(i -> i != null && i.getType() != Material.AIR)
				.collect(Collectors.toCollection(() -> new ArrayList<>())));
		this.iings = Collections
				.unmodifiableList(this.ings.stream().map(SerializedItem::of).collect(Collectors.toList()));
		for (SerializedItem ii : iings) {
			hashCodes.computeIfAbsent(ii.hashCode(), key -> 0);
			hashCodes.put(ii.hashCode(),
					hashCodes.get(ii.hashCode()) + SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(ii.getMap()));
		}
		this.size = (int) Math.ceil(Math.sqrt(this.ings.size()));
		hashMatrix = iings.stream().map(SerializedItem::hashCode).sorted().collect(Collectors.toList());
	}

	@Override
	public List<ItemStack> getIngredients() {
		return ings.stream().map(ItemStack::clone).collect(Collectors.toList());
	}

	@Override
	public List<SerializedItem> getSerializedIngredients() {
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
	public boolean matches(IMatrix<SerializedItem> m) {
		List<SerializedItem> input = Stream.of(m.getArray()).filter(i -> i != null).collect(Collectors.toList());
		List<Integer> hashMatrix = input.stream().map(SerializedItem::hashCode).sorted().collect(Collectors.toList());
		if (!hashMatrix.equals(this.hashMatrix))
			return false;
		if (isVanilla())
			return true;

		List<SerializedItem> compareTo = new ArrayList<>();
		compareTo.addAll(iings);

		Collections.sort(compareTo,
				(a, b) -> Integer.valueOf(SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(a.getMap()))
						.compareTo(SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(b.getMap())));
		Collections.sort(input,
				(a, b) -> Integer.valueOf(SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(a.getMap()))
						.compareTo(SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(b.getMap())));

		for (SerializedItem si : compareTo) {
			Iterator<SerializedItem> iter = input.iterator();
			boolean match = false;
			while (iter.hasNext()) {
				SerializedItem comp = iter.next();
				if (si.areEqualIgnoreAmount(comp) && SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(
						si.getMap()) <= SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(comp.getMap())) {
					match = true;
					iter.remove();
					break;
				}
			}
			if (!match)
				return false;
		}
		return input.isEmpty();
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
		file.set(path + ".shaped", false);
		file.set(path + ".width", size);
		file.set(path + ".height", size);
		file.set(path + ".uuid", uuid.toString());
		file.set(path + ".random", false);
		file.set(path + ".category", category.getName());
		for (int i = 0; i < getIngredients().size(); i++)
			if (getIngredients().get(i) != null)
				ItemStackUtils.setItemStack(file, path + ".ingredients." + i, getIngredients().get(i));
	}

	@Override
	public String saveToString() throws IOException {
		YamlFile temp = new YamlFile();
		temp.set("recipe.name", recipeName);
		temp.set("recipe.result", result.toYaml());
		temp.set("recipe.shaped", true);
		temp.set("recipe.width", size);
		temp.set("recipe.height", size);
		temp.set("recipe.uuid", uuid.toString());
		temp.set("recipe.random", false);
		temp.set("recipe.category", category.getName());
		for (int i = 0; i < getSerializedIngredients().size(); i++)
			temp.set("recipe.ingredients." + i,
					getSerializedIngredients().get(i).toYaml());
		return temp.saveToString();
	}

	@Override
	public List<Integer> getHashMatrix() {
		return hashMatrix;
	}
}