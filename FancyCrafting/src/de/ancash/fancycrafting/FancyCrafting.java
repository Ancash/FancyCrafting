package de.ancash.fancycrafting;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.ancash.libs.org.apache.commons.io.FileUtils;
import de.ancash.libs.org.simpleyaml.configuration.file.YamlFile;

import de.ancash.fancycrafting.commands.FancyCraftingCommand;
import de.ancash.fancycrafting.gui.WorkspaceDimension;
import de.ancash.fancycrafting.gui.WorkspaceObjects;
import de.ancash.fancycrafting.gui.WorkspaceSlotsBuilder;
import de.ancash.fancycrafting.gui.WorkspaceTemplate;
import de.ancash.fancycrafting.listeners.WorkbenchClickListener;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.Metrics;
import de.ancash.minecraft.MinecraftLoggerUtil;
import de.ancash.minecraft.updatechecker.UpdateCheckSource;
import de.ancash.minecraft.updatechecker.UpdateChecker;
import de.ancash.misc.ANSIEscapeCodes;
import de.ancash.misc.MathsUtils;
import de.ancash.misc.IPrintStream.ConsoleColor;

@SuppressWarnings("nls")
public class FancyCrafting extends JavaPlugin {

	private final ExecutorService threadPool = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
	private static FancyCrafting singleton;

	private static final int RESOURCE_ID = 87300;

	public static final Permission CREATE_PERM = new Permission("fancycrafting.admin.create", PermissionDefault.FALSE);
	public static final Permission EDIT_PERM = new Permission("fancycrafting.admin.edit", PermissionDefault.FALSE);
	public static final Permission VIEW_ALL_PERM = new Permission("fancycrafting.admin.view", PermissionDefault.FALSE);
	public static final Permission OPEN_DEFAULT_PERM = new Permission("fancycrafting.open", PermissionDefault.FALSE);
	public static final Permission OPEN_OTHER_DEFAULT_PERM = new Permission("fancycrafting.open.other");

	private DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

	private Response response;
	private UpdateChecker updateChecker;

	private RecipeManager recipeManager;
	private boolean checkRecipesAsync;
	private boolean quickCraftingAsync;
	private boolean permsForCustomRecipes;
	private boolean permsForVanillaRecipes;
	private boolean sortRecipesByRecipeName;
	private WorkspaceDimension defaultDim;
	private boolean debug;

	private final WorkspaceObjects workspaceObjects = new WorkspaceObjects();

	private FileConfiguration config;

	public void onEnable() {
		singleton = this;
		try {
			getLogger().info("Loading...");
			long now = System.nanoTime();
			MinecraftLoggerUtil.enableDebugging(this,
					(pl, record) -> debug ? true : record.getLevel().intValue() >= Level.INFO.intValue(),
					(pl, record) -> format(record));
			getLogger().info("Logger registered");
			new Metrics(this, 14152, true);
			checkForUpdates();
			loadFiles();
			recipeManager = new RecipeManager(this);
			response = new Response(this);
			PluginManager pm = Bukkit.getServer().getPluginManager();
			pm.registerEvents(new WorkbenchClickListener(singleton), this);

			getCommand("fc").setExecutor(new FancyCraftingCommand(this));

			getLogger().info("Done! " + MathsUtils.round((System.nanoTime() - now) / 1000000000D, 3) + "s");
		} catch (Exception e) {
			Bukkit.getPluginManager().disablePlugin(this);
			e.printStackTrace();
			return;
		}
	}

