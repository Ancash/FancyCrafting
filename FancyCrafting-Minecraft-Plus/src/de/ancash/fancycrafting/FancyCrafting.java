package de.ancash.fancycrafting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.base.IDefaultRecipeMatcherCallable;
import de.ancash.fancycrafting.commands.BlacklistSubCommand;
import de.ancash.fancycrafting.commands.FancyCraftingCommand;
import de.ancash.fancycrafting.gui.CraftingWorkspaceGUI;
import de.ancash.fancycrafting.base.gui.RecipeCollectionPagedViewGUI;
import de.ancash.fancycrafting.base.gui.SingleRecipePagedViewGUI;
import de.ancash.fancycrafting.base.gui.WorkspaceTemplate;
import de.ancash.fancycrafting.base.gui.manage.normal.CreateNormalRecipeGUI;
import de.ancash.fancycrafting.base.gui.manage.normal.EditNormalRecipeGUI;
import de.ancash.fancycrafting.base.gui.manage.normal.ViewNormalRecipeGUI;
import de.ancash.fancycrafting.base.gui.manage.random.CreateRandomRecipeGUI;
import de.ancash.fancycrafting.base.gui.manage.random.EditRandomRecipeGUI;
import de.ancash.fancycrafting.base.gui.manage.random.ViewRandomRecipeGUI;
import de.ancash.fancycrafting.recipe.IRandomRecipe;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.libs.org.apache.commons.io.FileUtils;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.misc.MathsUtils;

@SuppressWarnings("nls")
public class FancyCrafting extends AbstractFancyCrafting {
	
	private final File blacklistFile = new File("plugins/FancyCrafting/blacklist/config.yml");
	private final YamlConfiguration blacklistConfig = YamlConfiguration.loadConfiguration(blacklistFile);
	
	private String manageBlacklistTitle;
	private IItemStack addRecipeToBlacklistItem;
	private String addRecipeToBlacklistTitle;
	
	@Override
	public void reload() {
		long now = System.nanoTime();
		getLogger().info("Reloading...");
		checkForUpdates();
		try {
			loadFiles();
			loadBlacklistConfig();
		} catch (IOException | InvalidConfigurationException e) {
			getLogger().log(Level.SEVERE, "Could not load files", e);
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		loadListeners();
		recipeManager.clear();
		try {
			recipeManager = new RecipeManager(this);
		} catch (IOException | InvalidConfigurationException e) {
			getLogger().log(Level.SEVERE, "Could not load recipes", e);
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		System.gc();
		getLogger().info("Done! " + MathsUtils.round((System.nanoTime() - now) / 1000000000D, 3) + "s");
	}
	
	@SuppressWarnings("deprecation")
	private void loadBlacklistConfig() throws FileNotFoundException, IOException, InvalidConfigurationException {
		if (!blacklistFile.exists())
			FileUtils.copyInputStreamToFile(getResource("resources/blacklist-config.yml"),
					blacklistFile);
		super.checkFile(blacklistFile, "resources/blacklist-config.yml");
		blacklistConfig.load(blacklistFile);
		manageBlacklistTitle = blacklistConfig.getString("main-title");
		addRecipeToBlacklistItem = new IItemStack(ItemStackUtils.get(blacklistConfig, "add-recipe-to-blacklist.item"));
		addRecipeToBlacklistTitle = blacklistConfig.getString("add-recipe-to-blacklist.title");
	}

	@Override
	public void load() {
		long now = System.nanoTime();
		getLogger().info("Loading...");
		checkForUpdates();
		try {
			loadFiles();
			loadBlacklistConfig();
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
		
		getLogger().info("Done! " + MathsUtils.round((System.nanoTime() - now) / 1000000000D, 3) + "s");
	}

	@Override
	public void viewRecipeSingle(Player player, Set<IRecipe> recipes) {
		if (recipes.size() == 1) {
			IRecipe recipe = recipes.stream().findFirst().get();
			if (recipe instanceof IRandomRecipe)
				new ViewRandomRecipeGUI(this, player, recipe).open();
			else
				new ViewNormalRecipeGUI(this, player, recipe).open();
			return;
		} else if (recipes.size() > 1) {
			new SingleRecipePagedViewGUI(this, player, new ArrayList<>(recipes));
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
	public void openCreateRandomRecipe(Player p, String name) {
		new CreateRandomRecipeGUI(this, p, name).open();
	}

	@Override
	public void openCreateNormalRecipe(Player p, String name) {
		new CreateNormalRecipeGUI(this, p, name).open();
	}

	@Override
	public void openCraftingWorkspace(Player player, WorkspaceTemplate template) {
		new CraftingWorkspaceGUI(this, player, template);
	}

	@Override
	public void viewRecipeCollection(Player player, Set<IRecipe> recipes) {
		new RecipeCollectionPagedViewGUI(this, player,
				new ArrayList<>(getRecipeManager().getCustomRecipes()));
	}

	@Override
	public RecipeManager getRecipeManager() {
		return (RecipeManager) super.getRecipeManager();
	}
	
	public void addRecipeToBlacklist(IRecipe recipe) throws IOException {
		recipe.saveToFile(getRecipeManager().getBlacklistRecipeFileCfg(), recipe.getUUID().toString());
		getRecipeManager().getBlacklistRecipeFileCfg().save(getRecipeManager().getBlacklistRecipeFile());
		getRecipeManager().loadBlacklistedRecipes();
	}
	
	public void removeBlacklistedRecipe(IRecipe recipe) throws IOException {
		getRecipeManager().getBlacklistRecipeFileCfg().set(recipe.getUUID().toString(), null);
		getRecipeManager().getBlacklistRecipeFileCfg().save(getRecipeManager().getBlacklistRecipeFile());
		getRecipeManager().loadBlacklistedRecipes();
	}
	
	public String getManageBlacklistTitle() {
		return manageBlacklistTitle;
	}

	public IItemStack getAddRecipeToBlacklistItem() {
		return addRecipeToBlacklistItem;
	}

	public String getAddRecipeToBlacklistTitle() {
		return addRecipeToBlacklistTitle;
	}

	@Override
	public void loadSubCommands(FancyCraftingCommand fc) {
		fc.addSubCommand(new BlacklistSubCommand(this, "blacklist", "bl"));
	}

	@Override
	public IDefaultRecipeMatcherCallable newDefaultRecipeMatcher(Player player) {
		return new DefaultRecipeMatcherCallable(this, player);
	}
}