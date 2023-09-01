package de.ancash.fancycrafting;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.fancycrafting.exception.InvalidRecipeException;
import de.ancash.fancycrafting.exception.RecipeDeleteException;
import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRandomShapedRecipe;
import de.ancash.fancycrafting.recipe.IRandomShapelessRecipe;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.fancycrafting.recipe.RecipeCategory;
import de.ancash.misc.MathsUtils;
import de.ancash.nbtnexus.serde.SerializedItem;

public class RecipeManager {

	protected final Map<List<Integer>, IRecipe> cachedRecipes = new ConcurrentHashMap<>();
	protected final Set<IRecipe> customRecipes = new HashSet<>();
	protected final Set<IRecipe> autoMatchingRecipes = new HashSet<>();
	protected final Map<String, Set<IRecipe>> recipesByName = new ConcurrentHashMap<>();
	protected final Map<Integer, Set<IRecipe>> recipesByHash = new ConcurrentHashMap<>();

	private final File recipeFile = new File("plugins/FancyCrafting/recipes.yml"); //$NON-NLS-1$
	private final FileConfiguration recipeCfg = YamlConfiguration.loadConfiguration(recipeFile);

	private final File blacklistFile = new File("plugins/FancyCrafting/blacklist/recipes.yml"); //$NON-NLS-1$
	private final FileConfiguration blacklistCfg = YamlConfiguration.loadConfiguration(blacklistFile);

	private final Map<List<Integer>, IRecipe> blacklistedRecipes = new HashMap<>();
	protected final FancyCrafting plugin;

	public RecipeManager(FancyCrafting pl) throws IOException {
		this.plugin = pl;
		if (!recipeFile.exists())
			recipeFile.createNewFile();
		if (!blacklistFile.exists())
			blacklistFile.createNewFile();
		loadBukkitRecipes();
		loadCustomRecipes();
		loadBlacklistedRecipes();
	}

	public void loadBukkitRecipes() {
		Bukkit.recipeIterator().forEachRemaining(r -> {
			IRecipe recipe = IRecipe.fromVanillaRecipe(plugin, r);
			if (recipe == null)
				return;
			registerRecipe(recipe);
		});
	}

	public FileConfiguration getBlacklistRecipeFileCfg() {
		return blacklistCfg;
	}

	public File getBlacklistRecipeFile() {
		return blacklistFile;
	}

	public void cacheRecipe(IRecipe recipe) {
		if (recipe.isVanilla())
			return;
		if (recipe instanceof IShapedRecipe)
			cachedRecipes.put(recipe.getHashMatrix(), recipe);
		else
			cachedRecipes.put(
					recipe.getHashMatrix().stream().filter(i -> i != null).sorted().collect(Collectors.toList()),
					recipe);
	}

	public Set<IRecipe> getRecipeByHashs(ItemStack itemStack) {
		return getRecipeByHash(SerializedItem.of(itemStack));
	}

	public Set<IRecipe> getRecipeByHash(SerializedItem iItemStack) {
		if (recipesByHash.get(iItemStack.hashCode()) == null)
			return null;
		return Collections.unmodifiableSet(recipesByHash.get(iItemStack.hashCode()));
	}

