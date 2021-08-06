package de.ancash.fancycrafting;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import de.ancash.fancycrafting.commands.FancyCraftingCommand;
import de.ancash.fancycrafting.gui.RecipeCreateGUI;
import de.ancash.fancycrafting.gui.RecipeEditGUI;
import de.ancash.fancycrafting.gui.RecipeViewGUI;
import de.ancash.fancycrafting.gui.WorkbenchGUI;
import de.ancash.fancycrafting.listeners.InventoryClickListener;
import de.ancash.fancycrafting.listeners.WorkbenchClickListener;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.misc.FileUtils;

public class FancyCrafting extends JavaPlugin{

	public static final String PREFIX = "[FancyCrafting] ";
	
	private static FancyCrafting singleton;
	private RecipeManager recipeManager;
	private WorkbenchGUI workbenchGUI;
	private RecipeCreateGUI recipeCreateGUI;
	private RecipeEditGUI recipeEditGUI;
	private RecipeViewGUI recipeViewGUI;
	
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
			recipeViewGUI = new RecipeViewGUI(this);
		} catch (InvalidConfigurationException | IOException | org.bukkit.configuration.InvalidConfigurationException e) {
			e.printStackTrace();
			return;
		}
		
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new InventoryClickListener(singleton), this);
		pm.registerEvents(new WorkbenchClickListener(singleton), this);
		
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

	public RecipeViewGUI getRecipeViewGUI() {
		return recipeViewGUI;
	}
	
	public void info(String str) {
		Bukkit.getLogger().info(PREFIX + str);
	}
	
	public void warn(String str) {
		Bukkit.getLogger().warning(PREFIX + str);
	}
	
	public void severe(String str) {
		Bukkit.getLogger().severe(PREFIX + str);
	}
}