	private void loadFiles() throws IOException, InvalidConfigurationException {
		if (!new File("plugins/FancyCrafting/config.yml").exists())
			FileUtils.copyInputStreamToFile(getResource("resources/config.yml"),
					new File("plugins/FancyCrafting/config.yml"));

		checkFile(new File("plugins/FancyCrafting/config.yml"), "resources/config.yml");

		if (!new File("plugins/FancyCrafting/recipes.yml").exists())
			new File("plugins/FancyCrafting/recipes.yml").createNewFile();

		config = YamlConfiguration.loadConfiguration(new File("plugins/FancyCrafting/config.yml"));
		loadConfig();

		getLogger().info("Loading crafting templates...");
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
					getLogger().fine(String.format("Loaded %dx%d crafting template", width, height));
				} catch (Exception ex) {
					getLogger().log(Level.SEVERE,
							String.format("Could not load %dx%d crafting template!", width, height), ex);
				}
			}
		}
		getLogger().info("Crafting templates loaded!");
	}

	public void checkFile(File file, String src) throws IOException {
		getLogger().fine("Checking " + file.getPath() + " for completeness (comparing to " + src + ")");
		de.ancash.misc.FileUtils.setMissingConfigurationSections(new YamlFile(file), getResource(src),
				new HashSet<>(Arrays.asList("type")));
	}

	private void checkForUpdates() {
		updateChecker = new UpdateChecker(this, UpdateCheckSource.SPIGOT, String.valueOf(RESOURCE_ID))
				.setUsedVersion("v" + getDescription().getVersion()).setDownloadLink(RESOURCE_ID)
				.setChangelogLink(RESOURCE_ID).setNotifyOpsOnJoin(true).checkEveryXHours(6).checkNow();
	}

	@SuppressWarnings("deprecation")
	public void loadConfig() throws IOException, org.bukkit.configuration.InvalidConfigurationException {
		workspaceObjects.setBackgroundItem(new IItemStack(ItemStackUtils.get(config, "background")))
				.setBackItem(new IItemStack(ItemStackUtils.get(config, "recipe-view-gui.back")))
				.setCloseItem(new IItemStack(ItemStackUtils.get(config, "close")))
				.setPrevItem(new IItemStack(ItemStackUtils.get(config, "recipe-view-gui.previous")))
				.setNextItem(new IItemStack(ItemStackUtils.get(config, "recipe-view-gui.next")))
				.setValidItem(new IItemStack(ItemStackUtils.get(config, "workbench.valid_recipe")))
				.setInvalidItem(new IItemStack(ItemStackUtils.get(config, "workbench.invalid_recipe")))
				.setShapelessItem(new IItemStack(ItemStackUtils.get(config, "recipe-create-gui.shapeless")))
				.setShapedItem(new IItemStack(ItemStackUtils.get(config, "recipe-create-gui.shaped")))
				.setSaveItem(new IItemStack(ItemStackUtils.get(config, "recipe-create-gui.save")))
				.setEditItem(new IItemStack(ItemStackUtils.get(config, "recipe-create-gui.edit")))
				.setDeleteItem(new IItemStack(ItemStackUtils.get(config, "recipe-create-gui.delete")))
				.setQuickCraftingItem(new IItemStack(ItemStackUtils.get(config, "workbench.quick_crafting")))
				.setCreateNormalRecipeItem(
						new IItemStack(ItemStackUtils.get(config, "recipe-create-gui.create-normal")))
				.setCreateRandomRecipeItem(
						new IItemStack(ItemStackUtils.get(config, "recipe-create-gui.create-random")))
				.setManageRandomResultsItem(
						new IItemStack(ItemStackUtils.get(config, "recipe-create-gui.manage-random-results")))
				.setManageIngredientsItem(
						new IItemStack(ItemStackUtils.get(config, "recipe-create-gui.manage-ingredients")))
				.setManageRandomInvalidResultItem(
						new IItemStack(ItemStackUtils.get(config, "recipe-create-gui.manage-random-invalid-result")))
				.setInputRecipeNameLeftItem(
						new IItemStack(ItemStackUtils.get(config, "recipe-create-gui.input-recipe-name-left")))
				.setInputRecipeNameRightItem(
						new IItemStack(ItemStackUtils.get(config, "recipe-create-gui.input-recipe-name-right")))
				.setManageRecipeNameItem(new IItemStack(ItemStackUtils.get(config, "recipe-create-gui.manage-recipe-name")))
				.setCreateRecipeTitle(config.getString("recipe-create-gui.title"))
				.setCustomRecipesTitle(config.getString("recipe-view-gui.page-title"))
				.setViewRecipeTitle(config.getString("recipe-view-gui.single-title"))
				.setManageRandomResultsFormat(config.getString("recipe-create-gui.manage-random-results.format"))
				.setManageRandomIngredientsIdFormat(config.getString("recipe-create-gui.manage-ingredients.id-format"))
				.setEditRecipeTitle(config.getString("recipe-view-gui.edit-title"))
				.setBackCommands(Collections.unmodifiableList(config.getStringList("recipe-view-gui.back.commands")))
				.setIngredientsInputTitle(config.getString("recipe-create-gui.manage-ingredients-title"))
				.setManageResultTitle(config.getString("recipe-create-gui.manage-result-title"))
				.setManageProbabilityFooter(
						config.getStringList("recipe-create-gui.manage-random-result-probability.footer"))
				.setManageProbabilityHeader(
						config.getStringList("recipe-create-gui.manage-random-result-probability.header"))
				.setManageProbabilitiesTitle(config.getString("recipe-create-gui.manage-probabilities-title"))
				.setInputRecipeNameTitle(config.getString("recipe-create-gui.input-recipe-name-title"));

		defaultDim = new WorkspaceDimension(config.getInt("default-template-width"),
				config.getInt("default-template-height"));
		permsForCustomRecipes = config.getBoolean("perms-for-custom-recipes");
		permsForVanillaRecipes = config.getBoolean("perms-for-vanilla-recipes");
		checkRecipesAsync = config.getBoolean("check-recipes-async");
		quickCraftingAsync = config.getBoolean("check-quick-crafting-async");
		sortRecipesByRecipeName = config.getBoolean("sort-recipes-by-recipe-name");
		debug = config.getBoolean("debug");
		getLogger().info("Debug: " + debug);
		getLogger().info("Check recipes async: " + checkRecipesAsync);
		getLogger().info("Check quick crafting async: " + quickCraftingAsync);
		getLogger().info("Perms for custom recipes: " + permsForCustomRecipes);
		getLogger().info("Perms for vanilla recipes: " + permsForVanillaRecipes);
		getLogger().info("Sort recipes by recipe name: " + sortRecipesByRecipeName);
		getLogger().info("Default crafting template is " + defaultDim.getWidth() + "x" + defaultDim.getHeight());
	}

	private String format(LogRecord record) {
		StringBuilder builder = new StringBuilder();
		builder.append(ANSIEscapeCodes.MOVE_CURSOR_TO_BEGINNING_OF_NEXT_LINE_N_LINES_DOWN.replace("n", "0"));
		builder.append(ANSIEscapeCodes.ERASE_CURSOR_TO_END_OF_LINE);
		builder.append(parseColor(record.getLevel()));
		builder.append("[");
		builder.append(LocalDateTime.now().format(DATE_FORMATTER));
		builder.append("] [");
		builder.append(Thread.currentThread().getName());
		builder.append("/");
		builder.append(record.getLevel().toString());
		builder.append("] ");
		if (Bukkit.isPrimaryThread())
			builder.append("[FancyCrafting] ");
		builder.append(record.getMessage());
		return builder.toString();
	}

	private String parseColor(Level l) {
		if (l.intValue() <= 800)
			return "\033[38;5;159m";
		if (l.intValue() == 900)
			return ConsoleColor.YELLOW_BOLD_BRIGHT;
		if (l.intValue() == 1000)
			return ConsoleColor.RED_BOLD_BRIGHT;
		return "";
	}

	@Override
	public void onDisable() {
		updateChecker.stop();
		threadPool.shutdownNow();
		MinecraftLoggerUtil.disableDebugging(singleton);
	}

	public static boolean canCraftRecipe(IRecipe recipe, Player pl) {
		if (recipe.isVanilla() && !singleton.permsForVanillaRecipes)
			return true;
		if (!recipe.isVanilla() && !singleton.permsForCustomRecipes)
			return true;
		return pl.hasPermission(recipe.getCraftPermission());
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

	public WorkspaceObjects getWorkspaceObjects() {
		return workspaceObjects;
	}

	public WorkspaceDimension getDefaultDimension() {
		return defaultDim;
	}

	public boolean checkRecipesAsync() {
		return checkRecipesAsync;
	}

	public Response getResponse() {
		return response;
	}

	public boolean isQuickCraftingAsync() {
		return quickCraftingAsync;
	}

	public boolean sortRecipesByRecipeName() {
		return sortRecipesByRecipeName;
	}
}
