package de.ancash.fancycrafting.recipe.crafting;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.minecraft.crafting.IContainerWorkbench;
import de.ancash.minecraft.crafting.ICraftingManager;
import de.ancash.nbtnexus.serde.SerializedItem;

public class VanillaRecipeMatcher {

	private static final ConcurrentHashMap<List<Integer>, Optional<IRecipe>> cache = new ConcurrentHashMap<>();

	public static void clearCache() {
		cache.clear();
	}

	private final IContainerWorkbench icw;
	private final FancyCrafting pl;

	public VanillaRecipeMatcher(FancyCrafting pl, Player player) {
		icw = ICraftingManager.getSingleton().newInstance(player);
		this.pl = pl;
	}

	public IContainerWorkbench getContainerWorkbench() {
		return icw;
	}

	@SuppressWarnings("nls")
	public IRecipe matchVanillaRecipe(IMatrix<SerializedItem> matrix) {
		matrix = matrix.clone();
		if (matrix.getArray().length != 9) {
			matrix.cut(3, 3);
			if (matrix.getArray().length != 9)
				return null;
		}

		SerializedItem[] arr = matrix.getArray();
		List<Integer> key = Stream.of(arr).map(i -> i != null ? i.hashCode() : null).collect(Collectors.toList());
		if (!cache.containsKey(key)) {
			Recipe vanilla = icw.getRecipe(
					Arrays.asList(arr).stream().map(i -> i == null ? null : i.toItem()).toArray(ItemStack[]::new), key);
			if (vanilla == null)
				cache.put(key, Optional.empty());
			else {
				if (vanilla instanceof Keyed) {
//					Keyed k = (Keyed) vanilla;
//					if (Bukkit.getPluginManager().getPlugin("CustomCrafting") != null
//							&& k.getKey().getNamespace().equals("customcrafting")) {
//						WolfyCustomCraftingMatcher.match(vanilla, matrix);
//						cache.put(key, Optional.empty());
//						return null;
//					}
				}
				cache.put(key, Optional.ofNullable(IRecipe.fromVanillaRecipe(pl, vanilla, true)));
			}
		}
		return cache.get(key).orElse(null);
	}
}