package de.ancash.fancycrafting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.SkullMeta;

import de.ancash.fancycrafting.utils.IRecipe;
import de.ancash.fancycrafting.utils.IShapedRecipe;
import de.ancash.fancycrafting.utils.IShapelessRecipe;
import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.minecraft.nbt.NBTItem;
import de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException;

import static de.ancash.fancycrafting.utils.IShapedRecipe.*;

public class RecipeManager {
	
	private static final File file = new File("plugins/FancyCrafting/recipes.yml");
	private static final FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
	
	@SuppressWarnings("unused")
	private final FancyCrafting plugin;
	private final List<IRecipe> recipes = new ArrayList<IRecipe>();
	private final CompactMap<Integer, List<IRecipe>> recipesSortedBySize = new CompactMap<Integer, List<IRecipe>>();
	private final CompactMap<String, List<IRecipe>> recipesSortedByResult = new CompactMap<>();
	private List<IRecipe> customRecipes = new ArrayList<>();
	
	public RecipeManager(FancyCrafting plugin) throws InvalidConfigurationException, IOException, org.bukkit.configuration.InvalidConfigurationException {
		this.plugin = plugin;
		for(int i = 1; i<=9; i++) recipesSortedBySize.put(i, new ArrayList<IRecipe>());
		loadBukkitRecipes();
		loadFileRecipes();
		Collections.shuffle(recipes);
	}	
	
	public void shuffle() {
		Collections.shuffle(recipes);
	}
	
	public void updateRecipe(ItemStack result, CompactMap<Integer, ItemStack> ingredients, boolean shaped, UUID id) throws FileNotFoundException, IOException, org.bukkit.configuration.InvalidConfigurationException, InvalidConfigurationException {
		optimize(ingredients);
		saveRecipe(result, ingredients, shaped, id);
	}
	
	public void createRecipe(ItemStack result, CompactMap<Integer, ItemStack> ingredients, boolean shaped) throws FileNotFoundException, IOException, org.bukkit.configuration.InvalidConfigurationException, InvalidConfigurationException {
		optimize(ingredients);
		UUID id = UUID.randomUUID();
		saveRecipe(result, ingredients, shaped, id);
	}
	
	@SuppressWarnings("deprecation")
	public void saveRecipe(ItemStack result, CompactMap<Integer, ItemStack> ingredients, boolean shaped, UUID id) 
			throws FileNotFoundException, IOException, org.bukkit.configuration.InvalidConfigurationException, InvalidConfigurationException {
		fc.load(file);
		fc.set(id + "", null);
		result = result.clone();
		if(result.getType().equals(Material.SKULL_ITEM) && result.getData().getData() == 3) {
			String texture = null;
			try {
				texture = IRecipe.getTexure(result);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
					| IllegalAccessException e) {
				e.printStackTrace();
			}
			SkullMeta sm = (SkullMeta) result.getItemMeta();
			if(sm.getOwner() == null) sm.setOwner("Ancash");
			result.setItemMeta(sm);
			fc.set(id + ".result", result);
			if(texture != null) fc.set(id + ".result-texture", texture);
		} else {
			fc.set(id + ".result", result);
		}
		fc.set(id + ".shaped", shaped);
		ingredients.entrySet().forEach(entry -> {
			ItemStack is = entry.getValue().clone();
			if(is.getType().equals(Material.SKULL_ITEM) && is.getData().getData() == 3) {
				String texture = null;
				try {
					texture = IRecipe.getTexure(is);
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
						| IllegalAccessException e) {
					e.printStackTrace();
				}
				SkullMeta sm = (SkullMeta) is.getItemMeta();
				if(sm.getOwner() == null) sm.setOwner("Ancash");
				is.setItemMeta(sm);
				fc.set(id + ".ingredients." + entry.getKey(), is);
				if(texture != null) fc.set(id + ".ingredients." + entry.getKey() + "-texture", texture);
			} else {
				fc.set(id + ".ingredients." + entry.getKey(), is);
			}
		});
		fc.save(file);
		reloadRecipes();
	}
	
	private void reloadRecipes() throws InvalidConfigurationException, IOException, org.bukkit.configuration.InvalidConfigurationException {
		long now = System.currentTimeMillis();
		customRecipes = new ArrayList<>();
		System.out.println("Reloading Recipes!");
		for(int i = 1; i<=9; i++) recipesSortedBySize.put(i, new ArrayList<IRecipe>());
		loadBukkitRecipes();
		loadFileRecipes();
		Collections.shuffle(recipes);
		System.out.println("Reloaded! " + (System.currentTimeMillis() - now) + " ms");
	}
	
	private void loadFileRecipes() throws InvalidConfigurationException, IOException, org.bukkit.configuration.InvalidConfigurationException {
		fc.load(file);
		for(String key : fc.getKeys(false)) {
			ItemStack result = fc.getItemStack(key + ".result").clone();
			if(result == null) {
				System.err.println("Recipe result cannot be null! " + key);
				continue;
			}
			IRecipe recipe = getRecipeFromFile(fc, key);
			customRecipes.add(recipe);
			registerRecipe(recipe);
		}
		fc.save(file);
	}
	
