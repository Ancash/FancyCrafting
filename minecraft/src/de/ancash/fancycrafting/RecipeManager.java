package de.ancash.fancycrafting;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.exception.InvalidRecipeException;
import de.ancash.fancycrafting.exception.RecipeDeleteException;
import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRandomShapedRecipe;
import de.ancash.fancycrafting.recipe.IRandomShapelessRecipe;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.fancycrafting.recipe.RecipeCategory;
import de.ancash.misc.MathsUtils;
import de.ancash.nbtnexus.serde.SerializedItem;

public class RecipeManager {

	protected final Map<List<Integer>, IRecipe> cachedRecipes = new ConcurrentHashMap<>();
	protected final Set<IRecipe> customRecipes = new HashSet<>();
	protected final Set<IRecipe> autoMatchingRecipes = new HashSet<>();
	protected final Map<String, Set<IRecipe>> recipesByName = new ConcurrentHashMap<>();
	protected final Map<Integer, Set<IRecipe>> recipesByHash = new ConcurrentHashMap<>();

	private final File recipeFile = new File("plugins/FancyCrafting/recipes.yml"); //$NON-NLS-1$
	private final FileConfiguration recipeCfg = YamlConfiguration.loadConfiguration(recipeFile);

	private final File blacklistFile = new File("plugins/FancyCrafting/blacklist/recipes.yml"); //$NON-NLS-1$
	private final FileConfiguration blacklistCfg = YamlConfiguration.loadConfiguration(blacklistFile);

	private final Map<List<Integer>, IRecipe> blacklistedRecipes = new HashMap<>();
	protected final FancyCrafting plugin;

	public RecipeManager(FancyCrafting pl) throws IOException {
		this.plugin = pl;
		if (!recipeFile.exists())
			recipeFile.createNewFile();
		if (!blacklistFile.exists())
			blacklistFile.createNewFile();
		loadBukkitRecipes();
		loadCustomRecipes();
		loadBlacklistedRecipes();
	}

	public void loadBukkitRecipes() {
		Bukkit.recipeIterator().forEachRemaining(r -> {
			if (r instanceof Keyed) {
				Keyed k = (Keyed) r;
				if (k.getKey().getNamespace().equals("customcrafting")) {
					plugin.getLogger().info("Skipped recipe " + k.getKey());
					return;
				}
			}
			IRecipe recipe = IRecipe.fromVanillaRecipe(plugin, r);
			if (recipe == null)
				return;

			registerRecipe(recipe);
		});
	}

	public FileConfiguration getBlacklistRecipeFileCfg() {
		return blacklistCfg;
	}

	public File getBlacklistRecipeFile() {
		return blacklistFile;
	}

	public void cacheRecipe(IRecipe recipe) {
		if (recipe.isVanilla())
			return;
		if (recipe instanceof IShapedRecipe)
			cachedRecipes.put(recipe.getHashMatrix(), recipe);
		else
			cachedRecipes.put(
					recipe.getHashMatrix().stream().filter(i -> i != null).sorted().collect(Collectors.toList()),
					recipe);
	}

	public Set<IRecipe> getRecipeByResult(ItemStack itemStack) {
		return getRecipeByResult(SerializedItem.of(itemStack));
	}

	public Set<IRecipe> getRecipeByResult(SerializedItem iItemStack) {
		if (recipesByHash.get(iItemStack.hashCode()) == null)
			return null;
		return Collections.unmodifiableSet(recipesByHash.get(iItemStack.hashCode()));
	}

	public boolean registerRecipe(IRecipe recipe) {
		if (recipe == null)
			return false;
		if (recipe.getResult() == null || recipe.getResult().getType().equals(Material.AIR)) {
			plugin.getLogger().fine("Invalid recipe '" + recipe); //$NON-NLS-1$
			return false;
		}
		int hash = SerializedItem.of(recipe.getResult()).hashCode();
		String name = recipe.getRecipeName().replace(" ", "-"); //$NON-NLS-1$//$NON-NLS-2$

		recipesByName.computeIfAbsent(name, k -> new HashSet<>());
		recipesByName.get(name).add(recipe);
		recipesByHash.computeIfAbsent(hash, k -> new HashSet<>());
		recipesByHash.get(hash).add(recipe);

		if (recipe.isSuitableForAutoMatching())
			autoMatchingRecipes.add(recipe);
		else if (recipe.isVanilla())
			plugin.getLogger()
					.fine(String.format(
							"'%s' is not included in auto recipe matching (no unique ingredient identification)", //$NON-NLS-1$
							recipe.getRecipeName()));
		if (!recipe.isVanilla()) {
			customRecipes.add(recipe);
			cacheRecipe(recipe);
		}
		return true;
	}

