package de.ancash.fancycrafting;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.minecraft.ItemBuilder;
import de.ancash.minecraft.SerializableItemStack;
import de.ancash.minecraft.XMaterial;

public class RecipeManager {
	
	private final YamlFile recipeFile = new YamlFile("plugins/FancyCrafting/recipes.yml");
	
	private final FancyCrafting plugin;
	private final List<IRecipe> recipes = new ArrayList<IRecipe>();
	private final Map<Integer, List<IRecipe>> recipesSortedBySize = new HashMap<Integer, List<IRecipe>>();
	private final Map<SerializableItemStack, IRecipe> recipeSortedByResult = new HashMap<>();
	private List<IRecipe> customRecipes = new ArrayList<>();
	private final Map<String, IRecipe> customRecipesByName = new HashMap<>();
	
	public RecipeManager(FancyCrafting plugin) throws IOException, InvalidConfigurationException {
		this.plugin = plugin;
		for(int i = 1; i<=9; i++) recipesSortedBySize.put(i, new ArrayList<IRecipe>());
		loadBukkitRecipes();
		loadCustomRecipes();
		
		addMissingRecipes();
		
		Collections.shuffle(recipes);
	}	

	private final BiConsumer<Collection<ItemStack>, ItemStack> REGISTER_SHAPELESS_RECIPE = (ings, result) -> registerRecipe(new IShapelessRecipe(result, ings.stream().map(SerializableItemStack::new).collect(Collectors.toList()), null));
	
