package de.ancash.fancycrafting.recipe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.libs.org.simpleyaml.configuration.file.YamlFile;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.XMaterial;

public abstract class IRecipe {

	private static final char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWYabcdefghijklmnopqrstuvw".toCharArray(); //$NON-NLS-1$

	protected final IItemStack result;
	protected final String recipeName;
	protected final String resultName;
	protected final boolean vanilla;
	protected final boolean suitableForAutoMatching;
	protected final UUID uuid;
	protected final Permission craftPermission;
	protected final Permission viewPermission;

	public IRecipe(ItemStack result, String name, UUID uuid) {
		this(result, name, false, true, uuid);
	}

	public IRecipe(ItemStack result, String name, boolean vanilla, boolean suitableForAutoMatching) {
		this(result, name, vanilla, suitableForAutoMatching, null);
	}

	@SuppressWarnings("nls")
	IRecipe(ItemStack result, String name, boolean vanilla, boolean suitableForAutoMatching, UUID uuid) {
		this.uuid = uuid;
		this.result = result == null ? null : new IItemStack(result);
		this.vanilla = vanilla;
		if (vanilla)
			this.suitableForAutoMatching = suitableForAutoMatching;
		else
			this.suitableForAutoMatching = true;
		if (result != null) {
			if (vanilla)
				this.recipeName = XMaterial.matchXMaterial(result).toString();
			else
				this.recipeName = name;
			resultName = result.getItemMeta().getDisplayName() == null
					|| result.getItemMeta().getDisplayName().isEmpty() ? XMaterial.matchXMaterial(result).toString()
							: result.getItemMeta().getDisplayName();
			this.craftPermission = new Permission("fancycrafting.craft." + this.recipeName.replace(" ", "-"), //$NON-NLS-3$
					PermissionDefault.FALSE);
			this.viewPermission = new Permission("fancycrafting.view." + this.recipeName.replace(" ", "-"), //$NON-NLS-3$
					PermissionDefault.FALSE);
		} else {
			this.recipeName = name == null ? "null" : name;
			this.resultName = "null";
			this.craftPermission = null;
			this.viewPermission = null;
		}
	}

	public abstract String saveToString() throws IOException;
	
	public abstract void saveToFile(FileConfiguration file, String path);

	public abstract int getHeight();

	public abstract int getWidth();

	public abstract int getIngredientsSize();

	public abstract Map<Integer, Integer> getIngredientsHashCodes();

	public abstract List<ItemStack> getIngredients();

	public abstract List<IItemStack> getIIngredients();

	public abstract boolean isShiftCollectable();

	public ItemStack getResult() {
		return result == null ? null : result.getOriginal().clone();
	}

	public IItemStack getResultAsIItemStack() {
		return result;
	}

	public boolean isVanilla() {
		return vanilla;
	}

	public String getRecipeName() {
		return recipeName;
	}

	public UUID getUUID() {
		return uuid;
	}

	public boolean isSuitableForAutoMatching() {
		return suitableForAutoMatching;
	}

	public static boolean matchesShaped(IShapedRecipe recipe, IItemStack[] ings, int width, int height) {
		if (recipe.getWidth() != width || recipe.getHeight() != height)
			return false;
		for (int i = 0; i < ings.length; i++) {
			if ((recipe.asArray()[i] == null) != (ings[i] == null))
				return false;
			if (ings[i] == null)
				continue;
			if (recipe.asArray()[i].hashCode() != ings[i].hashCode()
					|| ings[i].getOriginal().getAmount() < recipe.getIngredientsArray()[i].getOriginal().getAmount())
				return false;
		}
		return true;
	}

	public static boolean matchesShapeless(IShapelessRecipe recipe, IItemStack[] ingredients) {
		List<IItemStack> origs = recipe.getIIngredients();
		List<IItemStack> ings = Arrays.asList(ingredients).stream().filter(i -> i != null).collect(Collectors.toList());

		if (ings.size() != origs.size())
			return false;

		for (int i1 = 0; i1 < origs.size(); i1++) {
			boolean matches = false;
			for (int i2 = 0; i2 < ings.size(); i2++) {
				if (origs.get(i1).hashCode() == ings.get(i2).hashCode()
						&& origs.get(i1).getOriginal().getAmount() <= ings.get(i2).getOriginal().getAmount()) {
					matches = true;
					ings.remove(i2);
					break;
				}
			}
			if (!matches)
				return false;
		}
		return true;
	}

	public static IRecipe getRecipeFromFile(File file, String path) throws IOException, InvalidConfigurationException {
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		fc.load(file);
		IRecipe r = getRecipeFromFile(file, fc, path);
		fc.save(file);
		return r;
	}
	
