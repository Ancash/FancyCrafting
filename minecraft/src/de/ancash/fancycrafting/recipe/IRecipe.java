package de.ancash.fancycrafting.recipe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.complex.ArmorDyeRecipe;
import de.ancash.fancycrafting.recipe.complex.BannerDuplicateRecipe;
import de.ancash.fancycrafting.recipe.complex.BookDuplicateRecipe;
import de.ancash.fancycrafting.recipe.complex.FireworkRecipe;
import de.ancash.fancycrafting.recipe.complex.RepairRecipe;
import de.ancash.fancycrafting.recipe.complex.ShulkerDyeRecipe;
import de.ancash.lambda.Lambda;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.crafting.recipe.ComplexRecipeWrapper;
import de.ancash.minecraft.crafting.recipe.WrappedRecipe;
import de.ancash.nbtnexus.serde.SerializedItem;
import de.ancash.nbtnexus.serde.access.SerializedMetaAccess;

public abstract class IRecipe {

	private static final char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWYabcdefghijklmnopqrstuvw".toCharArray(); //$NON-NLS-1$

	protected final SerializedItem result;
	protected final String recipeName;
	protected final String resultName;
	protected final boolean vanilla;
	protected final boolean suitableForAutoMatching;
	protected final UUID uuid;
	protected final Permission craftPermission;
	protected final Permission viewPermission;
	protected final RecipeCategory category;
	protected Map<SerializedItem, Integer> mappedIngredients;

	public IRecipe(ItemStack result, String name, UUID uuid, RecipeCategory category) {
		this(result, name, false, true, uuid, category);
	}

	public IRecipe(ItemStack result, String name, boolean vanilla, boolean suitableForAutoMatching,
			RecipeCategory category) {
		this(result, name, vanilla, suitableForAutoMatching, null, category);
	}

	@SuppressWarnings("nls")
	IRecipe(ItemStack result, String name, boolean vanilla, boolean suitableForAutoMatching, UUID uuid,
			RecipeCategory category) {
		this.uuid = uuid;
		if (vanilla) {
			this.category = RecipeCategory.VANILLA;
		} else if (category == null)
			this.category = RecipeCategory.DEFAULT;
		else
			this.category = category;
		this.result = result == null ? null : SerializedItem.of(result);
		this.vanilla = vanilla;
		if (vanilla)
			this.suitableForAutoMatching = suitableForAutoMatching;
		else
			this.suitableForAutoMatching = true;
		if (result != null) {
			if (vanilla)
				this.recipeName = ItemStackUtils.getDisplayName(result);
			else
				this.recipeName = name;
			resultName = ItemStackUtils.getDisplayName(result);
			this.craftPermission = new Permission("fancycrafting.craft." + this.recipeName.replace(" ", "-"), //$NON-NLS-3$
					FancyCrafting.getCraftPermDef());
			this.viewPermission = new Permission("fancycrafting.view." + this.recipeName.replace(" ", "-"), //$NON-NLS-3$
					FancyCrafting.getViewPermDef());
		} else {
			this.recipeName = name == null ? "null" : name;
			this.resultName = "null";
			this.craftPermission = null;
			this.viewPermission = null;
			mappedIngredients = null;
		}
	}

	public abstract String saveToString() throws IOException;

	public abstract void saveToFile(FileConfiguration file, String path);

	public abstract int getHeight();

	public abstract int getWidth();

	public abstract Map<Integer, Integer> getIngredientsHashCodes();

	public abstract List<Integer> getHashMatrix();

	public abstract List<ItemStack> getIngredients();

	public abstract List<SerializedItem> getSerializedIngredients();

	public abstract boolean isShiftCollectable();

	public abstract boolean matches(IMatrix<SerializedItem> m);

	public ItemStack getResult() {
		return result == null ? null : result.toItem();
	}

	public Map<SerializedItem, Integer> mapIngredients() {
		if (mappedIngredients == null)
			mappedIngredients = Collections.unmodifiableMap(
					getSerializedIngredients().stream().filter(Lambda.notNull()).collect(Collectors.toMap(i -> i,
							i -> SerializedMetaAccess.UNSPECIFIC_META_ACCESS.getAmount(i.getMap()), (a, b) -> a + b)));
		return mappedIngredients;
	}

	public SerializedItem getResultAsSerializedItem() {
		return result;
	}

	public boolean isVanilla() {
		return vanilla;
	}