	// [262834092, 458945362, 515090105, 451525351, 1709133265, 809502800,
	// 1546814510, 1726351098, 1502593426, 1170228701, 1114360399, 903423486,
	// 1910940110, 1814025267, 1940720513, 635287026, 1246728829, 1043547662,
	// 1229713676, 1314825750, 976030304, 45943538, 1707562385, 1697928556,
	// 1109785214, 1247099406, 863843105, 1702475044, 203187324, 1611280489,
	// 470813813, 456250199, 1753307367, 470998142, 1398319925, 1497239883,
	// 508614185, 1450436105, 1678904431, 396036069, 389521332, 1044473543,
	// 419139695, 1154112606, 1752426726, 413667390, 721976208, 1065531163,
	// 2055895569, 1251019278, 2054005453, 1113656003, 1560884652, 411830308,
	// 1703093545, 363926814, 2047867583, 2062496351, 878111972, 1166007755,
	// 1702258114, 1280867565, 1056757317, 854633034, 276811971, 1672100628,
	// 1013726419, 2005872432, 366595353, 217009199, 2117734138, 1585235645,
	// 1958573182, 253396953, 37294844, 1782233855, 1586660948, 1523692055,
	// 1503058257, 130707201, 1211470078, 519675444, 1374419237, 567996693,
	// 128362358, 1946899673, 410351778, 196307819, 534363907, 1128430489,
	// 1444314826, 1366872484, 445254230, 1360067020, 699746667, 820217858,
	// 709190308, 1204539183, 388702159, 1664054219, 1811667742, 1615063204,
	// 1187524143, 98647305, 1494655486, 1554498638, 714726005, 1302500685,
	// 1250380392, 1046652561, 1937496968, 2129319452, 1716731561, 1424143385,
	// 1033768293, 759537083, 1383508616, 629070971, 1769810040, 49869810,
	// 1644253974, 1851545412, 1125034515, 1604138684, 629675178, 1281901903,
	// 130682592, 2066360838, 192612950, 136304944, 1028381796, 712235840,
	// 1120439292, 1606821616, 629488829, 467561191, 1227759636, 497220761,
	// 1303837837, 67683636, 1389003766, 2021464385, 527859273, 699377903,
	// 466952848, 1239937184, 1254552462, 1174174896, 1598608903, 154814581,
	// 1504213240, 576434954, 1525978021, 1599327745, 1764935780, 750265839,
	// 1358209443, 1454292982, 656500344, 1662759490, 45679588, 1839459582,
	// 1455179741, 1214146879, 2015844672, 2018644282, 686389632, 1539996324,
	// 2064380539, 770849924, 2011201175, 627258395, 2038134791, 468988554,
	// 1127647817, 124143129, 626087120, 204463529, 5736912, 1672024609, 1539904071,
	// 1127213757, 712287207, 2116063157, 7854550, 1506963574, 1023128927,
	// 1884017134, 837967960, 157276405, 15644995, 321479302, 497945631, 2062107480,
	// 760755435, 1633704149, 1287171328, 1640153209, 2945431, 2020770249,
	// 789038787, 389207798, 2131537610, 2022793558, 1242428357, 1597890292,
	// 542755216, 1785797564, 1107928006, 644933567, 1264175752, 1654971255,
	// 1541397002, 492471424, 1394860792, 703488048, 1503748163, 2140800632,
	// 1299225747, 120354511, 212153670, 508997566, 550863166, 1638791232,
	// 556634307, 338589120, 1088540947, 708475854, 270324196, 1904543869,
	// 995316051, 669887982, 2144548315, 101942298, 386104584, 1976645573,
	// 1449069643, 910808156, 742223403, 1976961015, 12665576, 2145537481,
	// 848027808, 522605832, 333750741, 1208889912, 1459557573, 1670355379,
	// 460538190, 1365760800, 1013972519, 486793034, 1292681030, 1075042904,
	// 85577558, 1305036698, 1821232851, 1225925499, 2012255641, 635230131,
	// 263902666, 1848515677, 99907468, 279939796, 1016014579, 629313534,
	// 1918879964, 969059137, 1013173991, 1167341331, 193403151, 175821043,
	// 385084543, 1682783936, 1851786478, 1998796204, 1541927292, 1645453455,
	// 2056024080, 1473477967, 1345987236, 1213145838, 15274575, 860477921,
	// 1946525356, 1068768536, 1242691775, 2053408971, 1772649245, 175458499,
	// 1322202232, 835685240, 295840533, 1356339810, 1771377454, 1332030883,
	// 1338600705, 355356671, 491919283, 477564565, 820255244, 2127049764,
	// 256558500, 1664039390, 1387176026, 1360151526, 370932974, 1065604473,
	// 894334901, 1394151906, 1883415231, 1996448769, 239760564, 503064323,
	// 321470511, 772752648, 1265000557, 786773486, 48243179, 1383737718,
	// 1855233434, 1992652240, 70436644, 189421648, 1041007895, 1604256955,
	// 529665188, 1485241275, 2086631266, 341063529, 51879978, 31001318, 1193995371,
	// 485429164, 1916936454, 1126967398, 947725101, 1156943780, 1081264934,
	// 1608391818, 1016154055, 1550214974, 637867357, 1050861054, 212187130,
	// 472557386, 899362554, 1795846254, 1205322943, 10470388, 532585685, 260316406,
	// 1162212919, 1123107202, 1482191659, 1576808073, 1877873813, 389633093,
	// 1623389118, 1477399408, 1172627042, 14490459, 332142667, 1011122125,
	// 227880478, 1054645584, 1047217637, 2146432127, 2057136397, 656546997,
	// 30405208, 1341533295, 610840564, 297536035, 406178709, 1181574606, 652029300,
	// 1736821800, 1569091115, 1728191660, 1750129635, 1852882381, 1762796242,
	// 519925053, 1705588329, 123174037, 682777466, 1719285168, 727466129, 25131697,
	// 2088646568, 1499714229, 18006725, 2080149547, 171913700, 2808817, 1536491593,
	// 445145429, 379606397, 960948900, 438673862, 999958653, 1936949406, 721881321,
	// 1028183719, 63174707, 1177314779, 1640510003, 467851294, 1199983738,
	// 673518553, 754550532, 1248552555, 2021545087, 1106988538, 633182654,
	// 1769284991, 2109082045, 641688114, 2131761949, 1471599544, 1134464911,
	// 1525493502, 991325955, 179240630, 1729841923, 208582775, 2119049813,
	// 640209461, 115794683, 1418924209, 1628878115, 1954537538, 1599552361,
	// 516092669, 1049244362, 1556187267, 636916149, 1019287682, 971044261,
	// 517753499, 235436610, 87404927, 389403519, 1930583902, 934122465, 1626483903,
	// 1015976128, 1634870570, 1005393844, 648872688, 744423425, 1487918321,
	// 1378012796, 1531236132, 50212508, 1713558086, 1228725595, 2143493039,
	// 270218356, 936369083, 1787948772, 1459372682, 1086893233, 116171414,
	// 958793053, 932795362, 173534764, 1598368569, 1354544321, 2092462277,
	// 424490290, 926989253, 1292200329, 1423579223, 1337785133, 1645479561,
	// 2039291178, 818127978, 344412206, 848743992, 1752865999, 112421381,
	// 803777362, 1545152674, 584388200, 133833540, 1075553444, 1275375813,
	// 49944120, 1890030688, 2032723439, 639324892, 645073593, 107451051,
	// 2110593309, 575407774, 225161651, 2110202142, 456292329, 1777270028,
	// 962282937, 2035834298, 189755306, 250214927, 1318980214, 828618123,
	// 1792167486, 510591616, 1456224829, 1983578826, 887093292, 276831348,
	// 1045146302, 2070011798, 605909220, 335486721, 457559994, 1190216688,
	// 1407100639, 1752419697, 829267048, 1644171014, 233140461, 1880057094,
	// 706031388, 1283795343, 282742230, 1942056657, 2112139505, 1889332618,
	// 806143253, 1747187005, 364050578, 1915969298, 2116467460, 1881494790,
	// 1556019611, 615307659, 2094074360, 1645784883, 1326387748, 1613116682,
	// 1452979115, 1425596874, 1357805018, 1057313329, 131951339, 2106396859,
	// 2043172095, 1455031169, 1169643658, 1855978463, 1849774848, 2060898194,
	// 513600474, 1029435411, 832378065, 13237637, 981286706, 1224439202,
	// 2018710794, 539780470, 2027509127, 1811762845, 35319675, 2097825897,
	// 2137037315, 202719603, 760784959, 1872503540, 629795041, 191360538,
	// 906374022, 720379760, 1873105440, 1467040506, 1274900086, 1202221605,
	// 516211529, 1294891168, 1915535266, 1946142311, 1380554709, 856258196,
	// 2031244429, 1272155762, 57949904, 294497320, 1236016138, 1692289398,
	// 1162416353, 421518519, 1327501963, 1824370966, 267770458, 35856278,
	// 1793406815, 16972998, 1974794354, 701621275, 569951284, 1944833574,
	// 738507211, 73757063, 53386956, 2043515951, 246367047, 1082565999, 740065777,
	// 1889088887, 881181035, 801784356, 1864690388, 303114210, 69800412, 131572258,
	// 942183902, 1005176351, 1551251841, 2005506673, 216723734, 151958771,
	// 61703598, 1445929000, 1065834892, 1069361587, 1587517584, 80507587,
	// 1271584219, 323564891, 936034776, 751590099, 1441203141, 699135881,
	// 1220075163, 359312196, 943652352, 1998041385, 1681382007, 511627317,
	// 2075307513, 741524089, 1891888781, 1193367923, 367012791, 683613927,
	// 1455427740, 1817995779, 1790328041, 142679777, 1476092317, 1789803719,
	// 885715616, 1128434990, 2139511275, 456678743, 262775257, 261851617,
	// 1640480699, 1256725654, 119632223, 1219636941, 207526433, 1275841162,
	// 2086720225, 1393008003, 1856397344, 1632587713, 1560533900, 2045441919,
	// 47569999, 160112406, 1702472667, 2085589738, 154468242, 971073352, 666761484,
	// 510978247, 1043566726, 1463482754, 1569901354, 1493565375, 535318868,
	// 34526385, 2061626401, 1714598131, 472144609, 194387283, 432152330,
	// 1985206402, 2058601575, 832999280, 1787380816, 1297511307, 2027142668,
	// 1139102982, 1357872645, 594394502, 1517599897, 51181027, 871838981,
	// 1380234422, 517406754, 1226311663, 2070691220, 342254234, 1606560062,
	// 695070611, 891869736, 715959727, 384328172, 1516483947, 632636091, 376080762,
	// 2034521686, 1484498799, 67521297, 1611456276, 1131424878, 1482594175,
	// 1041637687, 1057511466, 2123196846, 803952847, 1106583250, 1498402406,
	// 1373200115, 768424678, 608668523, 22429305, 1787721150, 1832890888,
	// 441843053, 2086361955, 1855869348, 1392494019, 920152473, 761971247,
	// 1401074884, 1872838889, 683894666, 918391287, 623420002, 1669700808,
	// 1940919550, 2027042691, 199203976, 1620078538, 2132039753, 1833028133,
	// 1802468363, 142063377, 1629103741, 1962582929, 269965178, 772120939,
	// 242616600, 1753257967, 585037231, 1421163463, 710489387, 1647103327,
	// 1612922709, 307035450, 184759417, 2104275478, 2140934368, 1615085369,
	// 393894143, 1746108317, 930566392, 1097762558, 1542276222, 852432219,
	// 1445779896, 227678726, 748221198, 906715538, 1325963413, 803287167,
	// 2093040478, 422334131, 1906865672, 765340223, 1408461906, 194440243,
	// 1211932570, 960993007, 655857854, 1318799666, 186434742, 824417934,
	// 859163800, 1551373270, 1135641630, 1250286901, 46675326, 481927939,
	// 220093150, 2087836601, 849679725, 214823684, 767347314, 128036975, 588461276,
	// 1261952231, 335270959, 2103083663, 1193349371, 1271182361, 2003486859,
	// 1125758203, 846177677, 451607822, 1725137205, 1786748312, 1359644397,
	// 815136637, 459254967, 1434434198, 1135651930, 508348838, 503796204, 3981256,
	// 1972247162, 2017265611, 1586407799, 2132988125]

