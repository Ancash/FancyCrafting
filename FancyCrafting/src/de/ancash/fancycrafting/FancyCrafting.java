package de.ancash.fancycrafting;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

import de.ancash.libs.org.apache.commons.io.FileUtils;
import de.ancash.libs.org.simpleyaml.configuration.file.YamlFile;

import de.ancash.fancycrafting.commands.FancyCraftingCommand;
import de.ancash.fancycrafting.gui.WorkspaceDimension;
import de.ancash.fancycrafting.gui.WorkspaceSlotsBuilder;
import de.ancash.fancycrafting.gui.WorkspaceTemplate;
import de.ancash.fancycrafting.listeners.WorkbenchClickListener;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.VanillaRecipeMatcher;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.Metrics;
import de.ancash.minecraft.updatechecker.UpdateCheckSource;
import de.ancash.minecraft.updatechecker.UpdateChecker;

/**
 * 
 */
public class FancyCrafting extends JavaPlugin implements Listener {

	private final ExecutorService threadPool = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
	private static FancyCrafting singleton;
	private final Map<UUID, VanillaRecipeMatcher> recipeMatcher = new HashMap<>();
	private Response response;
	private UpdateChecker updateChecker;

	private RecipeManager recipeManager;
	private boolean checkRecipesAsync;
	private boolean checkQuickCraftingAsync;
	private boolean permsForCustomRecipes;
	private boolean permsForVanillaRecipes;
	private WorkspaceDimension defaultDim;

	private ItemStack backItem;
	private IItemStack closeItem;
	private ItemStack prevItem;
	private ItemStack nextItem;
	private IItemStack invalid;
	private IItemStack valid;
	private IItemStack background;
	private ItemStack shapeless;
	private ItemStack shaped;
	private ItemStack save;
	private ItemStack edit;
	private ItemStack delete;
	private ItemStack quickCrafting;
	private String createRecipeTitle;
	private String customRecipesTitle;
	private String viewRecipeTitle;
	private String editRecipeTitle;
	private List<String> backCommands;

	private FileConfiguration config;

