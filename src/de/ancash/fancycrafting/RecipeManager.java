package de.ancash.fancycrafting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import de.ancash.fancycrafting.utils.IRecipe;
import de.ancash.fancycrafting.utils.IShapedRecipe;
import de.ancash.fancycrafting.utils.IShapelessRecipe;
import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException;

import static de.ancash.fancycrafting.utils.IShapedRecipe.*;

public class RecipeManager {
	
	@SuppressWarnings("unused")
	private final FancyCrafting plugin;
	private final List<IRecipe> recipes = new ArrayList<IRecipe>();
	private final CompactMap<Integer, List<IRecipe>> recipesSortedBySize = new CompactMap<Integer, List<IRecipe>>();
	private final CompactMap<String, List<IRecipe>> recipesSortedByResult = new CompactMap<>();
	
	public RecipeManager(FancyCrafting plugin) throws InvalidConfigurationException, IOException {
		this.plugin = plugin;
		for(int i = 1; i<=9; i++) recipesSortedBySize.put(i, new ArrayList<IRecipe>());
		
		loadBukkitRecipes();
		loadFileRecipes();
		
		Collections.shuffle(recipes);
	}	
	
	private void loadFileRecipes() throws InvalidConfigurationException, IOException {
		File file = new File("plugins/FancyCrafting/recipes.yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		for(String key : fc.getKeys(false)) {
			ItemStack result = fc.getItemStack(key + ".result");
			if(result == null) {
				System.err.println("Recipe result cannot be null! " + key);
				continue;
			}
			CompactMap<Integer, ItemStack> ingredientsMap = new CompactMap<>();
			for(int i = 1; i<=9; i++) {
				ItemStack ingredient = fc.getItemStack(key + ".ingredients." + i);
				if(ingredient == null) continue;
				ingredientsMap.put(i, ingredient);
			}
			if(fc.getBoolean(key + ".shaped")) {	
				registerRecipe(new IShapedRecipe(ingredientsMap, result));
			} else {
				registerRecipe(new IShapelessRecipe(result, ingredientsMap.values()));
			}
			
		}
		fc.save(file);
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
	
	public String itemToString(ItemStack is) {
		return is.toString();
	}
	
	public IRecipe match(CompactMap<Integer, ItemStack> ingredientsMap, List<IRecipe> possibleRecipes) {
		optimize(ingredientsMap);
		if(ingredientsMap.isEmpty()) return null;

		if(possibleRecipes == null) return null;
		if(possibleRecipes.size() == 0) return null;
		if(possibleRecipes.size() == 1) return possibleRecipes.iterator().next();
		for(IRecipe recipe : possibleRecipes) {
			if(recipe instanceof IShapedRecipe) {
				if(IRecipe.matchesShaped(ingredientsMap, ((IShapedRecipe) recipe).getIngredientsMap(),
						possibleRecipes.size() == 1)) {
					return recipe;
				}
			}
			if(recipe instanceof IShapelessRecipe) {
				if(IRecipe.matchesShapeless(((IShapelessRecipe) recipe).getIngredientsList(), ingredientsMap.values(),
						possibleRecipes.size() == 1 )) {
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
		
		for(IRecipe recipe : recipesSortedBySize.get(ingredienstMap.size())) {
			if(recipe instanceof IShapedRecipe) {
				IShapedRecipe shaped = (IShapedRecipe) recipe;
				if(shaped.getIngredientsMap().size() != ingredienstMap.size()) continue;
				if(IRecipe.matchesShaped(ingredienstMap, shaped.getIngredientsMap(), true)) {
					recipes.add(recipe);
				}
			}
			if(recipe instanceof IShapelessRecipe) {
				IShapelessRecipe shapeless = (IShapelessRecipe) recipe;
				if(shapeless.getIngredientsList().size() != ingredienstMap.size()) continue;
				if(IRecipe.matchesShapeless(shapeless.getIngredientsList(), ingredienstMap.values(), true)) {
					recipes.add(recipe);
				}
			}
		}
		return recipes;
	}
	
	public List<IRecipe> getByResult(ItemStack result) {
		return recipesSortedByResult.get(itemToString(result));
	}
}
