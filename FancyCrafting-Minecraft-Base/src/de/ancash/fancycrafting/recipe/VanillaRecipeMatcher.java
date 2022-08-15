package de.ancash.fancycrafting.recipe;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.crafting.IContainerWorkbench;
import de.ancash.minecraft.crafting.ICraftingManager;

public class VanillaRecipeMatcher {

	private static final ConcurrentHashMap<List<Integer>, Optional<IRecipe>> cache = new ConcurrentHashMap<>();

	private final IContainerWorkbench icw;
	private final AbstractFancyCrafting pl;

	public VanillaRecipeMatcher(AbstractFancyCrafting pl, Player player) {
		icw = ICraftingManager.getSingleton().newInstance(player);
		this.pl = pl;
	}

	public IContainerWorkbench getContainerWorkbench() {
		return icw;
	}

	public IRecipe matchVanillaRecipe(IMatrix<IItemStack> matrix) {
		matrix = matrix.clone();
		if (matrix.getArray().length != 9) {
			matrix.cut(3, 3);
			if (matrix.getArray().length != 9)
				return null;
		}

		IItemStack[] arr = matrix.getArray();

		List<Integer> key = Stream.of(arr).map(i -> i != null ? i.hashCode() : null).collect(Collectors.toList());
		if (!cache.containsKey(key)) {
			Recipe vanilla = icw.getRecipe(arr, key);
			if (vanilla == null)
				cache.put(key, Optional.empty());
			else
				cache.put(key, Optional.ofNullable(IRecipe.fromVanillaRecipe(pl, vanilla, true)));
		}
		return cache.get(key).orElse(null);
	}
}