	public IRecipe matchRecipe(IMatrix<SerializedItem> matrix) {
		IRecipe match = cachedRecipes.get(
				Stream.of(matrix.getArray()).map(i -> i != null ? i.hashCode() : null).collect(Collectors.toList()));
		if (match == null) {
			List<Integer> hashs = Stream.of(matrix.getArray()).filter(i -> i != null).map(i -> i.hashCode())
					.collect(Collectors.toList());
			match = cachedRecipes.get(hashs);
			if (match == null) {
				Collections.sort(hashs);
				match = cachedRecipes.get(hashs);
				if (match == null)
					return null;
			}
		}
		return match.matches(matrix) ? match : null;
	}

	public void reloadRecipes() {
		plugin.getLogger().info("Reloading Recipes..."); //$NON-NLS-1$
		long now = System.nanoTime();
		clear();
		loadRecipes();
		plugin.getLogger().info("Reloaded! " + (MathsUtils.round((System.nanoTime() - now) / 1000000000D, 3)) + "s"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public Set<IRecipe> getAutoMatchingRecipes() {
		return Collections.unmodifiableSet(autoMatchingRecipes);
	}

	public Set<IRecipe> getCustomRecipes() {
		return Collections.unmodifiableSet(customRecipes);
	}

	public Set<IRecipe> getRecipeByName(String name) {
		if (recipesByName.get(name) == null)
			return null;
		return Collections.unmodifiableSet(recipesByName.get(name));
	}

	@SuppressWarnings("nls")
	public void printRecipe(IRecipe recipe) {
		plugin.getLogger().fine("Name: " + recipe.getRecipeName());
		plugin.getLogger().fine("Result: " + recipe.getResult());
		plugin.getLogger().fine("Width: " + recipe.getWidth());
		plugin.getLogger().fine("Height: " + recipe.getHeight());
		plugin.getLogger().fine("Type: " + recipe.getClass().getSimpleName());
		plugin.getLogger()
				.fine("Ingredients: \n"
						+ (recipe instanceof IShapedRecipe
								? String.join("\n",
										IRecipe.ingredientsToListColorless(plugin,
												recipe.getIngredients()
														.toArray(new ItemStack[recipe.getIngredients().size()]),
												recipe.getWidth(), recipe.getHeight(),
												plugin.getWorkspaceObjects().getViewIngredientsIdFormat()))
								: ((IShapelessRecipe) recipe).getIngredients()));
	}

	public boolean isBlacklisted(List<Integer> hashs) {
		if (blacklistedRecipes.containsKey(hashs))
			return true;
		hashs = hashs.stream().filter(i -> i != null).collect(Collectors.toList());
		Collections.sort(hashs);
		return blacklistedRecipes.containsKey(hashs);
	}

	public Set<List<Integer>> getBlacklistedRecipesHashes() {
		return blacklistedRecipes.keySet();
	}

	public Map<List<Integer>, IRecipe> getBlacklistedRecipes() {
		return blacklistedRecipes;
	}

	public void addBlacklistedRecipe(IRecipe disabled) {
		blacklistedRecipes.put(disabled.getHashMatrix(), disabled);
		plugin.getLogger()
				.info("Loaded blacklisted recipe: " + disabled.getRecipeName() + " (" + disabled.getUUID() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void loadBlacklistedRecipes() {
		plugin.getLogger().info("Loading blacklisted recipes..."); //$NON-NLS-1$
		try {
			blacklistCfg.load(blacklistFile);
		} catch (IOException | InvalidConfigurationException e1) {
			plugin.getLogger().log(Level.SEVERE, "Could not load blacklist file", e1); //$NON-NLS-1$
			return;
		}
		blacklistedRecipes.clear();
		for (String key : blacklistCfg.getKeys(false)) {
			try {
				IRecipe recipe = IRecipe.getRecipeFromFile(blacklistFile, blacklistCfg, key);
				plugin.getLogger().fine("-----------------------------------------------------"); //$NON-NLS-1$
				printRecipe(recipe);
				plugin.getLogger().fine("-----------------------------------------------------"); //$NON-NLS-1$
				addBlacklistedRecipe(recipe);
			} catch (IOException | InvalidConfigurationException e) {
				plugin.getLogger().log(Level.SEVERE, "Could not load recipe with key " + key, e); //$NON-NLS-1$
			}
		}
		plugin.getLogger().info("Loaded blacklisted recipes!"); //$NON-NLS-1$
	}

	public void createRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String id, UUID uuid, int width,
			int height, RecipeCategory category) throws InvalidRecipeException {
		saveRecipe(result, ingredients, shaped, id, uuid, width, height, category);
		reloadRecipes();
	}

	public void saveRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String name, UUID uuid, int width,
			int height, RecipeCategory category) throws InvalidRecipeException {
		try {
			recipeCfg.load(recipeFile);
			if (shaped)
				new IShapedRecipe(ingredients, width, height, result, name, uuid, category).saveToFile(recipeCfg,
						uuid.toString());
			else
				new IShapelessRecipe(Arrays.asList(ingredients), result, name, uuid, category).saveToFile(recipeCfg,
						uuid.toString());
			recipeCfg.save(recipeFile);
		} catch (Exception ex) {
			throw new InvalidRecipeException(ex);
		}
	}

	public void saveRandomRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String name, UUID uuid,
			int width, int height, Map<ItemStack, Integer> rngMap, RecipeCategory category)
			throws InvalidRecipeException {
		try {
			recipeCfg.load(recipeFile);
			if (shaped)
				new IRandomShapedRecipe(ingredients, width, height, result, name, uuid, rngMap, category)
						.saveToFile(recipeCfg, uuid.toString());
			else
				new IRandomShapelessRecipe(Arrays.asList(ingredients), result, name, uuid, rngMap, category)
						.saveToFile(recipeCfg, uuid.toString());
			recipeCfg.save(recipeFile);
		} catch (Exception ex) {
			throw new InvalidRecipeException(ex);
		}
	}

	public void loadRecipes() {
		loadBukkitRecipes();
		loadCustomRecipes();
		loadBlacklistedRecipes();
	}

	@SuppressWarnings("nls")
	public void clear() {
		blacklistedRecipes.clear();
		cachedRecipes.clear();
		autoMatchingRecipes.clear();
		recipesByHash.clear();
		recipesByName.clear();
		customRecipes.clear();
		plugin.getLogger().info("Cleared recipe cache");
	}

	public void loadCustomRecipes() {
		plugin.getLogger().info("Loading custom recipes..."); //$NON-NLS-1$
		try {
			recipeCfg.load(recipeFile);
		} catch (IOException | InvalidConfigurationException e1) {
			plugin.getLogger().log(Level.SEVERE, "Could not load recipes file", e1); //$NON-NLS-1$
			return;
		}
		for (String key : recipeCfg.getKeys(false)) {
			try {
				IRecipe recipe = IRecipe.getRecipeFromFile(recipeFile, recipeCfg, key);
				plugin.getLogger().fine("-----------------------------------------------------"); //$NON-NLS-1$
				printRecipe(recipe);
				plugin.getLogger().fine("-----------------------------------------------------"); //$NON-NLS-1$
				registerRecipe(recipe);
				plugin.getLogger()
						.info("Loaded custom recipe: " + recipeCfg.getString(key + ".name") + " (" + key + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			} catch (IOException | InvalidConfigurationException e) {
				plugin.getLogger().log(Level.SEVERE, "Could not load recipe with key " + key, e); //$NON-NLS-1$
			}
		}
		plugin.getLogger().info("Loaded custom recipes!"); //$NON-NLS-1$
	}

	public void delete(String recipeName) throws RecipeDeleteException {
		try {
			recipeCfg.load(recipeFile);
			recipeCfg.set(recipeName, null);
			recipeCfg.save(recipeFile);
			reloadRecipes();
		} catch (Exception ex) {
			throw new RecipeDeleteException(recipeName, ex);
		}
	}
}