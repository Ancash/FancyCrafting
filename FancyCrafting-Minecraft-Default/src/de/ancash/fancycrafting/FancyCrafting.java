package de.ancash.fancycrafting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.base.Response;
import de.ancash.fancycrafting.commands.FancyCraftingCommand;
import de.ancash.fancycrafting.gui.CraftingWorkspaceGUI;
import de.ancash.fancycrafting.gui.PagedRecipeViewGUI;
import de.ancash.fancycrafting.gui.RecipeCollectionViewGUI;
import de.ancash.fancycrafting.base.WorkspaceTemplate;
import de.ancash.fancycrafting.gui.normal.CreateNormalRecipeGUI;
import de.ancash.fancycrafting.gui.normal.EditNormalRecipeGUI;
import de.ancash.fancycrafting.gui.normal.ViewNormalRecipeGUI;
import de.ancash.fancycrafting.gui.random.CreateRandomRecipeGUI;
import de.ancash.fancycrafting.gui.random.EditRandomRecipeGUI;
import de.ancash.fancycrafting.gui.random.ViewRandomRecipeGUI;
import de.ancash.fancycrafting.listeners.WorkbenchClickListener;
import de.ancash.fancycrafting.recipe.IRandomRecipe;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.misc.MathsUtils;

@SuppressWarnings("nls")
public class FancyCrafting extends AbstractFancyCrafting {

	@Override
	public void reload() {
		long now = System.nanoTime();
		getLogger().info("Reloading...");
		HandlerList.unregisterAll(this);
		checkForUpdates();
		try {
			loadFiles();
		} catch (IOException | InvalidConfigurationException e) {
			getLogger().log(Level.SEVERE, "Could not load files", e);
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		recipeManager.clear();
		try {
			recipeManager = new RecipeManager(this);
		} catch (IOException | InvalidConfigurationException e) {
			getLogger().log(Level.SEVERE, "Could not load recipes", e);
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		response = new Response(this);
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new WorkbenchClickListener(this), this);

		getCommand("fc").setExecutor(new FancyCraftingCommand(this));
		System.gc();
		getLogger().info("Done! " + MathsUtils.round((System.nanoTime() - now) / 1000000000D, 3) + "s");
	}

	@Override
	public void load() {
		long now = System.nanoTime();
		getLogger().info("Loading...");
		checkForUpdates();
		try {
			loadFiles();
		} catch (IOException | InvalidConfigurationException e) {
			getLogger().log(Level.SEVERE, "Could not load files", e);
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		try {
			recipeManager = new RecipeManager(this);
		} catch (IOException | InvalidConfigurationException e) {
			getLogger().log(Level.SEVERE, "Could not load recipes", e);
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		response = new Response(this);
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new WorkbenchClickListener(this), this);

		getCommand("fc").setExecutor(new FancyCraftingCommand(this));
		
		getLogger().info("Done! " + MathsUtils.round((System.nanoTime() - now) / 1000000000D, 3) + "s");
	}

	@Override
	public void viewRecipe(Player player, Set<IRecipe> recipes) {
		if (recipes.size() == 1) {
			IRecipe recipe = recipes.stream().findFirst().get();
			if (recipe instanceof IRandomRecipe)
				new ViewRandomRecipeGUI(this, player, recipe).open();
			else
				new ViewNormalRecipeGUI(this, player, recipe).open();
			return;
		} else if (recipes.size() > 1) {
			new RecipeCollectionViewGUI(this, player, new ArrayList<>(recipes));
		}
	}

	@Override
	public void editRecipe(Player p, IRecipe r) {
		if (r instanceof IRandomRecipe)
			new EditRandomRecipeGUI(this, p, r).open();
		else
			new EditNormalRecipeGUI(this, p, r).open();
	}

	@Override
	public void createRandomRecipe(Player p, String name) {
		new CreateRandomRecipeGUI(this, p, name).open();
	}

	@Override
	public void createNormalRecipe(Player p, String name) {
		new CreateNormalRecipeGUI(this, p, name).open();
	}

	@Override
	public void openCraftingWorkspace(Player player, WorkspaceTemplate template) {
		new CraftingWorkspaceGUI(this, player, template);
	}

	@Override
	public void viewRecipesPaged(Player player, Set<IRecipe> recipes) {
		new PagedRecipeViewGUI(this, player,
				new ArrayList<>(getRecipeManager().getCustomRecipes()));
	}
}