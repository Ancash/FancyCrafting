package de.ancash.fancycrafting;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.ancash.fancycrafting.commands.FancyCraftingCommand;
import de.ancash.fancycrafting.listeners.WorkbenchClickListener;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.VanillaRecipeMatcher;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.Metrics;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.updatechecker.UpdateCheckSource;
import de.ancash.minecraft.updatechecker.UpdateChecker;
import de.ancash.misc.FileUtils;

public class FancyCrafting extends JavaPlugin implements Listener{
	
	private final ItemStack invalid = XMaterial.RED_STAINED_GLASS_PANE.parseItem().clone();
	private final ItemStack valid = XMaterial.GREEN_STAINED_GLASS_PANE.parseItem().clone();
	private ItemStack background;
	private static FancyCrafting singleton;
	private final Map<UUID, VanillaRecipeMatcher> recipeMatcher = new HashMap<>();
	
	private RecipeManager recipeManager;
	private boolean permsForCustomRecipes;
	private boolean permsForVanillaRecipes;
	private int defaultTemplate;
	
	private ItemStack backItem;
	private ItemStack closeItem;
	private ItemStack prevItem;
	private ItemStack nextItem;
	
	private String createRecipeTitle;
	private String viewRecipeTitle;
	private List<String> backCommands;
	
	private FileConfiguration config;
	