	@SuppressWarnings("nls")
	public static IRecipe getRecipeFromString(String string) throws IOException, InvalidConfigurationException {
		YamlFile file = YamlFile.loadConfigurationFromString(string);
		ItemStack result = ItemStackUtils.itemStackFromString(file.getString("recipe.result"));
		String name = file.getString("recipe.name");

		int width = file.getInt("recipe.width");
		int height = file.getInt("recipe.height");

		ItemStack[] ingredients = new ItemStack[width * height];
		for (int i = 0; i < ingredients.length; i++)
			if (file.getString("recipe.ingredients." + i) != null)
				ingredients[i] = ItemStackUtils.itemStackFromString(file.getString("recipe.ingredients." + i));

		boolean random = file.getBoolean("recipe.random");

		UUID uuid = UUID.fromString(file.getString("recipe.uuid"));

		Map<ItemStack, Integer> rngMap = new HashMap<>();

		if (random)
			for(String key : file.getConfigurationSection("recipe.random-map").getKeys(false))
				rngMap.put(ItemStackUtils.itemStackFromString(file.getString("recipe.random-map." + key + ".item")),
						file.getInt("recipe.random-map." + key + ".prob"));

		if (file.getBoolean("recipe.shaped"))
			if (random)
				return new IRandomShapedRecipe(ingredients, width, height, result, name, uuid, rngMap);
			else
				return new IShapedRecipe(ingredients, width, height, result, name, uuid);
		else if (random)
			return new IRandomShapelessRecipe(Arrays.asList(ingredients), result, name, uuid, rngMap);
		else
			return new IShapelessRecipe(Arrays.asList(ingredients), result, name, uuid);
	}

	@SuppressWarnings("nls")
	public static IRecipe getRecipeFromFile(File file, FileConfiguration fc, String path)
			throws IOException, InvalidConfigurationException {
		boolean save = false;

		ItemStack result = ItemStackUtils.getItemStack(fc, path + ".result");
		String name = fc.getString(path + ".name");

		int width = fc.getInt(path + ".width");
		int height = fc.getInt(path + ".height");

		ItemStack[] ingredients = new ItemStack[width * height];
		for (int i = 0; i < ingredients.length; i++)
			if (fc.getItemStack(path + ".ingredients." + i) != null)
				ingredients[i] = ItemStackUtils.getItemStack(fc, path + ".ingredients." + i);

		if (!fc.contains(path + ".random"))
			fc.set(path + ".random", !(save = true));

		boolean random = fc.getBoolean(path + ".random");
		boolean shaped = fc.getBoolean(path + ".shaped");

		UUID uuid;

		if (fc.contains(path + ".uuid"))
			uuid = UUID.fromString(fc.getString(path + ".uuid"));
		else {
			try {
				uuid = UUID.fromString(path.contains(".") ? path.split(".")[path.split(".").length - 1] : path); //$NON-NLS-3$
			} catch (IllegalArgumentException e) {
				uuid = UUID.randomUUID();
			}
			fc.set(path + ".uuid", uuid.toString());
			save = true;
		}

		Map<ItemStack, Integer> rngMap = new HashMap<>();

		if (random)
			fc.getConfigurationSection(path + ".random-map").getKeys(false).stream()
					.forEach(key -> rngMap.put(ItemStackUtils.getItemStack(fc, path + ".random-map." + key + ".item"),
							fc.getInt(path + ".random-map." + key + ".prob")));

		if (save) {
			fc.save(file);
			fc.load(file);
		}

		if (shaped)
			if (random)
				return new IRandomShapedRecipe(ingredients, width, height, result, name, uuid, rngMap);
			else
				return new IShapedRecipe(ingredients, width, height, result, name, uuid);
		else if (random)
			return new IRandomShapelessRecipe(Arrays.asList(ingredients), result, name, uuid, rngMap);
		else
			return new IShapelessRecipe(Arrays.asList(ingredients), result, name, uuid);
	}
	
	public static IRecipe fromVanillaRecipe(AbstractFancyCrafting pl, Recipe v) {
		return fromVanillaRecipe(pl, v, false);
	}
	