	private void addMissingRecipes() {
		if(XMaterial.STRIPPED_ACACIA_LOG.isSupported()) {
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.STRIPPED_ACACIA_LOG, 1, XMaterial.STRIPPED_ACACIA_LOG.getData()).build()), new ItemBuilder(XMaterial.ACACIA_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.STRIPPED_BIRCH_LOG, 1, XMaterial.STRIPPED_BIRCH_LOG.getData()).build()), new ItemBuilder(XMaterial.BIRCH_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.STRIPPED_DARK_OAK_LOG, 1, XMaterial.STRIPPED_DARK_OAK_LOG.getData()).build()), new ItemBuilder(XMaterial.DARK_OAK_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.STRIPPED_JUNGLE_LOG, 1, XMaterial.STRIPPED_JUNGLE_LOG.getData()).build()), new ItemBuilder(XMaterial.JUNGLE_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.STRIPPED_OAK_LOG, 1, XMaterial.STRIPPED_OAK_LOG.getData()).build()), new ItemBuilder(XMaterial.OAK_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.STRIPPED_SPRUCE_LOG, 1, XMaterial.STRIPPED_SPRUCE_LOG.getData()).build()), new ItemBuilder(XMaterial.SPRUCE_PLANKS, 4).build());
		}
		
		if(XMaterial.ACACIA_WOOD.isSupported()) {
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.ACACIA_WOOD, 1, XMaterial.ACACIA_WOOD.getData()).build()), new ItemBuilder(XMaterial.ACACIA_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.BIRCH_WOOD, 1, XMaterial.BIRCH_WOOD.getData()).build()), new ItemBuilder(XMaterial.BIRCH_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.DARK_OAK_WOOD, 1, XMaterial.DARK_OAK_WOOD.getData()).build()), new ItemBuilder(XMaterial.DARK_OAK_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.JUNGLE_WOOD, 1, XMaterial.JUNGLE_WOOD.getData()).build()), new ItemBuilder(XMaterial.JUNGLE_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.OAK_WOOD, 1, XMaterial.OAK_WOOD.getData()).build()), new ItemBuilder(XMaterial.OAK_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.SPRUCE_WOOD, 1, XMaterial.SPRUCE_WOOD.getData()).build()), new ItemBuilder(XMaterial.SPRUCE_PLANKS, 4).build());
		}
		
		if(XMaterial.STRIPPED_ACACIA_WOOD.isSupported()) {
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.STRIPPED_ACACIA_WOOD, 1, XMaterial.STRIPPED_ACACIA_WOOD.getData()).build()), new ItemBuilder(XMaterial.ACACIA_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.STRIPPED_BIRCH_WOOD, 1, XMaterial.STRIPPED_BIRCH_WOOD.getData()).build()), new ItemBuilder(XMaterial.BIRCH_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.STRIPPED_DARK_OAK_WOOD, 1, XMaterial.STRIPPED_DARK_OAK_WOOD.getData()).build()), new ItemBuilder(XMaterial.DARK_OAK_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.STRIPPED_JUNGLE_WOOD, 1, XMaterial.STRIPPED_JUNGLE_WOOD.getData()).build()), new ItemBuilder(XMaterial.JUNGLE_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.STRIPPED_OAK_WOOD, 1, XMaterial.STRIPPED_OAK_WOOD.getData()).build()), new ItemBuilder(XMaterial.OAK_PLANKS, 4).build());
			REGISTER_SHAPELESS_RECIPE.accept(Arrays.asList(new ItemBuilder(XMaterial.STRIPPED_SPRUCE_WOOD, 1, XMaterial.STRIPPED_SPRUCE_WOOD.getData()).build()), new ItemBuilder(XMaterial.SPRUCE_PLANKS, 4).build());
		}
	}
	
	public void shuffle() {
		Collections.shuffle(recipes);
	}
	
	public void updateRecipe(SerializableItemStack result, Map<Integer, SerializableItemStack> ingredients, boolean shaped, String id) throws FileNotFoundException, IOException, InvalidConfigurationException {
		IRecipe.optimize(ingredients);
		saveRecipe(result, ingredients, shaped, id);
		reloadRecipes();
	}
	
	public void createRecipe(SerializableItemStack result, Map<Integer, SerializableItemStack> ingredients, boolean shaped, String id) throws FileNotFoundException, IOException, InvalidConfigurationException{
		if(customRecipesByName.containsKey(id)) {
			plugin.warn("Duplicated recipe name: " + id);
			return;
		}
		IRecipe.optimize(ingredients);
		saveRecipe(result, ingredients, shaped, id);
		reloadRecipes();
	}
	
	public boolean exists(String recipeName) {
		return customRecipesByName.containsKey(recipeName) || customRecipesByName.containsKey(recipeName.replace(" ", "-")) || customRecipesByName.containsKey(recipeName.replace("-", " "));
	}
	
	public void saveRecipe(SerializableItemStack result, Map<Integer, SerializableItemStack> ingredients, boolean shaped, String id) throws IOException, InvalidConfigurationException  {
		recipeFile.createOrLoad();
		id = id.replace(" ", "-");
		recipeFile.set(id + "", null);
		recipeFile.set(id + ".result", result.asBase64());
		recipeFile.set(id + ".shaped", shaped);
		for(Entry<Integer, SerializableItemStack> entry : ingredients.entrySet()) 
			recipeFile.set(id + ".ingredients." + entry.getKey(), entry.getValue().asBase64());
		
		recipeFile.save();
	}
	
	private void reloadRecipes() {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				plugin.info("Reloading Recipes!");
				long now = System.currentTimeMillis();
				customRecipes.clear();
				customRecipesByName.clear();
				for(int i = 1; i<=9; i++) recipesSortedBySize.put(i, new ArrayList<IRecipe>());
				loadBukkitRecipes();
				try {
					loadCustomRecipes();
				} catch (IOException | InvalidConfigurationException e) {
					plugin.severe("Could not load custom recipes from file:");
					e.printStackTrace();
				}
				Collections.shuffle(recipes);
				plugin.info("Reloaded! " + (System.currentTimeMillis() - now) + " ms");
			}
		}.runTaskAsynchronously(plugin);
	}
	
	private void loadCustomRecipes() throws IOException, InvalidConfigurationException {
		plugin.info("Loading custom recipes...");
		recipeFile.createOrLoad();
		for(String key : recipeFile.getKeys(false)) {
			try {
				registerRecipe(getRecipeFromFile( key));
				plugin.info("Loaded custom recipe: " + key);
			} catch (ClassNotFoundException | IOException e) {
				System.err.println("Could not load recipe:");
				e.printStackTrace();
			}
		}
		recipeFile.save();
		plugin.info("Loaded custom recipes!");
	}
	
	public IRecipe getRecipeFromFile(String key) throws ClassNotFoundException, IOException {
		SerializableItemStack result = SerializableItemStack.fromBase64(recipeFile.getString(key + ".result"));

		Map<Integer, SerializableItemStack> ingredientsMap = new HashMap<>();
		for(int i = 1; i<=9; i++) {
			if(recipeFile.getString(key + ".ingredients." + i) == null) continue;
			ingredientsMap.put(i, SerializableItemStack.fromBase64(recipeFile.getString(key + ".ingredients." + i)));
		}
		if(recipeFile.getBoolean(key + ".shaped")) {	
			return new IShapedRecipe(result, ingredientsMap, key);
		} else {
			return new IShapelessRecipe(result.restore(), ingredientsMap.values(), key);
		}
	}
	
	private void loadBukkitRecipes() {
		Bukkit.recipeIterator().forEachRemaining(r -> registerRecipe(IRecipe.toIRecipe(r)));
	}
	
	public boolean registerRecipe(IRecipe recipe) {
		if(recipe == null) return false;
		if(recipe.getName() != null && customRecipesByName.containsKey(recipe.getName())) {
			System.err.println("Duplicated recipe name: " + recipe.getName());
			return false;
		}
		if(recipe.getName() != null) {
			customRecipes.add(recipe);
			customRecipesByName.put(recipe.getName(), recipe);
			recipeSortedByResult.put(recipe.getSerializedResult(), recipe);
		}
		recipes.add(recipe);
		recipesSortedBySize.get(recipe.getIngredientsSize()).add(recipe);
		return true;
	}
	
	public IRecipe getRecipeByResult(SerializableItemStack s) {
		if(s == null) return null;
		for(Entry<SerializableItemStack, IRecipe> entry : recipeSortedByResult.entrySet()) 
			if(s.equals(entry.getKey())) return entry.getValue();
		
		return null;
	}

	
	public IRecipe match(Map<Integer, SerializableItemStack> ingredientsMap) {
		if(ingredientsMap.isEmpty()) return null;
		return getRecipe(ingredientsMap);
	}
	
	public IRecipe getRecipe(Map<Integer, SerializableItemStack> ingredienstMap) {
		Collection<SerializableItemStack> ingredientsList = ingredienstMap.values();
		for(IRecipe recipe : recipesSortedBySize.get(ingredienstMap.size())) {
			if(recipe instanceof IShapedRecipe) {
				IShapedRecipe shaped = (IShapedRecipe) recipe;
				if(shaped.getIngredientsMap().size() != ingredienstMap.size()) continue;
				if(IRecipe.matchesShaped(shaped.getIngredientsMap(), ingredienstMap)) {
					return recipe;
				}
			} else if(recipe instanceof IShapelessRecipe) {
				IShapelessRecipe shapeless = (IShapelessRecipe) recipe;
				if(shapeless.getIngredients().size() != ingredienstMap.size()) continue;
				if(IRecipe.matchesShapeless(shapeless.getIngredients(), ingredientsList)) {
					return recipe;
				}
			}
		}
		return null;
	}
	
	public IRecipe getCustomRecipe(String name) {
		return customRecipesByName.get(name);
	}
	
	public List<IRecipe> getCustomRecipes() {
		return customRecipes;
	}

	public void delete(String fromString) throws FileNotFoundException, IOException, InvalidConfigurationException {
		recipeFile.createOrLoad();
		recipeFile.set(fromString + "", null);
		recipeFile.save();
		reloadRecipes();
	}
}
