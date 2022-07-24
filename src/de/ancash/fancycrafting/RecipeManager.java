package de.ancash.fancycrafting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IRandomShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.fancycrafting.recipe.IRandomShapelessRecipe;
import de.ancash.minecraft.IItemStack;

@SuppressWarnings("nls")
public class RecipeManager {

	private final File recipeFile = new File("plugins/FancyCrafting/recipes.yml");
	private final FileConfiguration recipeCfg = YamlConfiguration.loadConfiguration(recipeFile);

	private final FancyCrafting plugin;
	private final Set<IRecipe> customRecipes = new HashSet<>();
	private final Set<IRecipe> autoMatchingRecipes = new HashSet<>();
	private final Map<Integer, Set<IRecipe>> customRecipesBySize = new ConcurrentHashMap<Integer, Set<IRecipe>>();
	private final Map<String, Set<IRecipe>> recipesByName = new ConcurrentHashMap<>();
	private final Map<Integer, Set<IRecipe>> recipesByResult = new ConcurrentHashMap<>();

	public RecipeManager(FancyCrafting plugin) throws IOException, InvalidConfigurationException {
		this.plugin = plugin;
		if (!recipeFile.exists())
			recipeFile.createNewFile();
		loadBukkitRecipes();
		loadCustomRecipes();
	}

