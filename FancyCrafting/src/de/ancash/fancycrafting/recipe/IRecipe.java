package de.ancash.fancycrafting.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.XMaterial;

public abstract class IRecipe {

	private final IItemStack result;
	private final String name;
	private final boolean vanilla;
	private final boolean suitableForAutoMatching;
	private UUID uuid;

	public IRecipe(ItemStack result, String name, UUID uuid) {
		this(result, name, false, true);
		this.uuid = uuid;
	}

	public IRecipe(ItemStack result, String name, boolean vanilla, boolean suitableForAutoMatching) {
		if (name == null && !vanilla)
			throw new IllegalArgumentException("Custom recipe must have a name");
		this.result = new IItemStack(result);
		this.vanilla = vanilla;
		if (vanilla)
			this.suitableForAutoMatching = suitableForAutoMatching;
		else
			this.suitableForAutoMatching = true;
		if (vanilla)
			this.name = XMaterial.matchXMaterial(result).toString();
		else
			this.name = name;
	}

	public abstract int getHeight();

	public abstract int getWidth();

	public abstract int getIngredientsSize();

	public abstract Map<Integer, Integer> getIngredientsHashCodes();

	public abstract List<ItemStack> getIngredients();
	
	public abstract List<IItemStack> getIIngredients();

	public ItemStack getResult() {
		return result.getOriginal();
	}

	public IItemStack getResultAsIItemStack() {
		return result;
	}

	public boolean isVanilla() {
		return vanilla;
	}

	public String getName() {
		return name;
	}

	public UUID getUUID() {
		return uuid;
	}

	public boolean isSuitableForAutoMatching() {
		return suitableForAutoMatching;
	}

	public static boolean matchesShaped(IShapedRecipe recipe, IItemStack[] ings, int width, int height) {
		if (recipe.getWidth() != width || recipe.getHeight() != height)
			return false;
		for (int i = 0; i < ings.length; i++) {
			if ((recipe.asArray()[i] == null) != (ings[i] == null))
				return false;
			if (ings[i] == null)
				continue;
			if (recipe.asArray()[i].hashCode() != ings[i].hashCode()
					|| ings[i].getOriginal().getAmount() < recipe.getIngredientsArray()[i].getOriginal().getAmount())
				return false;
		}
		return true;
	}

	public static boolean matchesShapeless(IShapelessRecipe recipe, IItemStack[] ingredients) {
		List<IItemStack> origs = recipe.getIIngredients();
		List<IItemStack> ings = Arrays.asList(ingredients).stream().filter(i -> i != null).collect(Collectors.toList());

		if (ings.size() != origs.size())
			return false;

		for (int i1 = 0; i1 < origs.size(); i1++) {
			boolean matches = false;
			for (int i2 = 0; i2 < ings.size(); i2++) {
				if (origs.get(i1).hashCode() == ings.get(i2).hashCode()
						&& origs.get(i1).getOriginal().getAmount() <= ings.get(i2).getOriginal().getAmount()) {
					matches = true;
					ings.remove(i2);
					break;
				}
			}
			if (!matches)
				return false;
		}
		return true;
	}

	public static IRecipe fromVanillaRecipe(FancyCrafting pl, Recipe v) {
		if (v == null)
			return null;
		IRecipe r = null;
		try {
			AtomicBoolean suitableForAutoMatching = new AtomicBoolean(true);
			if (v instanceof ShapedRecipe) {
				ShapedRecipe s = (ShapedRecipe) v;
				ItemStack[] ings = new ItemStack[9];
				for (int a = 0; a < s.getShape().length; a++) {
					String str = s.getShape()[a];
					for (int b = 0; b < str.length(); b++) {
						suitableForAutoMatching.compareAndSet(true,
								isSuitableForAutoMatching(s.getIngredientMap().get(str.charAt(b))));
						ings[a * 3 + b] = removeUnspecificMeta(s, s.getIngredientMap().get(str.charAt(b)));
					}
				}
				r = new IShapedRecipe(ings, 3, 3, v.getResult(), null, true, suitableForAutoMatching.get());
			} else if (v instanceof ShapelessRecipe) {
				ShapelessRecipe s = (ShapelessRecipe) v;
				r = new IShapelessRecipe(s.getIngredientList().stream().map(i -> {
					suitableForAutoMatching.compareAndSet(true, isSuitableForAutoMatching(i));
					return removeUnspecificMeta(s, i);
				}).collect(Collectors.toList()), v.getResult(), null, true, suitableForAutoMatching.get());
			}
		} catch (Exception e) {
			List<ItemStack> ings = null;
			if (v instanceof ShapedRecipe) {
				ings = new ArrayList<>(((ShapedRecipe) v).getIngredientMap().values());
			} else if (v instanceof ShapelessRecipe) {
				ings = ((ShapelessRecipe) v).getIngredientList();
			}
			pl.getLogger().log(Level.SEVERE, "Could not load Bukkit recipe w/ result: " + v.getResult() + " & ingredients: " + ings, e);
			r = null;
		}
		return r;
	}

	@SuppressWarnings("deprecation")
	private static boolean isSuitableForAutoMatching(ItemStack i) {
		if (i == null)
			return true;
		return i.getDurability() != Short.MAX_VALUE;
	}

	@SuppressWarnings("deprecation")
	private static ItemStack removeUnspecificMeta(Recipe r, ItemStack is) {
		if (is == null)
			return null;
		if (is.getItemMeta().toString().toLowerCase().contains("unspecific") && is.getDurability() == 0)
			is.setItemMeta(null);
		return is;
	}

	@Override
	public String toString() {
		return "IRecipe{name=" + name + ";result=" + result + ";ingredients=" + getIngredients() + "}";
	}

	public static IItemStack[] toIItemStackArray(ItemStack[] from) {
		return Arrays.asList(from).stream().map(item -> item == null ? null : new IItemStack(item))
				.toArray(IItemStack[]::new);
	}
}