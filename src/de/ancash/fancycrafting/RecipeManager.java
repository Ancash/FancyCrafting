package de.ancash.fancycrafting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.fancycrafting.gui.AbstractCraftingGUI;
import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.ItemStackUtils;

public class RecipeManager {
	
	private final File recipeFile = new File("plugins/FancyCrafting/recipes.yml");
	private final FileConfiguration recipeCfg = YamlConfiguration.loadConfiguration(recipeFile);
	
	private final FancyCrafting plugin;
	private final Map<Integer, Set<IRecipe>> customRecipesBySize = new HashMap<Integer, Set<IRecipe>>();
	private final Map<String, Set<IRecipe>> recipesByName = new HashMap<>();
	private final Map<Integer, Set<IRecipe>> recipesByResult = new HashMap<>();
	
	public RecipeManager(FancyCrafting plugin) throws IOException, InvalidConfigurationException {
		this.plugin = plugin;
		if(!recipeFile.exists())
			recipeFile.createNewFile();
		loadBukkitRecipes();
		loadCustomRecipes();
	}	
	
	public void updateRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String id, int width, int height) throws IOException, InvalidConfigurationException {
		saveRecipe(result, ingredients, shaped, id, width, height);
		reloadRecipes();
	}
	
	public void createRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String id, UUID uuid, int width, int height) throws IOException, InvalidConfigurationException {
		saveRecipe(result, ingredients, shaped, id, uuid, width, height);
		reloadRecipes();
	}
	
	public boolean exists(String recipeName) {
		return recipesByName.containsKey(recipeName) || recipesByName.containsKey(recipeName.replace(" ", "-")) || recipesByName.containsKey(recipeName.replace("-", " "));
	}
	
	public void saveRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String name, int width, int height) throws IOException, InvalidConfigurationException {
		saveRecipe(result, ingredients, shaped, name, UUID.randomUUID(), height, width);
	}
	
	public void saveRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String name, UUID uuid, int width, int height) throws FileNotFoundException, IOException, InvalidConfigurationException {
		IMatrix<ItemStack> matrix = new IMatrix<>(ingredients, width, height);
		matrix.optimize();
		ingredients = matrix.getArray();
		recipeCfg.load(recipeFile);
		recipeCfg.set(uuid + "", null);
		recipeCfg.set(uuid + ".name", name);
		ItemStackUtils.setItemStack(recipeCfg, uuid + ".result", result);
		recipeCfg.set(uuid + ".shaped", shaped);
		recipeCfg.set(uuid + ".width", matrix.getWidth());
		recipeCfg.set(uuid + ".height", matrix.getHeight());
		for(int i = 0; i<ingredients.length; i++) 
			if(ingredients[i] != null)
				ItemStackUtils.setItemStack(recipeCfg, uuid + ".ingredients." + i, ingredients[i]);
		recipeCfg.save(recipeFile);
	}
	
	public void reloadRecipes() {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				plugin.info("Reloading Recipes!");
				long now = System.currentTimeMillis();
				recipesByName.clear();
				customRecipesBySize.clear();
				recipesByResult.clear();
				loadBukkitRecipes();
				try {
					loadCustomRecipes();
				} catch (IOException | InvalidConfigurationException e) {
					plugin.severe("Could not load custom recipes from file:");
					e.printStackTrace();
				}
				plugin.info("Reloaded! " + (System.currentTimeMillis() - now) + " ms");
			}
		}.runTaskAsynchronously(plugin);
	}
	
	private void loadCustomRecipes() throws IOException, InvalidConfigurationException {
		plugin.info("Loading custom recipes...");
		for(String key : recipeCfg.getKeys(false)) {
			try {
				registerRecipe(getRecipeFromFile(key));
				plugin.info("Loaded custom recipe: " + recipeCfg.getString(key + ".name") + " (" + key + ")");
			} catch (ClassNotFoundException | IOException e) {
				System.err.println("Could not load recipe w/ key " + key + ":");
				e.printStackTrace();
			}
		}
		plugin.info("Loaded custom recipes!");
	}
	
	public IRecipe getRecipeFromFile(String key) throws ClassNotFoundException, IOException {
		ItemStack result = ItemStackUtils.getItemStack(recipeCfg, key + ".result");
		String name = recipeCfg.getString(key + ".name");
		ItemStack[] ingredients = new ItemStack[getWidth(key) * getHeight(key)];
		for(int i = 0; i<ingredients.length; i++) 
			if(recipeCfg.getItemStack(key + ".ingredients." + i) != null) 
				ingredients[i] = ItemStackUtils.getItemStack(recipeCfg, key + ".ingredients." + i);
				
		if(recipeCfg.getBoolean(key + ".shaped")) {	
			return new IShapedRecipe(ingredients, getWidth(key), getHeight(key), result, name, UUID.fromString(key));
		} else {
			return new IShapelessRecipe(Arrays.asList(ingredients), result, name, UUID.fromString(key));
		}
	}
	
	private int getWidth(String path) {
		if(!recipeCfg.contains(path + ".width"))
			return recipeCfg.getInt(path + ".matrix");
		return recipeCfg.getInt(path + ".width");
	}
	
	private int getHeight(String path) {
		if(!recipeCfg.contains(path + ".height"))
			return recipeCfg.getInt(path + ".matrix");
		return recipeCfg.getInt(path + ".height");
	}
	
	private void loadBukkitRecipes() {
		Bukkit.recipeIterator().forEachRemaining(r -> registerRecipe(IRecipe.fromVanillaRecipe(r)));
	}
	
	public boolean registerRecipe(IRecipe recipe) {
		if(recipe == null)
			return false;
		if(recipe.getResult() == null || recipe.getResult().getType().equals(Material.AIR)) {
			plugin.warn("Invalid recipe '" + recipe);
			return false;
		}
		int hash = new IItemStack(recipe.getResult()).hashCode();
		String name = recipe.getName().replace(" ", "-");
		
		recipesByName.computeIfAbsent(name, k -> new HashSet<>());
		recipesByName.get(name).add(recipe);
		recipesByResult.computeIfAbsent(hash, k -> new HashSet<>());
		recipesByResult.get(hash).add(recipe);
		
		if(!recipe.isVanilla()) {
			customRecipesBySize.computeIfAbsent(recipe.getIngredientsSize(), k -> new HashSet<>());
			customRecipesBySize.get(recipe.getIngredientsSize()).add(recipe);
		}
		return true;
	}

	public IRecipe matchRecipe(ItemStack[] ingredients, int width, int height, AbstractCraftingGUI gui) {
		IRecipe vanilla = matchVanillaRecipe(ingredients, width, height, gui);
		if(vanilla != null)
			return vanilla;
		
		Collection<ItemStack> ingredientsList = Arrays.asList(ingredients).stream().filter(i -> i != null).collect(Collectors.toList());
		if(!customRecipesBySize.containsKey(ingredientsList.size()))
			return null;

		for(IRecipe recipe : customRecipesBySize.get(ingredientsList.size())) {
			if(recipe instanceof IShapedRecipe) {
				IShapedRecipe shaped = (IShapedRecipe) recipe;
				try {
					if(IRecipe.matchesShaped(shaped, ingredients, width, height))
						return recipe;
				} catch(NullPointerException npe) {
					npe.printStackTrace();
				}
			} else if(recipe instanceof IShapelessRecipe) {
				IShapelessRecipe shapeless = (IShapelessRecipe) recipe;
				if(IRecipe.matchesShapeless(shapeless.getIngredients().toArray(new ItemStack[shapeless.getIngredientsSize()]), ingredients))
					return recipe;
			}
		}
		return null;
	}
	
	public Stream<IRecipe> getCustomRecipes() {
		return customRecipesBySize.values().stream().flatMap(Collection::stream);
	}
	
	public IRecipe matchVanillaRecipe(ItemStack[] ingredients, int width, int height, AbstractCraftingGUI gui) {
		return gui.getVanillaRecipeMatcher().matchVanillaRecipe(new IMatrix<>(ingredients, width, height));
	}
	
	public Set<IRecipe> getRecipeByName(String name) {
		return recipesByName.get(name);
	}

	public void delete(String recipeName) throws FileNotFoundException, IOException, InvalidConfigurationException {
		recipeCfg.load(recipeFile);
		recipeCfg.set(recipeName + "", null);
		recipeCfg.save(recipeFile);
		reloadRecipes();
	}

	public Set<IRecipe> getRecipeByResult(IItemStack iItemStack) {
		return recipesByResult.get(iItemStack.hashCode());
	}
}
