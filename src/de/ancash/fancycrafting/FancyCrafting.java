package de.ancash.fancycrafting;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.ancash.fancycrafting.gui.WorkbenchGUI;
import de.ancash.fancycrafting.listeners.InventoryClickListener;
import de.ancash.fancycrafting.listeners.WorkbenchClickListener;
import de.ancash.ilibrary.misc.FileUtils;
import de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException;

public class FancyCrafting extends JavaPlugin{

	private FancyCrafting singleton;
	private RecipeManager recipeManager;
	private WorkbenchGUI workbenchGUI;
	
	public void onEnable() {
		singleton = this;
		try {
			if(!new File("plugins/FancyCrafting/config.yml").exists()) 
				FileUtils.copyInputStreamToFile(getResource("resources/config.yml"), new File("plugins/FancyCrafting/config.yml"));
			recipeManager = new RecipeManager(singleton);
			workbenchGUI = new WorkbenchGUI(singleton);
		} catch (InvalidConfigurationException | IOException e) {
			e.printStackTrace();
		}
		
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new InventoryClickListener(singleton), singleton);
		pm.registerEvents(new WorkbenchClickListener(singleton), singleton);
	}
	
	public void onDisable() {
		workbenchGUI.closeAll();
	}
	
	public WorkbenchGUI getWorkbenchGUI() {
		return workbenchGUI;
	}
	
	public RecipeManager getRecipeManager() {
		return recipeManager;
	}
}