	public boolean registerRecipe(IRecipe recipe) {
		if (recipe == null)
			return false;
		if (recipe.getResult() == null || recipe.getResult().getType().equals(Material.AIR)) {
			plugin.getLogger().fine("Invalid recipe '" + recipe); //$NON-NLS-1$
			return false;
		}
		int hash = SerializedItem.of(recipe.getResult()).hashCode();
		String name = recipe.getRecipeName().replace(" ", "-"); //$NON-NLS-1$//$NON-NLS-2$

		recipesByName.computeIfAbsent(name, k -> new HashSet<>());
		recipesByName.get(name).add(recipe);
		recipesByHash.computeIfAbsent(hash, k -> new HashSet<>());
		recipesByHash.get(hash).add(recipe);

		if (recipe.isSuitableForAutoMatching())
			autoMatchingRecipes.add(recipe);
		else if (recipe.isVanilla())
			plugin.getLogger()
					.fine(String.format(
							"'%s' is not included in auto recipe matching (no unique ingredient identification)", //$NON-NLS-1$
							recipe.getRecipeName()));
		if (!recipe.isVanilla()) {
			customRecipes.add(recipe);
			cacheRecipe(recipe);
		}
		return true;
	}

	public IRecipe matchRecipe(IMatrix<SerializedItem> matrix) {
		long l = System.nanoTime();
		IRecipe match = cachedRecipes.get(
				Stream.of(matrix.getArray()).map(i -> i != null ? i.hashCode() : null).collect(Collectors.toList()));
		if (match == null) {
			List<Integer> hashs = Stream.of(matrix.getArray()).filter(i -> i != null).map(i -> i.hashCode())
					.collect(Collectors.toList());
			match = cachedRecipes.get(hashs);
			if (match == null) {
				Collections.sort(hashs);
				match = cachedRecipes.get(hashs);
				if (match == null)
					return null;
			}
		}
		IRecipe r = match.matches(matrix) ? match : null;
		System.out.println("matched in " + (System.nanoTime() - l) / 1000 + " micros");
		return r;
	}