	public RecipeCategory getCategory() {
		return category;
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

	public static IRecipe getRecipeFromFile(File file, String path) throws IOException, InvalidConfigurationException {
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		fc.load(file);
		IRecipe r = getRecipeFromFile(file, fc, path);
		fc.save(file);
		return r;
	}

//	@SuppressWarnings("nls")
//	public static IRecipe getRecipeFromStrings(String string) throws IOException, InvalidConfigurationException {
//		YamlFile file = YamlFile.loadConfigurationFromString(string);
//		ItemStack result = ItemStackUtils.itemStackFromString(file.getString("recipe.result"));
//		String name = file.getString("recipe.name");
//
//		int width = file.getInt("recipe.width");
//		int height = file.getInt("recipe.height");
//
//		ItemStack[] ingredients = new ItemStack[width * height];
//		for (int i = 0; i < ingredients.length; i++)
//			if (file.contains("recipe.ingredients." + i))
//				ingredients[i] = ItemStackUtils.itemStackFromString(file.getString("recipe.ingredients." + i));
//
//		boolean random = file.getBoolean("recipe.random");
//
//		UUID uuid = UUID.fromString(file.getString("recipe.uuid"));
//
//		Map<ItemStack, Integer> rngMap = new HashMap<>();
//
//		if (random)
//			for (String key : file.getConfigurationSection("recipe.random-map").getKeys(false))
//				rngMap.put(ItemStackUtils.itemStackFromString(file.getString("recipe.random-map." + key + ".item")),
//						file.getInt("recipe.random-map." + key + ".prob"));
//
//		if (file.getBoolean("recipe.shaped"))
//			if (random)
//				return new IRandomShapedRecipe(ingredients, width, height, result, name, uuid, rngMap);
//			else
//				return new IShapedRecipe(ingredients, width, height, result, name, uuid);
//		else if (random)
//			return new IRandomShapelessRecipe(Arrays.asList(ingredients), result, name, uuid, rngMap);
//		else
//			return new IShapelessRecipe(Arrays.asList(ingredients), result, name, uuid);
//	}

	@SuppressWarnings("nls")
	public static IRecipe getRecipeFromFile(File file, FileConfiguration fc, String path)
			throws IOException, InvalidConfigurationException {
		boolean save = false;

		ItemStack result = fc.contains(path + ".result") ? ItemStackUtils.getItemStack(fc, path + ".result") : null;
		String name = fc.getString(path + ".name");
		String catName = fc.getString(path + ".category");
		if (catName == null) {
			catName = RecipeCategory.DEFAULT.getName();
			fc.set(path + ".category", catName);
			FancyCrafting.getPlugin(FancyCrafting.class).getLogger()
					.info("No category set for '" + name + "' at '" + path + "'. Set to '" + catName + "'");
			save = true;
		}
		RecipeCategory category = RecipeCategory.getOrCreateCategory(catName);

		int width = fc.getInt(path + ".width");
		int height = fc.getInt(path + ".height");

		ItemStack[] ingredients = new ItemStack[width * height];
		for (int i = 0; i < ingredients.length; i++)
			if (fc.contains(path + ".ingredients." + i))
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
			for (String key : fc.getConfigurationSection(path + ".random-map").getKeys(false))
				rngMap.put(ItemStackUtils.getItemStack(fc, path + ".random-map." + key + ".item"),
						fc.getInt(path + ".random-map." + key + ".prob"));

		if (save) {
			fc.save(file);
			fc.load(file);
		}

		if (shaped)
			if (random)
				return new IRandomShapedRecipe(ingredients, width, height, result, name, uuid, rngMap, category);
			else
				return new IShapedRecipe(ingredients, width, height, result, name, uuid, category);
		else if (random)
			return new IRandomShapelessRecipe(Arrays.asList(ingredients), result, name, uuid, rngMap, category);
		else
			return new IShapelessRecipe(Arrays.asList(ingredients), result, name, uuid, category);
	}

	public static IRecipe fromVanillaRecipe(FancyCrafting pl, Recipe v) {
		return fromVanillaRecipe(pl, v, false);
	}

	@SuppressWarnings("nls")
	public static IRecipe fromVanillaRecipe(FancyCrafting pl, Recipe v, boolean ignoreMeta) {
		if (v == null)
			return null;
		IRecipe r = null;

		if (v instanceof WrappedRecipe) {
			WrappedRecipe wrapped = (WrappedRecipe) v;

			if (wrapped instanceof ComplexRecipeWrapper) {
				ComplexRecipeWrapper complex = (ComplexRecipeWrapper) wrapped;
				ItemStack[] ings = new ItemStack[9];
				for (int a = 0; a < complex.getShape().length; a++) {
					String str = complex.getShape()[a];
					for (int b = 0; b < str.length(); b++) {
						ings[a * 3 + b] = removeUnspecificMeta(complex, complex.getIngredientMap().get(str.charAt(b)),
								ignoreMeta);
					}
				}
				switch (complex.getType()) {
				case BOOK_DUPLICATE:
					return new BookDuplicateRecipe(Arrays.asList(ings), v.getResult());
				case BANNER_DUPLICATE:
					return new BannerDuplicateRecipe(Arrays.asList(ings), v.getResult());
				case ARMOR_DYE:
					return new ArmorDyeRecipe(Arrays.asList(ings), v.getResult());
				case SHULKER_DYE:
					return new ShulkerDyeRecipe(Arrays.asList(ings), v.getResult());
				case FIREWORK:
					return new FireworkRecipe(Arrays.asList(ings), v.getResult());
				case REPAIR:
					return new RepairRecipe(Arrays.asList(ings), v.getResult());
				default:
					throw new IllegalArgumentException(complex.getType().name());
				}
			}
		}

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
				r = new IShapedRecipe(ings, 3, 3, v.getResult(), null, true, suitableForAutoMatching.get(), null);
			} else if (v instanceof ShapelessRecipe) {
				ShapelessRecipe s = (ShapelessRecipe) v;
				r = new IShapelessRecipe(s.getIngredientList().stream().map(i -> {
					suitableForAutoMatching.compareAndSet(true, isSuitableForAutoMatching(i));
					return removeUnspecificMeta(s, i, ignoreMeta);
				}).collect(Collectors.toList()), v.getResult(), null, true, suitableForAutoMatching.get(), null);
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
		if (is.hasItemMeta() && is.getItemMeta().toString().toLowerCase(Locale.ENGLISH).contains("unspecific") //$NON-NLS-1$
				&& (ignoreMeta ? true : is.getDurability() == 0))
			is.setItemMeta(null);
		return is;
	}

	public static List<String> ingredientsToList(FancyCrafting pl, ItemStack[] ingredients, String format) {
		return ingredientsToList(pl, ingredients, 8, 6, format);
	}

	@SuppressWarnings("nls")
	public static List<String> ingredientsToList(FancyCrafting pl, ItemStack[] ingredients, int width, int height,
			String format) {
		StringBuilder builder = new StringBuilder();
		Map<Integer, Character> mapped = new HashMap<>();
		int cpos = 0;
		for (int i = 0; i < ingredients.length; i++) {
			if (ingredients[i] != null) {
				int hash = SerializedItem.of(ingredients[i]).hashCode();
				if (!mapped.containsKey(hash)) {
					mapped.put(hash, chars[cpos++]);
					builder.append(format.replace("%id%", String.valueOf(mapped.get(hash))).replace("%item%",
							ItemStackUtils.getDisplayName(ingredients[i]))).append('\n');
				}
			}
		}

		for (int col = 0; col < height; col++) {
			for (int row = 0; row < width; row++) {
				ItemStack ing = ingredients[col * width + row];
				if (ing == null)
					builder.append("§7----");
				else
					builder.append("§a").append(mapped.get(SerializedItem.of(ing).hashCode())).append("§7x")
							.append(ing.getAmount()).append(ing.getAmount() >= 10 ? "" : "§7-");
				if (row < width - 1)
					builder.append("§7|");
			}
			if (col < height - 1) {
				builder.append("\n§7");
				for (int i = 0; i < width; i++)
					builder.append("----" + (i % 2 == 0 ? "-" : ""));
				builder.append('\n');
			}
		}
		return Arrays.asList(builder.toString().split("\n"));
	}

	public static List<String> ingredientsToListColorless(FancyCrafting pl, ItemStack[] ingredients, String format) {
		return ingredientsToListColorless(pl, ingredients, 8, 6, format);
	}

	@SuppressWarnings("nls")
	public static List<String> ingredientsToListColorless(FancyCrafting pl, ItemStack[] ingredients, int width,
			int height, String format) {
		List<String> str = ingredientsToList(pl, ingredients, width, height, format);
		for (int i = 0; i < str.size(); i++)
			str.set(i, str.get(i).replaceAll("§[a-fk-rZ0-9]", ""));
		return str;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "IRecipe{name=" + recipeName + ";result=" + result.getMap() + ";ingredients=" //$NON-NLS-3$
				+ getSerializedIngredients().stream().map(s -> s != null ? s.getMap() : null)
						.collect(Collectors.toList())
				+ "}"; //$NON-NLS-1$
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