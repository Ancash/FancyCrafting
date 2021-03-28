package de.ancash.fancycrafting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.ancash.fancycrafting.commands.FancyCraftingCommand;
import de.ancash.fancycrafting.gui.RecipeCreateGUI;
import de.ancash.fancycrafting.gui.RecipeEditGUI;
import de.ancash.fancycrafting.gui.WorkbenchGUI;
import de.ancash.fancycrafting.listeners.InventoryClickListener;
import de.ancash.fancycrafting.listeners.WorkbenchClickListener;
import de.ancash.fancycrafting.utils.IRecipe;
import de.ancash.fancycrafting.utils.IShapedRecipe;
import de.ancash.fancycrafting.utils.IShapelessRecipe;
import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.misc.FileUtils;
import de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException;

public class FancyCrafting extends JavaPlugin{

	private static FancyCrafting singleton;
	private RecipeManager recipeManager;
	private WorkbenchGUI workbenchGUI;
	private RecipeCreateGUI recipeCreateGUI;
	private RecipeEditGUI recipeEditGUI;
	
	public void onEnable() {
		
		singleton = this;
		try {
			if(!new File("plugins/FancyCrafting/config.yml").exists()) 
				FileUtils.copyInputStreamToFile(getResource("resources/config.yml"), new File("plugins/FancyCrafting/config.yml"));
			if(!new File("plugins/FancyCrafting/recipes.yml").exists()) 
				new File("plugins/FancyCrafting/recipes.yml").createNewFile();
			recipeManager = new RecipeManager(this);
			workbenchGUI = new WorkbenchGUI(this);
			recipeCreateGUI = new RecipeCreateGUI(this);
			recipeEditGUI = new RecipeEditGUI(this);
		} catch (InvalidConfigurationException | IOException | org.bukkit.configuration.InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new InventoryClickListener(singleton), this);
		pm.registerEvents(new WorkbenchClickListener(singleton), this);
		
		//The ingredients, key is slot in the crafting table grid
		CompactMap<Integer, ItemStack> shapedIngredients = new CompactMap<>();
		//insert ingredient
		shapedIngredients.put(1, new ItemStack(Material.APPLE));
		//new recipe (shaped)
		IShapedRecipe shapedRecipe = new IShapedRecipe(shapedIngredients, new ItemStack(Material.GOLDEN_APPLE), UUID.randomUUID());
		//register recipe
		FancyCrafting.registerRecipe(shapedRecipe);
		
		//ingredients list (any collection)
		List<ItemStack> shapelessIngredients = new ArrayList<ItemStack>();
		shapelessIngredients.add(new ItemStack(Material.APPLE));
		shapelessIngredients.add(new ItemStack(Material.APPLE));
		//new recipe (shaped)
		IShapelessRecipe shapelessRecipe = new IShapelessRecipe(new ItemStack(Material.GOLDEN_APPLE), shapelessIngredients, UUID.randomUUID());
		//register recipe
		FancyCrafting.registerRecipe(shapelessRecipe);
		
		getCommand("fc").setExecutor(new FancyCraftingCommand(this));
	}
	
	public void onDisable() {
		workbenchGUI.closeAll();
	}
	
	public RecipeEditGUI getRecipeEditGUI() {
		return recipeEditGUI;
	}
	
	public WorkbenchGUI getWorkbenchGUI() {
		return workbenchGUI;
	}
	
	public RecipeManager getRecipeManager() {
		return recipeManager;
	}
	
	public RecipeCreateGUI getRecipeCreateGUI() {
		return recipeCreateGUI;
	}
	
	public static boolean registerRecipe(IRecipe recipe) {
		return singleton.getRecipeManager().registerRecipe(recipe);
	}
}
