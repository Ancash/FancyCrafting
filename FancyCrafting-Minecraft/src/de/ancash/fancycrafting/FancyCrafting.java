package de.ancash.fancycrafting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.ancash.libs.org.apache.commons.io.FileUtils;
import de.ancash.libs.org.simpleyaml.configuration.file.YamlFile;
import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.commands.BlacklistSubCommand;
import de.ancash.fancycrafting.commands.CreateSubCommand;
import de.ancash.fancycrafting.commands.EditSubCommand;
import de.ancash.fancycrafting.commands.FancyCraftingCommand;
import de.ancash.fancycrafting.commands.OpenSubCommand;
import de.ancash.fancycrafting.commands.ReloadSubCommand;
import de.ancash.fancycrafting.commands.ViewSubCommand;
import de.ancash.fancycrafting.gui.CraftingWorkspaceGUI;
import de.ancash.fancycrafting.gui.RecipeCollectionPagedViewGUI;
import de.ancash.fancycrafting.gui.SingleRecipePagedViewGUI;
import de.ancash.fancycrafting.gui.ViewSlots;
import de.ancash.fancycrafting.gui.WorkspaceDimension;
import de.ancash.fancycrafting.gui.WorkspaceObjects;
import de.ancash.fancycrafting.gui.WorkspaceTemplate;
import de.ancash.fancycrafting.gui.manage.normal.CreateNormalRecipeGUI;
import de.ancash.fancycrafting.gui.manage.normal.EditNormalRecipeGUI;
import de.ancash.fancycrafting.gui.manage.normal.ViewNormalRecipeGUI;
import de.ancash.fancycrafting.gui.manage.random.CreateRandomRecipeGUI;
import de.ancash.fancycrafting.gui.manage.random.EditRandomRecipeGUI;
import de.ancash.fancycrafting.gui.manage.random.ViewRandomRecipeGUI;
import de.ancash.fancycrafting.listeners.WorkbenchClickListener;
import de.ancash.fancycrafting.listeners.WorkbenchOpenListener;
import de.ancash.fancycrafting.recipe.RecipeMatcherCallable;
import de.ancash.fancycrafting.recipe.IRandomRecipe;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.Metrics;
import de.ancash.minecraft.MinecraftLoggerUtil;
import de.ancash.minecraft.updatechecker.UpdateCheckSource;
import de.ancash.minecraft.updatechecker.UpdateChecker;
import de.ancash.misc.ANSIEscapeCodes;
import de.ancash.misc.MathsUtils;
import de.ancash.misc.io.IPrintStream.ConsoleColor;

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
	public static final Permission OPEN_OTHER_DEFAULT_PERM = new Permission("fancycrafting.open.other",
			PermissionDefault.FALSE);
	public static final Permission RELOAD = new Permission("fancycrafting.admin.reload", PermissionDefault.FALSE);

	protected final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

	protected Response response;
	protected UpdateChecker updateChecker;
	protected WorkspaceDimension defaultDim;
	protected ViewSlots viewSlots;
	protected RecipeManager recipeManager;
	protected final WorkspaceObjects workspaceObjects = new WorkspaceObjects();
	private final File blacklistFile = new File("plugins/FancyCrafting/blacklist/config.yml");
	private final YamlConfiguration blacklistConfig = YamlConfiguration.loadConfiguration(blacklistFile);

	private String manageBlacklistTitle;
	private IItemStack addRecipeToBlacklistItem;
	private String addRecipeToBlacklistTitle;

	protected boolean checkRecipesAsync;
	protected boolean quickCraftingAsync;
	protected boolean permsForCustomRecipes;
	protected boolean permsForVanillaRecipes;
	protected boolean permsForQuickCrafting;
	protected boolean sortRecipesByRecipeName;
	protected boolean supportVanilla3x3;
	protected boolean supportVanilla2x2;
	protected int craftingCooldown;
	protected boolean debug;
	protected PermissionDefault craftPermDef;
	protected PermissionDefault viewPermDef;

	protected FileConfiguration config;
	private final File configFile = new File("plugins/FancyCrafting/config.yml");

	public void onEnable() {
		singleton = this;
		new Metrics(this, 14152, true);
		load();
		loadCommands();
		loadListeners();
	}

	protected void loadCommands() {
		FancyCraftingCommand cmd = new FancyCraftingCommand(this);
		loadSubCommands(cmd);
		cmd.addSubCommand(new ReloadSubCommand(this, "reload", "rl"));
		cmd.addSubCommand(new CreateSubCommand(this, "create"));
		cmd.addSubCommand(new OpenSubCommand(this, "open"));
		cmd.addSubCommand(new EditSubCommand(this, "edit"));
		cmd.addSubCommand(new ViewSubCommand(this, "view"));
		getCommand("fc").setExecutor(cmd);
	}

	protected void loadListeners() {
		HandlerList.unregisterAll(this);
		PluginManager pm = Bukkit.getServer().getPluginManager();
		if (config.getBoolean("crafting.use-custom-gui"))
			pm.registerEvents(new WorkbenchOpenListener(this), this);

		Bukkit.getPluginManager()
				.registerEvents(new WorkbenchClickListener(this, config.getBoolean("crafting.use-custom-gui"),
						config.getBoolean("crafting.support-vanilla-3x3"),
						config.getBoolean("crafting.support-vanilla-2x2")), this);
	}

	@Override
	public FileConfiguration getConfig() {
		return config;
	}

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
			recipeManager = new RecipeManager((FancyCrafting) this);
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Could not load recipes", e);
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		System.gc();
		getLogger().info("Done! " + MathsUtils.round((System.nanoTime() - now) / 1000000000D, 3) + "s");
	}

	private void loadBlacklistConfig() throws FileNotFoundException, IOException, InvalidConfigurationException {
		if (!blacklistFile.exists())
			FileUtils.copyInputStreamToFile(getResource("resources/blacklist-config.yml"), blacklistFile);
		checkFile(blacklistFile, "resources/blacklist-config.yml");
		blacklistConfig.load(blacklistFile);
		manageBlacklistTitle = blacklistConfig.getString("main-title");
		addRecipeToBlacklistItem = new IItemStack(
				ItemStackUtils.getItemStack(blacklistConfig, "add-recipe-to-blacklist.item"));
		addRecipeToBlacklistTitle = blacklistConfig.getString("add-recipe-to-blacklist.title");
	}

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
			recipeManager = new RecipeManager((FancyCrafting) this);
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Could not load recipes", e);
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		getLogger().info("Done! " + MathsUtils.round((System.nanoTime() - now) / 1000000000D, 3) + "s");
	}

	public void viewRecipe(Player p, IRecipe recipe) {
		viewRecipeSingle(p, new HashSet<>(Arrays.asList(recipe)));
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

	public void loadSubCommands(FancyCraftingCommand fc) {
		fc.addSubCommand(new BlacklistSubCommand(this, "blacklist", "bl"));
	}

	public RecipeMatcherCallable newDefaultRecipeMatcher(Player player) {
		return new RecipeMatcherCallable(this, player);
	}

	private void loadFiles() throws IOException, InvalidConfigurationException {
		if (!configFile.exists())
			FileUtils.copyInputStreamToFile(getResource("resources/config.yml"), configFile);
		applyConfigPatches();
		checkFile(configFile, "resources/config.yml");

		if (!new File("plugins/FancyCrafting/recipes.yml").exists())
			new File("plugins/FancyCrafting/recipes.yml").createNewFile();

		config = YamlConfiguration.loadConfiguration(configFile);
		loadConfig();

		getLogger().info("Loading crafting templates...");
		for (int width = 1; width <= 8; width++) {
			for (int height = 1; height <= 6; height++) {
				try {
					File craftingTemplateFile = new File(
							"plugins/FancyCrafting/crafting-" + width + "x" + height + ".yml");
					if (!craftingTemplateFile.exists())
						FileUtils.copyInputStreamToFile(
								getResource("resources/crafting-" + width + "x" + height + ".yml"),
								craftingTemplateFile);

					checkFile(craftingTemplateFile, "resources/crafting-" + width + "x" + height + ".yml");

					FileConfiguration craftingTemplateConfig = YamlConfiguration
							.loadConfiguration(craftingTemplateFile);
					WorkspaceTemplate.add(this, new WorkspaceTemplate(craftingTemplateConfig.getString("title"),
							new WorkspaceDimension(width, height, craftingTemplateConfig.getInt("size")),
							new WorkspaceSlotsBuilder().setResultSlot(craftingTemplateConfig.getInt("result-slot"))
									.setCloseSlot(craftingTemplateConfig.getInt("close-slot"))
									.setCraftingSlots(craftingTemplateConfig.getIntegerList("crafting-slots").stream()
											.mapToInt(Integer::intValue).toArray())
									.setCraftStateSlots(craftingTemplateConfig.getIntegerList("craft-state-slots")
											.stream().mapToInt(Integer::intValue).toArray())
									.setAutoCraftingSlots(craftingTemplateConfig.getBoolean("enable-quick-crafting")
											? craftingTemplateConfig.getIntegerList("quick-crafting-slots").stream()
													.mapToInt(Integer::intValue).toArray()
											: null)
									.setEnableQuickCrafting(craftingTemplateConfig.getBoolean("enable-quick-crafting"))
									.build()));
					getLogger().fine(String.format("Loaded %dx%d crafting template", width, height));
				} catch (Exception ex) {
					getLogger().log(Level.SEVERE,
							String.format("Could not load %dx%d crafting template!", width, height), ex);
				}
			}
		}
		getLogger().info("Crafting templates loaded!");
		response = new Response(this);
	}

	private void applyConfigPatches()
			throws de.ancash.libs.org.simpleyaml.exceptions.InvalidConfigurationException, IOException {
		YamlFile yamlCfg = new YamlFile(configFile);
		yamlCfg.load();
		de.ancash.misc.io.FileUtils.move(yamlCfg, "use-custom-crafting-gui", "crafting.use-custom-gui");
		de.ancash.misc.io.FileUtils.move(yamlCfg, "crafting-cooldown-message", "crafting.cooldown-message");
		de.ancash.misc.io.FileUtils.move(yamlCfg, "crafting-cooldown", "crafting.cooldown");
		de.ancash.misc.io.FileUtils.move(yamlCfg, "check-recipes-async", "crafting.check-recipes-async");
		de.ancash.misc.io.FileUtils.move(yamlCfg, "check-quick-crafting-async", "crafting.check-quick-crafting-async");
		de.ancash.misc.io.FileUtils.move(yamlCfg, "perms-for-custom-recipes", "crafting.perms-for-custom-recipes");
		de.ancash.misc.io.FileUtils.move(yamlCfg, "perms-for-vanilla-recipes", "crafting.perms-for-vanilla-recipes");
		de.ancash.misc.io.FileUtils.move(yamlCfg, "default-template-width", "crafting.default-template-width");
		de.ancash.misc.io.FileUtils.move(yamlCfg, "default-template-height", "crafting.default-template-height");
		yamlCfg.save();
	}

	public void checkFile(File file, String src) throws IOException {
		getLogger().fine("Checking " + file.getPath() + " for completeness (comparing to " + src + ")");
		de.ancash.misc.io.FileUtils.setMissingConfigurationSections(new YamlFile(file), getResource(src),
				new HashSet<>(Arrays.asList("type")));
	}

	protected void checkForUpdates() {
		updateChecker = new UpdateChecker(this, UpdateCheckSource.SPIGOT, String.valueOf(RESOURCE_ID))
				.setUsedVersion("v" + getDescription().getVersion()).setDownloadLink(RESOURCE_ID)
				.setChangelogLink(RESOURCE_ID).setNotifyOpsOnJoin(true).checkEveryXHours(6).checkNow();
	}

	private void loadConfig() throws IOException, InvalidConfigurationException {
		viewSlots = new ViewSlots(config.getInt("recipe-view-gui.size"), config.getInt("recipe-view-gui.result-slot"),
				config.getInt("recipe-view-gui.ingredients-slot"), config.getInt("recipe-view-gui.probability-slot"),
				config.getInt("recipe-view-gui.close-slot"), config.getInt("recipe-view-gui.back-slot"),
				config.getInt("recipe-view-gui.previous-slot"), config.getInt("recipe-view-gui.next-slot"),
				config.getInt("recipe-view-gui.edit-slot"));
		workspaceObjects.setBackgroundItem(new IItemStack(ItemStackUtils.getItemStack(config, "background")))
				.setBackItem(new IItemStack(ItemStackUtils.getItemStack(config, "recipe-view-gui.back")))
				.setCloseItem(new IItemStack(ItemStackUtils.getItemStack(config, "close")))
				.setPrevItem(new IItemStack(ItemStackUtils.getItemStack(config, "recipe-view-gui.previous")))
				.setNextItem(new IItemStack(ItemStackUtils.getItemStack(config, "recipe-view-gui.next")))
				.setValidItem(new IItemStack(ItemStackUtils.getItemStack(config, "workbench.valid_recipe")))
				.setInvalidItem(new IItemStack(ItemStackUtils.getItemStack(config, "workbench.invalid_recipe")))
				.setShapelessItem(new IItemStack(ItemStackUtils.getItemStack(config, "recipe-create-gui.shapeless")))
				.setShapedItem(new IItemStack(ItemStackUtils.getItemStack(config, "recipe-create-gui.shaped")))
				.setSaveItem(new IItemStack(ItemStackUtils.getItemStack(config, "recipe-create-gui.save")))
				.setEditItem(new IItemStack(ItemStackUtils.getItemStack(config, "recipe-create-gui.edit")))
				.setDeleteItem(new IItemStack(ItemStackUtils.getItemStack(config, "recipe-create-gui.delete")))
				.setQuickCraftingItem(new IItemStack(ItemStackUtils.getItemStack(config, "workbench.quick_crafting")))
				.setCreateNormalRecipeItem(
						new IItemStack(ItemStackUtils.getItemStack(config, "recipe-create-gui.create-normal")))
				.setCreateRandomRecipeItem(
						new IItemStack(ItemStackUtils.getItemStack(config, "recipe-create-gui.create-random")))
				.setManageRandomResultsItem(
						new IItemStack(ItemStackUtils.getItemStack(config, "recipe-create-gui.manage-random-results")))
				.setViewRandomResultsItem(
						new IItemStack(ItemStackUtils.getItemStack(config, "recipe-view-gui.view-random-results")))
				.setManageIngredientsItem(
						new IItemStack(ItemStackUtils.getItemStack(config, "recipe-create-gui.manage-ingredients")))
				.setManageIngredientsItem(
						new IItemStack(ItemStackUtils.getItemStack(config, "recipe-create-gui.manage-ingredients")))
				.setViewIngredientsItem(
						new IItemStack(ItemStackUtils.getItemStack(config, "recipe-view-gui.view-ingredients")))
				.setManageRandomInvalidResultItem(new IItemStack(
						ItemStackUtils.getItemStack(config, "recipe-create-gui.manage-random-invalid-result")))
				.setInputRecipeNameLeftItem(
						new IItemStack(ItemStackUtils.getItemStack(config, "recipe-create-gui.input-recipe-name-left")))
				.setInputRecipeNameRightItem(new IItemStack(
						ItemStackUtils.getItemStack(config, "recipe-create-gui.input-recipe-name-right")))
				.setManageRecipeNameItem(
						new IItemStack(ItemStackUtils.getItemStack(config, "recipe-create-gui.manage-recipe-name")))
				.setCreateRecipeTitle(config.getString("recipe-create-gui.title"))
				.setCustomRecipesTitle(config.getString("recipe-view-gui.page-title"))
				.setViewRecipeTitle(config.getString("recipe-view-gui.single-title"))
				.setManageRandomResultsFormat(config.getString("recipe-create-gui.manage-random-results.format"))
				.setViewRandomResultsFormat(config.getString("recipe-view-gui.view-random-results.format"))
				.setManageIngredientsIdFormat(config.getString("recipe-create-gui.manage-ingredients.id-format"))
				.setViewIngredientsIdFormat(config.getString("recipe-view-gui.view-ingredients.id-format"))
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
		defaultDim = new WorkspaceDimension(config.getInt("crafting.default-template-width"),
				config.getInt("crafting.default-template-height"));
		permsForCustomRecipes = config.getBoolean("crafting.perms-for-custom-recipes");
		permsForVanillaRecipes = config.getBoolean("crafting.perms-for-vanilla-recipes");
		permsForQuickCrafting = config.getBoolean("crafting.perms-for-quick-crafting");
		checkRecipesAsync = config.getBoolean("crafting.check-recipes-async");
		quickCraftingAsync = config.getBoolean("crafting.check-quick-crafting-async");
		sortRecipesByRecipeName = config.getBoolean("sort-recipes-by-recipe-name");
		supportVanilla2x2 = config.getBoolean("crafting.support-vanilla-2x2");
		supportVanilla3x3 = config.getBoolean("crafting.support-vanilla-3x3");
		debug = config.getBoolean("debug");
		craftingCooldown = config.getInt("crafting.cooldown");
		craftPermDef = parsePermissionDefault(config.getString("craft-recipe-permission-default"));
		viewPermDef = parsePermissionDefault(config.getString("view-recipe-permission-default"));
		getLogger().info("Debug: " + debug);
		MinecraftLoggerUtil.enableDebugging(this,
				(pl, record) -> debug ? true : record.getLevel().intValue() >= Level.INFO.intValue(),
				(pl, record) -> format(record));
		getLogger().info("Check recipes async: " + checkRecipesAsync);
		getLogger().info("Check quick crafting async: " + quickCraftingAsync);
		getLogger().info("Perms for custom recipes: " + permsForCustomRecipes);
		getLogger().info("Perms for vanilla recipes: " + permsForVanillaRecipes);
		getLogger().info("Perms for quick crafting: " + permsForQuickCrafting);
		getLogger().info("Craft recipe permission default: " + craftPermDef);
		getLogger().info("View recipe permission default: " + viewPermDef);
		getLogger().info("Sort recipes by recipe name: " + sortRecipesByRecipeName);
		getLogger().info("Default crafting template: " + defaultDim.getWidth() + "x" + defaultDim.getHeight());
		getLogger().info("Crafting cooldown in ticks: " + craftingCooldown);
		getLogger().info("Open on crafting table open: " + config.getBoolean("open-on-crafting-table-open"));
		getLogger().info("Support vanilla 3x3: " + supportVanilla3x3);
		getLogger().info("Support vanilla 2x2: " + supportVanilla2x2);
	}
	
	private PermissionDefault parsePermissionDefault(String s) {
		try {
			return PermissionDefault.valueOf(s.toUpperCase());
		} catch(Exception e) {
			getLogger().warning("'" + s.toUpperCase() + "' is not a valid permission default");
			getLogger().warning("Possible values: " + PermissionDefault.values());
			getLogger().warning("Using FALSE as permission default");
			return PermissionDefault.FALSE;
		}
	}

	private String format(LogRecord record) {
		StringBuilder builder = new StringBuilder();
		List<String> unformatted = new ArrayList<>();
		if (record.getMessage().contains("\n"))
			unformatted.addAll(Arrays.asList(record.getMessage().split("\n")));
		else
			unformatted.add(record.getMessage());

		if (record.getThrown() != null) {
			Throwable th = record.getThrown();
			unformatted.add(th.getClass().getCanonicalName() + ": " + th.getLocalizedMessage());
			for (StackTraceElement e : th.getStackTrace())
				unformatted.add("        at " + e.toString());
			th = th.getCause();
			while (th != null) {
				unformatted.add("Caused by: " + th.getClass().getCanonicalName() + ": " + th.getLocalizedMessage());
				for (StackTraceElement e : th.getStackTrace())
					unformatted.add("        at " + e.toString());
				th = th.getCause();
			}
		}

		for (int i = 0; i < unformatted.size(); i++) {
			builder.append(ANSIEscapeCodes.MOVE_CURSOR_TO_BEGINNING_OF_NEXT_LINE_N_LINES_DOWN.replace("n", "0"))
					.append(ANSIEscapeCodes.ERASE_CURSOR_TO_END_OF_LINE).append(parseColor(record.getLevel()))
					.append('[').append(LocalDateTime.now().format(DATE_FORMATTER)).append("] [")
					.append(Thread.currentThread().getName()).append('/').append(record.getLevel().toString())
					.append("] [").append(getName()).append("] ")
					.append(unformatted.get(i).replace("[" + getName() + "] ", "")).append(ConsoleColor.RESET);
			if (i < unformatted.size() - 1)
				builder.append('\n');
		}
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
		HandlerList.unregisterAll(this);
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
	
	public static PermissionDefault getCraftPermDef() {
		return singleton.craftPermDef;
	}
	
	public static PermissionDefault getViewPermDef() {
		return singleton.viewPermDef;
	}

	public static boolean permsForCustomRecipes() {
		return singleton.permsForCustomRecipes;
	}

	public static boolean permsForVanillaRecipes() {
		return singleton.permsForVanillaRecipes;
	}

	public static boolean permsForQuickCrafting() {
		return singleton.permsForQuickCrafting;
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

	public ViewSlots getViewSlots() {
		return viewSlots;
	}

	public int getCraftingCooldown() {
		return craftingCooldown;
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

	public void editRecipe(Player p, IRecipe r) {
		if (r instanceof IRandomRecipe)
			new EditRandomRecipeGUI(this, p, r).open();
		else
			new EditNormalRecipeGUI(this, p, r).open();
	}

	public void openCreateRandomRecipe(Player p, String name) {
		new CreateRandomRecipeGUI(this, p, name).open();
	}

	public void openCreateNormalRecipe(Player p, String name) {
		new CreateNormalRecipeGUI(this, p, name).open();
	}

	public void openCraftingWorkspace(Player player, WorkspaceTemplate template) {
		new CraftingWorkspaceGUI(this, player, template);
	}

	public void viewRecipeCollection(Player player, Set<IRecipe> recipes) {
		new RecipeCollectionPagedViewGUI(this, player, new ArrayList<>(getRecipeManager().getCustomRecipes()));
	}
}