	@SuppressWarnings("nls")
	public static IRecipe fromVanillaRecipe(AbstractFancyCrafting pl, Recipe v, boolean ignoreMeta) {
		if (v == null)
			return null;
		IRecipe r = null;
		try {
			AtomicBoolean suitableForAutoMatching = new AtomicBoolean(true);
			if (v instanceof ShapedRecipe) {
				ShapedRecipe s = (ShapedRecipe) v;
				ItemStack[] ings = new ItemStack[9];
				for (int a = 0; a < s.getShape().length; a++) {
					String str = s.getShape()[a];
					for (int b = 0; b < str.length(); b++) {
						suitableForAutoMatching.compareAndSet(true,
								isSuitableForAutoMatching(s.getIngredientMap().get(str.charAt(b))));
						ings[a * 3 + b] = removeUnspecificMeta(s, s.getIngredientMap().get(str.charAt(b)), ignoreMeta);
					}
				}
				r = new IShapedRecipe(ings, 3, 3, v.getResult(), null, true, suitableForAutoMatching.get());
			} else if (v instanceof ShapelessRecipe) {
				ShapelessRecipe s = (ShapelessRecipe) v;
				r = new IShapelessRecipe(s.getIngredientList().stream().map(i -> {
					suitableForAutoMatching.compareAndSet(true, isSuitableForAutoMatching(i));
					return removeUnspecificMeta(s, i, ignoreMeta);
				}).collect(Collectors.toList()), v.getResult(), null, true, suitableForAutoMatching.get());
			}
		} catch (Exception e) {
			List<ItemStack> ings = null;
			if (v instanceof ShapedRecipe) {
				ings = new ArrayList<>(((ShapedRecipe) v).getIngredientMap().values());
			} else if (v instanceof ShapelessRecipe) {
				ings = ((ShapelessRecipe) v).getIngredientList();
			}
			pl.getLogger().warning(
					"Could not load Bukkit recipe w/ result: " + v.getResult() + " & ingredients: " + ings + ": " + e); //$NON-NLS-3$
			r = null;
		}
		return r;
	}
	
	@SuppressWarnings("deprecation")
	private static boolean isSuitableForAutoMatching(ItemStack i) {
		if (i == null)
			return true;
		return i.getDurability() != Short.MAX_VALUE;
	}

	@SuppressWarnings("deprecation")
	private static ItemStack removeUnspecificMeta(Recipe r, ItemStack is, boolean ignoreMeta) {
		if (is == null)
			return null;
		if (is.getItemMeta().toString().toLowerCase().contains("unspecific") && (ignoreMeta ? true : is.getDurability() == 0)) //$NON-NLS-1$
			is.setItemMeta(null);
		return is;
	}

	public static List<String> ingredientsToList(AbstractFancyCrafting pl, ItemStack[] ingredients, String format) {
		return ingredientsToList(pl, ingredients, 8, 6, format);
	}
	
	@SuppressWarnings("nls")
	public static List<String> ingredientsToList(AbstractFancyCrafting pl, ItemStack[] ingredients, int width, int height, String format) {
		StringBuilder builder = new StringBuilder();
		Map<Integer, Character> mapped = new HashMap<>();
		int cpos = 0;
		for (int i = 0; i < ingredients.length; i++) {
			if (ingredients[i] != null) {
				int hash = new IItemStack(ingredients[i]).hashCode();
				if (!mapped.containsKey(hash)) {
					mapped.put(hash, chars[cpos++]);
					builder.append(format
							.replace("%id%", String.valueOf(mapped.get(hash)))
							.replace("%item%", ItemStackUtils.getDisplayName(ingredients[i])) + "\n");
				}
			}
		}

		for (int col = 0; col < height; col++) {
			for (int row = 0; row < width; row++) {
				ItemStack ing = ingredients[col * width + row];
				if (ing == null)
					builder.append("§7----");
				else
					builder.append("§a" + mapped.get(new IItemStack(ing).hashCode()) + "§7x" + ing.getAmount()
							+ (ing.getAmount() >= 10 ? "" : "§7-"));
				if (row < width - 1)
					builder.append("§7|");
			}
			if (col < height - 1) {
				builder.append("\n§7");
				for (int i = 0; i < width; i++)
					builder.append("----" + (i % 2 == 0 ? "-" : ""));
				builder.append("\n");
			}
		}
		return Arrays.asList(builder.toString().split("\n"));
	}

	public static List<String> ingredientsToListColorless(AbstractFancyCrafting pl, ItemStack[] ingredients, String format) {
		return ingredientsToListColorless(pl, ingredients, 8, 6, format);
	}

	@SuppressWarnings("nls")
	public static List<String> ingredientsToListColorless(AbstractFancyCrafting pl, ItemStack[] ingredients, int width,
			int height, String format) {
		List<String> str = ingredientsToList(pl, ingredients, width, height, format);
		for (int i = 0; i < str.size(); i++)
			str.set(i, str.get(i).replaceAll("§[a-fk-rZ0-9]", ""));
		return str;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "IRecipe{name=" + recipeName + ";result=" + result + ";ingredients=" + getIngredients() + "}"; //$NON-NLS-3$ //$NON-NLS-4$
	}

	public Permission getCraftPermission() {
		return craftPermission;
	}

	public Permission getViewPermission() {
		return viewPermission;
	}

	public String getResultName() {
		return resultName;
	}
}