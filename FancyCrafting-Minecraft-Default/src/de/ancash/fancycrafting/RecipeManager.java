package de.ancash.fancycrafting;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

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

@SuppressWarnings("nls")
public class RecipeManager extends IRecipeManager{

	private final File recipeFile = new File("plugins/FancyCrafting/recipes.yml");
	private final FileConfiguration recipeCfg = YamlConfiguration.loadConfiguration(recipeFile);

	private final FancyCrafting plugin;

	public RecipeManager(FancyCrafting plugin) throws IOException, InvalidConfigurationException {
		super(plugin);
		this.plugin = plugin;
		if (!recipeFile.exists())
			recipeFile.createNewFile();
		loadBukkitRecipes();
		loadCustomRecipes();
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

	@Override
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
				plugin.getLogger().fine("Name: " + recipe.getRecipeName());
				plugin.getLogger().fine("Result: " + recipe.getResult());
				plugin.getLogger().fine("Width: " + recipe.getWidth());
				plugin.getLogger().fine("Height: " + recipe.getHeight());
				plugin.getLogger().fine("Type: " + recipe.getClass().getSimpleName());
				plugin.getLogger()
						.fine("Ingredients: \n" + (recipe instanceof IShapedRecipe
								? String.join("\n",
										IRecipe.ingredientsToListColorless(plugin,
												recipe.getIngredients()
														.toArray(new ItemStack[recipe.getIngredients().size()]),
												recipe.getWidth(), recipe.getHeight(),
												plugin.getWorkspaceObjects().getViewIngredientsIdFormat()))
								: ((IShapelessRecipe) recipe).getIngredients()));
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