	public void onEnable() {
		singleton = this;
		try {
			new Metrics(this, 14152, true);
			checkForUpdates();
			loadFiles();
			config = YamlConfiguration.loadConfiguration(new File("plugins/FancyCrafting/config.yml"));
			recipeManager = new RecipeManager(this);
			loadConfig();
			response = new Response(this);
			Bukkit.getPluginManager().registerEvents(this, singleton);
			PluginManager pm = Bukkit.getServer().getPluginManager();
			pm.registerEvents(new WorkbenchClickListener(singleton), this);

			getCommand("fc").setExecutor(new FancyCraftingCommand(this));
			for (Player p : Bukkit.getOnlinePlayers())
				recipeMatcher.put(p.getUniqueId(), new VanillaRecipeMatcher(this, p));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private void loadFiles() throws IOException {
		if (!new File("plugins/FancyCrafting/config.yml").exists())
			FileUtils.copyInputStreamToFile(getResource("resources/config.yml"),
					new File("plugins/FancyCrafting/config.yml"));

		checkFile(new File("plugins/FancyCrafting/config.yml"), "resources/config.yml");

		if (!new File("plugins/FancyCrafting/recipes.yml").exists())
			new File("plugins/FancyCrafting/recipes.yml").createNewFile();
		getLogger().info("Loading crafting templates:");
		for (int width = 1; width <= 8; width++) {
			for (int height = 1; height <= 6; height++) {
				try {
					File craftingTemplateFile = new File(
							"plugins/FancyCrafting/crafting-" + width + "x" + height + ".yml");
					if (!craftingTemplateFile.exists()) {
						if (getResource("resources/crafting-" + width + "x" + height + ".yml") == null)
							throw new NullPointerException();
						FileUtils.copyInputStreamToFile(
								getResource("resources/crafting-" + width + "x" + height + ".yml"),
								craftingTemplateFile);
					}
					checkFile(craftingTemplateFile, "resources/crafting-" + width + "x" + height + ".yml");

					FileConfiguration craftingTemplateConfig = YamlConfiguration
							.loadConfiguration(craftingTemplateFile);
					WorkspaceTemplate.add(this, new WorkspaceTemplate(craftingTemplateConfig.getString("title"),
							new WorkspaceDimension(width, height, craftingTemplateConfig.getInt("size")),
							new WorkspaceSlotsBuilder().setResultSlot(craftingTemplateConfig.getInt("result-slot"))
									.setCloseSlot(craftingTemplateConfig.getInt("close-slot"))
									.setBackSlot(craftingTemplateConfig.getInt("back-slot"))
									.setPrevSlot(craftingTemplateConfig.getInt("prev-slot"))
									.setNextSlot(craftingTemplateConfig.getInt("next-slot"))
									.setEditSlot(craftingTemplateConfig.getInt("edit-slot"))
									.setSaveSlot(craftingTemplateConfig.getInt("save-slot"))
									.setDeleteSlot(craftingTemplateConfig.getInt("delete-slot"))
									.setRecipeTypeSlot(craftingTemplateConfig.getInt("recipe-type-slot"))
									.setCraftingSlots(craftingTemplateConfig.getIntegerList("crafting-slots").stream()
											.mapToInt(Integer::intValue).toArray())
									.setCraftStateSlots(craftingTemplateConfig.getIntegerList("craft-state-slots")
											.stream().mapToInt(Integer::intValue).toArray())
									.setAutoCraftingSlots(craftingTemplateConfig.getIntegerList("quick-crafting-slots")
											.stream().mapToInt(Integer::intValue).toArray())
									.build()));
					getLogger().info(String.format("Loaded %dx%d crafting template", width, height));
				} catch (Exception ex) {
					getLogger().warning(String.format("Could not load %dx%d crafting template!", width, height));
				}
			}
		}
		getLogger().info("Crafting templates loaded!");
	}

	public void checkFile(File file, String src)
			throws de.ancash.libs.org.simpleyaml.exceptions.InvalidConfigurationException, IllegalArgumentException,
			IOException {
		getLogger().info("Checking " + file.getPath() + " for completeness (comparing to " + src + ")");
		de.ancash.misc.FileUtils.setMissingConfigurationSections(new YamlFile(file), getResource(src),
				new HashSet<>(Arrays.asList("type")));
	}

	private final int SPIGOT_RESOURCE_ID = 87300;

	private void checkForUpdates() {
		updateChecker = new UpdateChecker(this, UpdateCheckSource.SPIGOT, SPIGOT_RESOURCE_ID + "")
				.setUsedVersion("v" + getDescription().getVersion()).setDownloadLink(SPIGOT_RESOURCE_ID)
				.setChangelogLink(SPIGOT_RESOURCE_ID).setNotifyOpsOnJoin(true).checkEveryXHours(6).checkNow();
	}

	@SuppressWarnings("deprecation")
	public void loadConfig() throws IOException, org.bukkit.configuration.InvalidConfigurationException {
		background = new IItemStack(ItemStackUtils.get(config, "background"));
		backItem = ItemStackUtils.get(config, "recipe-view-gui.back");
		closeItem = new IItemStack(ItemStackUtils.get(config, "close"));
		prevItem = ItemStackUtils.get(config, "recipe-view-gui.previous");
		nextItem = ItemStackUtils.get(config, "recipe-view-gui.next");
		valid = new IItemStack(ItemStackUtils.get(config, "workbench.valid_recipe"));
		invalid = new IItemStack(ItemStackUtils.get(config, "workbench.invalid_recipe"));
		shapeless = ItemStackUtils.get(config, "recipe-create-gui.shapeless");
		shaped = ItemStackUtils.get(config, "recipe-create-gui.shaped");
		save = ItemStackUtils.get(config, "recipe-create-gui.save");
		edit = ItemStackUtils.get(config, "recipe-create-gui.edit");
		delete = ItemStackUtils.get(config, "recipe-create-gui.delete");
		quickCrafting = ItemStackUtils.get(config, "workbench.quick_crafting");
		defaultDim = new WorkspaceDimension(config.getInt("default-template-width"),
				config.getInt("default-template-height"));
		createRecipeTitle = config.getString("recipe-create-gui.title");
		customRecipesTitle = config.getString("recipe-view-gui.page-title");
		viewRecipeTitle = config.getString("recipe-view-gui.single-title");
		editRecipeTitle = config.getString("recipe-view-gui.edit-title");
		backCommands = Collections.unmodifiableList(config.getStringList("recipe-view-gui.back.commands"));
		permsForCustomRecipes = config.getBoolean("perms-for-custom-recipes");
		permsForVanillaRecipes = config.getBoolean("perms-for-vanilla-recipes");
		checkRecipesAsync = config.getBoolean("check-recipes-async");
		checkQuickCraftingAsync = config.getBoolean("check-quick-crafting-async");
		getLogger().info("Check recipes async: " + checkRecipesAsync);
		getLogger().info("Check quick crafting async: " + checkQuickCraftingAsync);
		getLogger().info("Default crafting template is " + defaultDim.getWidth() + "x" + defaultDim.getHeight());
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent j) {
		recipeMatcher.put(j.getPlayer().getUniqueId(), new VanillaRecipeMatcher(this, j.getPlayer()));
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent q) {
		recipeMatcher.remove(q.getPlayer().getUniqueId());
	}

	@Override
	public void onDisable() {
		updateChecker.stop();
		updateChecker = null;
		recipeManager = null;
		threadPool.shutdownNow();
	}

	public void submit(Runnable r) {
		threadPool.submit(r);
	}

	public <T> Future<T> submit(Callable<T> call) {
		return threadPool.submit(call);
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

	public static boolean registerRecipe(IRecipe recipe) {
		return singleton.getRecipeManager().registerRecipe(recipe);
	}

	public IItemStack getValidItem() {
		return valid;
	}

	public IItemStack getInvalidItem() {
		return invalid;
	}

	public IItemStack getBackgroundItem() {
		return background;
	}

	public String getCreateRecipeTitle() {
		return createRecipeTitle;
	}

	public ItemStack getBackItem() {
		return backItem;
	}

	public IItemStack getCloseItem() {
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

	public WorkspaceDimension getDefaultDimension() {
		return defaultDim;
	}

	public ItemStack getShapelessItem() {
		return shapeless;
	}

	public ItemStack getShapedItem() {
		return shaped;
	}

	public ItemStack getSaveItem() {
		return save;
	}

	public ItemStack getEditItem() {
		return edit;
	}

	public ItemStack getDeleteItem() {
		return delete;
	}

	public boolean checkRecipesAsync() {
		return checkRecipesAsync;
	}

	public String getCustomRecipesTitle() {
		return customRecipesTitle;
	}

	public String getEditRecipeTitle() {
		return editRecipeTitle;
	}

	public Response getResponse() {
		return response;
	}

	public ItemStack getQuickCraftingItem() {
		return quickCrafting;
	}

	public boolean checkQuickCraftingAsync() {
		return checkQuickCraftingAsync;
	}
}