	public void onEnable() {
		singleton = this;
		try {
			try {
				new Metrics(this, 14152, true);
				checkForUpdates();
			} catch(Throwable th) {
				warn("Please update ILibrary to v3.2.1 or higher!");
			}
			loadFiles();
			config = YamlConfiguration.loadConfiguration(new File("plugins/FancyCrafting/config.yml"));
			permsForCustomRecipes = config.getBoolean("perms-for-custom-recipes");
			permsForVanillaRecipes = config.getBoolean("perms-for-vanilla-recipes");
			recipeManager = new RecipeManager(this);
			loadConfig();
			Bukkit.getPluginManager().registerEvents(this, singleton);
			PluginManager pm = Bukkit.getServer().getPluginManager();
			pm.registerEvents(new WorkbenchClickListener(singleton), this);
			
			getCommand("fc").setExecutor(new FancyCraftingCommand(this));
			for(Player p : Bukkit.getOnlinePlayers())
				recipeMatcher.put(p.getUniqueId(), new VanillaRecipeMatcher(p));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	private void loadFiles() throws IOException {
		if(!new File("plugins/FancyCrafting/config.yml").exists()) 
			FileUtils.copyInputStreamToFile(getResource("resources/config.yml"), new File("plugins/FancyCrafting/config.yml"));
		if(!new File("plugins/FancyCrafting/recipes.yml").exists()) 
			new File("plugins/FancyCrafting/recipes.yml").createNewFile();
		info("Loading crafting templates:");
		for(int i = 1; i<= 6; i++) {
			File craftingTemplateFile = new File("plugins/FancyCrafting/crafting-" + i + "x" + i + ".yml");
			if(!craftingTemplateFile.exists())
				FileUtils.copyInputStreamToFile(getResource("resources/crafting-" + i +"x" + i + ".yml"), craftingTemplateFile);
			FileConfiguration craftingTemplateConfig = YamlConfiguration.loadConfiguration(craftingTemplateFile);
			CraftingTemplate.add(this, new CraftingTemplate(craftingTemplateConfig.getString("title")
					, craftingTemplateConfig.getInt("size")
					, craftingTemplateConfig.getInt("result-slot")
					, craftingTemplateConfig.getInt("close-slot")
					, craftingTemplateConfig.getInt("back-slot")
					, craftingTemplateConfig.getInt("prev-slot")
					, craftingTemplateConfig.getInt("next-slot")
					, craftingTemplateConfig.getInt("edit-slot")
					, craftingTemplateConfig.getInt("save-slot")
					, craftingTemplateConfig.getInt("delete-slot")
					, craftingTemplateConfig.getInt("recipe-type-slot")
					, craftingTemplateConfig.getIntegerList("crafting-slots").stream().mapToInt(Integer::intValue).toArray()
					, craftingTemplateConfig.getIntegerList("craft-state-slots").stream().mapToInt(Integer::intValue).toArray()), i);
		}
		info("Crafting templates loaded!");
	}
	
	private final int SPIGOT_RESOURCE_ID = 87300;
	
	private void checkForUpdates() {
		new UpdateChecker(this, UpdateCheckSource.SPIGOT, SPIGOT_RESOURCE_ID + "") 
			.setUsedVersion("v" + getDescription().getVersion())
			.setDownloadLink(SPIGOT_RESOURCE_ID)
			.setChangelogLink(SPIGOT_RESOURCE_ID)
            .setNotifyOpsOnJoin(true)
            .checkEveryXHours(6)
			.checkNow();
	}
	
	@SuppressWarnings("deprecation")
	public void loadConfig() throws IOException, org.bukkit.configuration.InvalidConfigurationException  {
		background = ItemStackUtils.get(config, "background");
		backItem = ItemStackUtils.get(config, "recipe-view-gui.back");
		closeItem = ItemStackUtils.get(config, "close");
		prevItem = ItemStackUtils.get(config, "recipe-view-gui.previous");
		nextItem = ItemStackUtils.get(config, "recipe-view-gui.next");
		defaultTemplate = config.getInt("default-template");
		createRecipeTitle = config.getString("recipe-create-gui.title");
		viewRecipeTitle = config.getString("recipe-view-gui.title");
		backCommands = Collections.unmodifiableList(config.getStringList("recipe-view-gui.back.commands"));
		info("Default crafting template is " + defaultTemplate + "x" + defaultTemplate);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent j) {
		recipeMatcher.put(j.getPlayer().getUniqueId(), new VanillaRecipeMatcher(j.getPlayer()));
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent q) {
		recipeMatcher.remove(q.getPlayer().getUniqueId());
	}
	
	public void onDisable() {
		recipeManager = null;
	}

	public RecipeManager getRecipeManager() {
		return recipeManager;
	}
	
	public static boolean permsForCustomRecipes() {
		return singleton.permsForCustomRecipes;
	}
	
	public static boolean permsForVanillaRecipes() {
		return singleton.permsForVanillaRecipes;
	}
	
	public static VanillaRecipeMatcher getVanillaRecipeMatcher(Player p) {
		return singleton.recipeMatcher.get(p.getUniqueId());
	}
	
	public static IRecipe matchVanillaRecipe(ItemStack[] ings, Player p) {
		return singleton.recipeManager.matchVanillaRecipe(ings, p);
	}
	
	public static boolean registerRecipe(IRecipe recipe) {
		return singleton.getRecipeManager().registerRecipe(recipe);
	}

	public void info(String str) {
		getLogger().info(str);
	}
	
	public void warn(String str) {
		getLogger().warning(str);
	}
	
	public void severe(String str) {
		getLogger().severe(str);
	}

	public ItemStack getValidItem() {
		return valid;
	}
	
	public ItemStack getInvalid() {
		return invalid;
	}
	
	public ItemStack getBackgroundItem() {
		return background;
	}

	public String getCreateRecipeTitle() {
		return createRecipeTitle;
	}

	public ItemStack getBackItem() {
		return backItem;
	}
	
	public ItemStack getCloseItem() {
		return closeItem;
	}

	public String getViewRecipeTitle() {
		return viewRecipeTitle;
	}

	public List<String> getBackCommands() {
		return backCommands;
	}

	public ItemStack getNextItem() {
		return nextItem;
	}

	public ItemStack getPrevItem() {
		return prevItem;
	}

	public int getDefaultTemplate() {
		return defaultTemplate;
	}
}