	public IRecipe getRecipeFromFile(FileConfiguration fc, String key) {
		ItemStack result = fc.getItemStack(key + ".result").clone();
		if(result == null) {
			System.err.println("Recipe result cannot be null! " + key);
			return null;
		}
		if(fc.getString(key + ".result-texture") != null) result = IRecipe.setTexture(result, fc.getString(key + ".result-texture"));
		CompactMap<Integer, ItemStack> ingredientsMap = new CompactMap<>();
		for(int i = 1; i<=9; i++) {
			ItemStack temp = fc.getItemStack(key + ".ingredients." + i);
			if(temp == null) continue;
			ItemStack ingredient = temp.clone();
			if(fc.getString(key + ".ingredients." + i + "-texture") != null) ingredient = IRecipe.setTexture(ingredient, fc.getString(key + ".ingredients." + i + "-texture"));
			ingredientsMap.put(i, ingredient);
		}
		if(fc.getBoolean(key + ".shaped")) {	
			return new IShapedRecipe(ingredientsMap, result, UUID.fromString(key));
		} else {
			return new IShapelessRecipe(result, ingredientsMap.values(), UUID.fromString(key));
		}
	}
	
	private void loadBukkitRecipes() {
		Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
		Recipe recipe = null;
		while (recipeIterator.hasNext()){
			recipe = recipeIterator.next();
			IRecipe irecipe = IRecipe.toIRecipe(recipe);
			if(irecipe != null)
				registerRecipe(irecipe);
		}
	}
	
	public boolean registerRecipe(IRecipe recipe) {
		if(!(recipe instanceof IShapelessRecipe) && !(recipe instanceof IShapedRecipe)) return false;
		recipes.add(recipe);
		recipesSortedBySize.get(recipe.getIngredientsSize()).add(recipe);
		if(!recipesSortedByResult.containsKey(itemToString(recipe.getResult())))
			recipesSortedByResult.put(itemToString(recipe.getResult()), new ArrayList<>());
		recipesSortedByResult.get(itemToString(recipe.getResult())).add(recipe);
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public String itemToString(ItemStack is) {
		if(is.getType().equals(Material.SKULL_ITEM) && is.getData().getData() == 3) {
			ItemStack temp = is.clone();
			SkullMeta sm = (SkullMeta) temp.getItemMeta();
			sm.setOwner("Ancash");
			temp.setItemMeta(sm);
			return temp.toString().replace(", skull-owner=Ancash","");
		}
		return is.toString();
	}
	
	public IRecipe match(CompactMap<Integer, ItemStack> ingredientsMap, List<IRecipe> possibleRecipes) {
		optimize(ingredientsMap);
		if(ingredientsMap.isEmpty()) return null;

		if(possibleRecipes == null) return null;

		if(possibleRecipes.size() == 0) return null;
		for(IRecipe recipe : possibleRecipes) {
			if(recipe instanceof IShapedRecipe){
				if(IRecipe.matchesShaped(ingredientsMap, ((IShapedRecipe) recipe).getIngredientsMap(),
						possibleRecipes.size() == 1 && !new NBTItem(recipe.getResultWithId()).hasKey("fancycrafting.id"))) {
					return recipe;
				}
			}
			if(recipe instanceof IShapelessRecipe) {
				if(IRecipe.matchesShapeless(((IShapelessRecipe) recipe).getIngredientsList(), ingredientsMap.values(),
						possibleRecipes.size() == 1 && !new NBTItem(recipe.getResultWithId()).hasKey("fancycrafting.id"))) {
					return recipe;
				}
			}
		}
		return null;
	}
	
	public IRecipe match(CompactMap<Integer, ItemStack> ingredientsMap) {
		optimize(ingredientsMap);
		if(ingredientsMap.isEmpty()) return null;
		return match(ingredientsMap, getRecipes(ingredientsMap));
	}
	
	public List<IRecipe> getRecipes(CompactMap<Integer, ItemStack> ingredienstMap) {
		List<IRecipe> recipes = new ArrayList<>();
		Collection<ItemStack> ingredientsList = ingredienstMap.values();
		for(IRecipe recipe : recipesSortedBySize.get(ingredienstMap.size())) {
			if(recipe instanceof IShapedRecipe) {
				IShapedRecipe shaped = (IShapedRecipe) recipe;
				if(shaped.getIngredientsMap().size() != ingredienstMap.size()) continue;
				if(IRecipe.matchesShaped(ingredienstMap, shaped.getIngredientsMap(), true)) {
					recipes.add(recipe);
				}
			} else if(recipe instanceof IShapelessRecipe) {
				IShapelessRecipe shapeless = (IShapelessRecipe) recipe;
				if(shapeless.getIngredientsList().size() != ingredienstMap.size()) continue;
				if(IRecipe.matchesShapeless(shapeless.getIngredientsList(), ingredientsList, true)) {
					recipes.add(recipe);
				}
			}
		}
		return recipes;
	}
	
	public List<IRecipe> getCustomRecipes() {
		return customRecipes;
	}
	
	public List<IRecipe> getByResult(ItemStack result) {
		return recipesSortedByResult.get(itemToString(result));
	}

	public void delete(UUID fromString) throws FileNotFoundException, IOException, org.bukkit.configuration.InvalidConfigurationException, InvalidConfigurationException {
		fc.load(file);
		fc.set(fromString + "", null);
		fc.save(file);
		reloadRecipes();
	}
}
