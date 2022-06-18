package de.ancash.fancycrafting;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.Metrics;
import de.ancash.minecraft.updatechecker.UpdateCheckSource;
import de.ancash.minecraft.updatechecker.UpdateChecker;

/**
 * 
 */
public class FancyCrafting extends JavaPlugin {

	private final ExecutorService threadPool = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
	private static FancyCrafting singleton;
	
	private final int resourceId = 87300;
	private Response response;
	private UpdateChecker updateChecker;

	private RecipeManager recipeManager;
	private boolean checkRecipesAsync;
	private boolean quickCraftingAsync;
	private boolean permsForCustomRecipes;
	private boolean permsForVanillaRecipes;
	private WorkspaceDimension defaultDim;

	private ItemStack backItem;
	private IItemStack closeItem;
	private ItemStack prevItem;
	private ItemStack nextItem;
	private IItemStack invalidItem;
	private IItemStack validItem;
	private IItemStack backgroundItem;
	private ItemStack shapeless;
	private ItemStack shapedItem;
	private ItemStack saveItem;
	private ItemStack editItem;
	private ItemStack deleteItem;
	private ItemStack quickCraftingItem;
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
			PluginManager pm = Bukkit.getServer().getPluginManager();
			pm.registerEvents(new WorkbenchClickListener(singleton), this);

			getCommand("fc").setExecutor(new FancyCraftingCommand(this));
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

	private void checkForUpdates() {
		updateChecker = new UpdateChecker(this, UpdateCheckSource.SPIGOT, String.valueOf(resourceId))
				.setUsedVersion("v" + getDescription().getVersion()).setDownloadLink(resourceId)
				.setChangelogLink(resourceId).setNotifyOpsOnJoin(true).checkEveryXHours(6).checkNow();
	}

	@SuppressWarnings("deprecation")
	public void loadConfig() throws IOException, org.bukkit.configuration.InvalidConfigurationException {
		backgroundItem = new IItemStack(ItemStackUtils.get(config, "background"));
		backItem = ItemStackUtils.get(config, "recipe-view-gui.back");
		closeItem = new IItemStack(ItemStackUtils.get(config, "close"));
		prevItem = ItemStackUtils.get(config, "recipe-view-gui.previous");
		nextItem = ItemStackUtils.get(config, "recipe-view-gui.next");
		validItem = new IItemStack(ItemStackUtils.get(config, "workbench.valid_recipe"));
		invalidItem = new IItemStack(ItemStackUtils.get(config, "workbench.invalid_recipe"));
		shapeless = ItemStackUtils.get(config, "recipe-create-gui.shapeless");
		shapedItem = ItemStackUtils.get(config, "recipe-create-gui.shaped");
		saveItem = ItemStackUtils.get(config, "recipe-create-gui.save");
		editItem = ItemStackUtils.get(config, "recipe-create-gui.edit");
		deleteItem = ItemStackUtils.get(config, "recipe-create-gui.delete");
		quickCraftingItem = ItemStackUtils.get(config, "workbench.quick_crafting");
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
		quickCraftingAsync = config.getBoolean("check-quick-crafting-async");
		getLogger().info("Check recipes async: " + checkRecipesAsync);
		getLogger().info("Check quick crafting async: " + quickCraftingAsync);
		getLogger().info("Default crafting template is " + defaultDim.getWidth() + "x" + defaultDim.getHeight());
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

	public static boolean registerRecipe(IRecipe recipe) {
		return singleton.getRecipeManager().registerRecipe(recipe);
	}

	public IItemStack getValidItem() {
		return validItem;
	}

	public IItemStack getInvalidItem() {
		return invalidItem;
	}

	public IItemStack getBackgroundItem() {
		return backgroundItem;
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
		return shapedItem;
	}

	public ItemStack getSaveItem() {
		return saveItem;
	}

	public ItemStack getEditItem() {
		return editItem;
	}

	public ItemStack getDeleteItem() {
		return deleteItem;
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
		return quickCraftingItem;
	}

	public boolean isQuickCraftingAsync() {
		return quickCraftingAsync;
	}
}
