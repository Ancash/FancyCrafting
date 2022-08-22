package de.ancash.fancycrafting;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.base.IRecipeManager;
import de.ancash.fancycrafting.exception.InvalidRecipeException;
import de.ancash.fancycrafting.exception.RecipeDeleteException;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IRandomShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.fancycrafting.recipe.IRandomShapelessRecipe;

public class RecipeManager extends IRecipeManager{

	private final File recipeFile = new File("plugins/FancyCrafting/recipes.yml");
	private final FileConfiguration recipeCfg = YamlConfiguration.loadConfiguration(recipeFile);

	private final File blacklistFile = new File("plugins/FancyCrafting/blacklist/recipes.yml");
	private final FileConfiguration blacklistCfg = YamlConfiguration.loadConfiguration(blacklistFile);
	
	private final Map<List<Integer>, IRecipe> blacklistedRecipes = new HashMap<>();
	
	private final FancyCrafting plugin;
	
	public RecipeManager(FancyCrafting plugin) throws IOException, InvalidConfigurationException {
		super(plugin);
		this.plugin = plugin;
		if (!recipeFile.exists())
			recipeFile.createNewFile();
		if(!blacklistFile.exists())
			blacklistFile.createNewFile();
		loadBukkitRecipes();
		loadCustomRecipes();
		loadBlacklistedRecipes();
	}

	public FileConfiguration getBlacklistRecipeFileCfg() {
		return blacklistCfg;
	}
	
	public File getBlacklistRecipeFile() {
		return blacklistFile;
	}
	
	@Override
	public void loadRecipes() {
		loadBukkitRecipes();
		loadCustomRecipes();
		loadBlacklistedRecipes();
	}
	
	@Override
	public void clear() {
		customRecipes.clear();
		autoMatchingRecipes.clear();
		recipesByName.clear();
		recipesByHash.clear();
	}
	
	public void loadBlacklistedRecipes() {
		plugin.getLogger().info("Loading blacklisted recipes...");
		try {
			blacklistCfg.load(blacklistFile);
		} catch (IOException | InvalidConfigurationException e1) {
			plugin.getLogger().log(Level.SEVERE, "Could not load blacklist file", e1);
			return;
		}
		blacklistedRecipes.clear();
		for (String key : blacklistCfg.getKeys(false)) {
			try {
				IRecipe recipe = IRecipe.getRecipeFromFile(blacklistFile, blacklistCfg, key);
				plugin.getLogger().fine("-----------------------------------------------------");
				printRecipe(recipe);
				plugin.getLogger().fine("-----------------------------------------------------");
				addBlacklistedRecipe(recipe);
			} catch (IOException | InvalidConfigurationException e) {
				plugin.getLogger().log(Level.SEVERE, "Could not load recipe with key " + key, e);
			}
		}
		plugin.getLogger().info("Loaded blacklisted recipes!");
	}
	
	public void addBlacklistedRecipe(IRecipe disabled) {
		blacklistedRecipes.put(disabled.getHashMatrix(), disabled);
		plugin.getLogger()
				.info("Loaded blacklisted recipe: " + disabled.getRecipeName() + " (" + disabled.getUUID() + ")");
	}

	public Set<List<Integer>> getBlacklistedRecipesHashes() {
		return blacklistedRecipes.keySet();
	}

	public Map<List<Integer>, IRecipe> getBlacklistedRecipes() {
		return blacklistedRecipes;
	}

	public boolean isBlacklisted(List<Integer> hashs) {
		if(blacklistedRecipes.containsKey(hashs))
			return true;
		hashs = hashs.stream().filter(i -> i != null).collect(Collectors.toList());
		Collections.sort(hashs);
		return blacklistedRecipes.containsKey(hashs);
	}
	
	@Override
	public void createRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String id, UUID uuid, int width,
			int height) throws InvalidRecipeException {
		saveRecipe(result, ingredients, shaped, id, uuid, width, height);
		reloadRecipes();
	}

	@Override
	public void saveRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String name, UUID uuid, int width,
			int height) throws InvalidRecipeException {
		try {
			recipeCfg.load(recipeFile);
			if (shaped)
				new IShapedRecipe(ingredients, width, height, result, name, uuid).saveToFile(recipeCfg, uuid.toString());
			else
				new IShapelessRecipe(Arrays.asList(ingredients), result, name, uuid).saveToFile(recipeCfg, uuid.toString());
			recipeCfg.save(recipeFile);
		} catch(Exception ex) {
			throw new InvalidRecipeException(ex);
		}
	}

	@Override
	public void saveRandomRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String name, UUID uuid,
			int width, int height, Map<ItemStack, Integer> rngMap) throws InvalidRecipeException {
		try {
			recipeCfg.load(recipeFile);
			if (shaped)
				new IRandomShapedRecipe(ingredients, width, height, result, name, uuid, rngMap).saveToFile(recipeCfg,
						uuid.toString());
			else
				new IRandomShapelessRecipe(Arrays.asList(ingredients), result, name, uuid, rngMap).saveToFile(recipeCfg,
						uuid.toString());
			recipeCfg.save(recipeFile);
		} catch(Exception ex) {
			throw new InvalidRecipeException(ex);
		}
	}

	public void loadCustomRecipes() {
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
				printRecipe(recipe);
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

	@Override
	public boolean isVanillaRecipeIncluded(IRecipe vanilla) {
		return true;
	}

	@Override
	public void delete(String recipeName) throws RecipeDeleteException {
		try {
			recipeCfg.load(recipeFile);
			recipeCfg.set(recipeName, null);
			recipeCfg.save(recipeFile);
			reloadRecipes();
		} catch(Exception ex) {
			throw new RecipeDeleteException(recipeName, ex);
		}
	}
}