	public void createRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String id, UUID uuid, int width,
			int height) throws IOException, InvalidConfigurationException {
		saveRecipe(result, ingredients, shaped, id, uuid, width, height);
		reloadRecipes();
	}

	public boolean exists(String recipeName) {
		return recipesByName.containsKey(recipeName) || recipesByName.containsKey(recipeName.replace(" ", "-"))
				|| recipesByName.containsKey(recipeName.replace("-", " "));
	}

	public void saveRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String name, UUID uuid, int width,
			int height) throws FileNotFoundException, IOException, InvalidConfigurationException {
		recipeCfg.load(recipeFile);
		if (shaped)
			new IShapedRecipe(ingredients, width, height, result, name, uuid).saveToFile(recipeCfg, uuid.toString());
		else
			new IShapelessRecipe(Arrays.asList(ingredients), result, name, uuid).saveToFile(recipeCfg, uuid.toString());
		recipeCfg.save(recipeFile);
	}

	public void saveRandomRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String name, UUID uuid,
			int width, int height, Map<ItemStack, Integer> rngMap)
			throws FileNotFoundException, IOException, InvalidConfigurationException {
		recipeCfg.load(recipeFile);
		if (shaped)
			new IRandomShapedRecipe(ingredients, width, height, result, name, uuid, rngMap).saveToFile(recipeCfg,
					uuid.toString());
		else
			new IRandomShapelessRecipe(Arrays.asList(ingredients), result, name, uuid, rngMap).saveToFile(recipeCfg,
					uuid.toString());
		recipeCfg.save(recipeFile);
	}

	public void reloadRecipes() {
		new BukkitRunnable() {

			@Override
			public void run() {
				plugin.getLogger().info("Reloading Recipes...");
				long now = System.currentTimeMillis();
				customRecipes.clear();
				recipesByName.clear();
				customRecipesBySize.clear();
				recipesByResult.clear();
				autoMatchingRecipes.clear();
				loadBukkitRecipes();
				loadCustomRecipes();
				plugin.getLogger().info("Reloaded! " + (System.currentTimeMillis() - now) + " ms");
			}
		}.runTaskAsynchronously(plugin);
	}

	private void loadCustomRecipes() {
		plugin.getLogger().info("Loading custom recipes...");
		try {
			recipeCfg.load(recipeFile);
		} catch (IOException | InvalidConfigurationException e1) {
			plugin.getLogger().log(Level.SEVERE, "Could not load recipes file", e1);
			return;
		}
		for (String key : recipeCfg.getKeys(false)) {
			try {
				IRecipe recipe = IRecipe.getRecipeFromFile(recipeFile, recipeCfg, key);
				plugin.getLogger().fine("-----------------------------------------------------");
				plugin.getLogger().fine("Name: " + recipe.getRecipeName());
				plugin.getLogger().fine("Result: " + recipe.getResult());
				plugin.getLogger().fine("Width: " + recipe.getWidth());
				plugin.getLogger().fine("Height: " + recipe.getHeight());
				plugin.getLogger().fine("Type: " + recipe.getClass().getSimpleName());
				plugin.getLogger().fine("Ingredients: \n" + (recipe instanceof IShapedRecipe ? String.join("\n",
						IRecipe.ingredientsToListColorless(plugin,
								recipe.getIngredients().toArray(new ItemStack[recipe.getIngredients().size()]),
								recipe.getWidth(), recipe.getHeight(), plugin.getWorkspaceObjects().getViewIngredientsIdFormat())) : ((IShapelessRecipe) recipe).getIngredients()));
				plugin.getLogger().fine("-----------------------------------------------------");
				registerRecipe(recipe);
				plugin.getLogger()
						.info("Loaded custom recipe: " + recipeCfg.getString(key + ".name") + " (" + key + ")");
			} catch (IOException | InvalidConfigurationException e) {
				plugin.getLogger().log(Level.SEVERE, "Could not load recipe with key " + key, e);
			}
		}
		plugin.getLogger().info("Loaded custom recipes!");
	}

	private void loadBukkitRecipes() {
		Bukkit.recipeIterator().forEachRemaining(r -> registerRecipe(IRecipe.fromVanillaRecipe(plugin, r)));
	}

	public final boolean registerRecipe(IRecipe recipe) {
		if (recipe == null)
			return false;
		if (recipe.getResult() == null || recipe.getResult().getType().equals(Material.AIR)) {
			plugin.getLogger().fine("Invalid recipe '" + recipe);
			return false;
		}
		int hash = new IItemStack(recipe.getResult()).hashCode();
		String name = recipe.getRecipeName().replace(" ", "-");

		recipesByName.computeIfAbsent(name, k -> new HashSet<>());
		recipesByName.get(name).add(recipe);
		recipesByResult.computeIfAbsent(hash, k -> new HashSet<>());
		recipesByResult.get(hash).add(recipe);

		if (recipe.isSuitableForAutoMatching())
			autoMatchingRecipes.add(recipe);
		else if (recipe.isVanilla())
			plugin.getLogger()
					.fine(String.format(
							"'%s' is not included in auto recipe matching (no unique ingredient identification)",
							recipe.getRecipeName()));
		if (!recipe.isVanilla()) {
			customRecipesBySize.computeIfAbsent(recipe.getIngredientsSize(), k -> new HashSet<>());
			customRecipesBySize.get(recipe.getIngredientsSize()).add(recipe);
			customRecipes.add(recipe);
		}
		return true;
	}

	public IRecipe matchRecipe(IMatrix<IItemStack> matrix, Set<IRecipe> recipes) {
		int size = (int) Arrays.asList(matrix.getArray()).stream().filter(item -> item != null).count();
		for (IRecipe recipe : recipes.stream().filter(recipe -> recipe.getIngredientsSize() == size)
				.collect(Collectors.toSet()))
			if (recipe instanceof IShapedRecipe && IRecipe.matchesShaped((IShapedRecipe) recipe, matrix.getArray(),
					matrix.getWidth(), matrix.getHeight()))
				return recipe;
			else if (recipe instanceof IShapelessRecipe
					&& IRecipe.matchesShapeless((IShapelessRecipe) recipe, matrix.getArray()))
				return recipe;
		return null;
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

	public Set<IRecipe> getCustomRecipesBySize(int i) {
		if (customRecipesBySize.get(i) == null)
			return null;
		return Collections.unmodifiableSet(customRecipesBySize.get(i));
	}

	public void delete(String recipeName) throws FileNotFoundException, IOException, InvalidConfigurationException {
		recipeCfg.load(recipeFile);
		recipeCfg.set(recipeName, null);
		recipeCfg.save(recipeFile);
		reloadRecipes();
	}

	public Set<IRecipe> getRecipeByResult(ItemStack itemStack) {
		return getRecipeByResult(new IItemStack(itemStack));
	}

	public Set<IRecipe> getRecipeByResult(IItemStack iItemStack) {
		if (recipesByResult.get(iItemStack.hashCode()) == null)
			return null;
		return Collections.unmodifiableSet(recipesByResult.get(iItemStack.hashCode()));
	}

	public void clear() {
		customRecipes.clear();
		this.autoMatchingRecipes.clear();
		this.customRecipesBySize.clear();
		this.recipesByName.clear();
		this.recipesByResult.clear();
	}
}