	public void reloadRecipes() {
		new BukkitRunnable() {

			@Override
			public void run() {
				plugin.getLogger().info("Reloading Recipes..."); //$NON-NLS-1$
				long now = System.nanoTime();
				customRecipes.clear();
				recipesByName.clear();
				recipesByHash.clear();
				autoMatchingRecipes.clear();
				loadRecipes();
				plugin.getLogger()
						.info("Reloaded! " + (MathsUtils.round((System.nanoTime() - now) / 1000000000D, 3)) + "s"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}.runTaskAsynchronously(plugin);
	}

	public Set<IRecipe> getAutoMatchingRecipes() {
		return Collections.unmodifiableSet(autoMatchingRecipes);
	}

	public Set<IRecipe> getCustomRecipes() {
		return Collections.unmodifiableSet(customRecipes);
	}

	public Set<IRecipe> getRecipeByName(String name) {
		if (recipesByName.get(name) == null)
			return null;
		return Collections.unmodifiableSet(recipesByName.get(name));
	}

	@SuppressWarnings("nls")
	public void printRecipe(IRecipe recipe) {
		plugin.getLogger().fine("Name: " + recipe.getRecipeName());
		plugin.getLogger().fine("Result: " + recipe.getResult());
		plugin.getLogger().fine("Width: " + recipe.getWidth());
		plugin.getLogger().fine("Height: " + recipe.getHeight());
		plugin.getLogger().fine("Type: " + recipe.getClass().getSimpleName());
		plugin.getLogger()
				.fine("Ingredients: \n"
						+ (recipe instanceof IShapedRecipe
								? String.join("\n",
										IRecipe.ingredientsToListColorless(plugin,
												recipe.getIngredients()
														.toArray(new ItemStack[recipe.getIngredients().size()]),
												recipe.getWidth(), recipe.getHeight(),
												plugin.getWorkspaceObjects().getViewIngredientsIdFormat()))
								: ((IShapelessRecipe) recipe).getIngredients()));
	}

	public boolean isBlacklisted(List<Integer> hashs) {
		if (blacklistedRecipes.containsKey(hashs))
			return true;
		hashs = hashs.stream().filter(i -> i != null).collect(Collectors.toList());
		Collections.sort(hashs);
		return blacklistedRecipes.containsKey(hashs);
	}

	public Set<List<Integer>> getBlacklistedRecipesHashes() {
		return blacklistedRecipes.keySet();
	}

	public Map<List<Integer>, IRecipe> getBlacklistedRecipes() {
		return blacklistedRecipes;
	}

	public void addBlacklistedRecipe(IRecipe disabled) {
		blacklistedRecipes.put(disabled.getHashMatrix(), disabled);
		plugin.getLogger()
				.info("Loaded blacklisted recipe: " + disabled.getRecipeName() + " (" + disabled.getUUID() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void loadBlacklistedRecipes() {
		plugin.getLogger().info("Loading blacklisted recipes..."); //$NON-NLS-1$
		try {
			blacklistCfg.load(blacklistFile);
		} catch (IOException | InvalidConfigurationException e1) {
			plugin.getLogger().log(Level.SEVERE, "Could not load blacklist file", e1); //$NON-NLS-1$
			return;
		}
		blacklistedRecipes.clear();
		for (String key : blacklistCfg.getKeys(false)) {
			try {
				IRecipe recipe = IRecipe.getRecipeFromFile(blacklistFile, blacklistCfg, key);
				plugin.getLogger().fine("-----------------------------------------------------"); //$NON-NLS-1$
				printRecipe(recipe);
				plugin.getLogger().fine("-----------------------------------------------------"); //$NON-NLS-1$
				addBlacklistedRecipe(recipe);
			} catch (IOException | InvalidConfigurationException e) {
				plugin.getLogger().log(Level.SEVERE, "Could not load recipe with key " + key, e); //$NON-NLS-1$
			}
		}
		plugin.getLogger().info("Loaded blacklisted recipes!"); //$NON-NLS-1$
	}

	public void createRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String id, UUID uuid, int width,
			int height, RecipeCategory category) throws InvalidRecipeException {
		saveRecipe(result, ingredients, shaped, id, uuid, width, height, category);
		reloadRecipes();
	}

	public void saveRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String name, UUID uuid, int width,
			int height, RecipeCategory category) throws InvalidRecipeException {
		try {
			recipeCfg.load(recipeFile);
			if (shaped)
				new IShapedRecipe(ingredients, width, height, result, name, uuid, category).saveToFile(recipeCfg,
						uuid.toString());
			else
				new IShapelessRecipe(Arrays.asList(ingredients), result, name, uuid, category).saveToFile(recipeCfg,
						uuid.toString());
			recipeCfg.save(recipeFile);
		} catch (Exception ex) {
			throw new InvalidRecipeException(ex);
		}
	}

	public void saveRandomRecipe(ItemStack result, ItemStack[] ingredients, boolean shaped, String name, UUID uuid,
			int width, int height, Map<ItemStack, Integer> rngMap, RecipeCategory category)
			throws InvalidRecipeException {
		try {
			recipeCfg.load(recipeFile);
			if (shaped)
				new IRandomShapedRecipe(ingredients, width, height, result, name, uuid, rngMap, category)
						.saveToFile(recipeCfg, uuid.toString());
			else
				new IRandomShapelessRecipe(Arrays.asList(ingredients), result, name, uuid, rngMap, category)
						.saveToFile(recipeCfg, uuid.toString());
			recipeCfg.save(recipeFile);
		} catch (Exception ex) {
			throw new InvalidRecipeException(ex);
		}
	}

	public void loadRecipes() {
		loadBukkitRecipes();
		loadCustomRecipes();
		loadBlacklistedRecipes();
	}

	public void clear() {
		customRecipes.clear();
		autoMatchingRecipes.clear();
		recipesByName.clear();
		recipesByHash.clear();
	}

	public void loadCustomRecipes() {
		plugin.getLogger().info("Loading custom recipes..."); //$NON-NLS-1$
		try {
			recipeCfg.load(recipeFile);
		} catch (IOException | InvalidConfigurationException e1) {
			plugin.getLogger().log(Level.SEVERE, "Could not load recipes file", e1); //$NON-NLS-1$
			return;
		}
		for (String key : recipeCfg.getKeys(false)) {
			try {
				IRecipe recipe = IRecipe.getRecipeFromFile(recipeFile, recipeCfg, key);
				plugin.getLogger().fine("-----------------------------------------------------"); //$NON-NLS-1$
				printRecipe(recipe);
				plugin.getLogger().fine("-----------------------------------------------------"); //$NON-NLS-1$
				registerRecipe(recipe);
				plugin.getLogger()
						.info("Loaded custom recipe: " + recipeCfg.getString(key + ".name") + " (" + key + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			} catch (IOException | InvalidConfigurationException e) {
				plugin.getLogger().log(Level.SEVERE, "Could not load recipe with key " + key, e); //$NON-NLS-1$
			}
		}
		plugin.getLogger().info("Loaded custom recipes!"); //$NON-NLS-1$
	}

	public void delete(String recipeName) throws RecipeDeleteException {
		try {
			recipeCfg.load(recipeFile);
			recipeCfg.set(recipeName, null);
			recipeCfg.save(recipeFile);
			reloadRecipes();
		} catch (Exception ex) {
			throw new RecipeDeleteException(recipeName, ex);
		}
	}
}