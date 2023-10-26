package de.ancash.fancycrafting.recipe.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IMatrix;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.fancycrafting.recipe.RecipeCategory;
import de.ancash.nbtnexus.serde.SerializedItem;
import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.recipes.CraftingRecipeShaped;
import me.wolfyscript.customcrafting.recipes.CraftingRecipeShapeless;
import me.wolfyscript.customcrafting.recipes.CustomRecipe;
import me.wolfyscript.customcrafting.recipes.items.Ingredient;
import me.wolfyscript.utilities.util.NamespacedKey;

public class WolfyCustomCraftingMatcher {

	@SuppressWarnings("nls")
	public static final String CATEGORY = "WolfyCustomCraftingCache";

	@SuppressWarnings({ "rawtypes", "unchecked", "nls" })
	public static void match(Recipe vanilla, IMatrix<SerializedItem> matrix) {
		Keyed k = (Keyed) vanilla;
		if (Bukkit.getPluginManager().getPlugin("CustomCrafting") != null
				&& k.getKey().getNamespace().equals("customcrafting")) {
			org.bukkit.NamespacedKey bukkitKey = k.getKey();
			NamespacedKey nk = new NamespacedKey(bukkitKey.getNamespace(),
					bukkitKey.getKey().replace("cc_placeholder.", "").replace("cc_display.", ""));

			CustomRecipe<?> cr = CustomCrafting.inst().getRegistries().getRecipes().get(nk);

			if (cr == null)
				return;

			matrix.optimize();

			if (cr instanceof CraftingRecipeShaped) {
				CraftingRecipeShaped crs = (CraftingRecipeShaped) cr;
				List[] rows = new List[crs.getMaxGridDimension() * crs.getMaxGridDimension()];
				Map<Character, Ingredient> mappedIngs = crs.getMappedIngredients();
				for (int a = 0; a < crs.getShape().length; a++) {
					String row = crs.getShape()[a];
					for (int b = 0; b < row.length(); b++)
						rows[a * crs.getMaxGridDimension() + b] = mappedIngs.get(row.charAt(b)).getBukkitChoices()
								.stream().map(SerializedItem::of).collect(Collectors.toList());
				}
				IMatrix<List<SerializedItem>> wolfy = new IMatrix<>(rows, crs.getMaxGridDimension(),
						crs.getMaxGridDimension());
				wolfy.optimize();
				List<SerializedItem>[] wolfyArr = wolfy.getArray();
				SerializedItem[] inputArr = matrix.getArray();
				ItemStack[] ings = new ItemStack[wolfy.getHeight() * wolfy.getWidth()];
				for (int a = 0; a < wolfy.getHeight(); a++) {
					for (int b = 0; b < wolfy.getWidth(); b++) {
						int pos = a * wolfy.getWidth() + b;
						if ((wolfyArr[pos] == null) != (inputArr[pos] == null))
							throw new IllegalStateException(
									Arrays.asList(wolfyArr) + " & " + Arrays.asList(inputArr) + " differ at " + pos);
						List<SerializedItem> orig = wolfyArr[pos];
						SerializedItem in = inputArr[pos];
						List<SerializedItem> match = orig.stream().filter(f -> f.areEqualIgnoreAmount(f))
								.collect(Collectors.toList());
						if (match.isEmpty())
							throw new IllegalStateException("no match for " + in + " in " + orig);
						ings[pos] = match.get(0).toItem();
					}
				}
				FancyCrafting.registerRecipe(
						new IShapedRecipe(ings, wolfy.getWidth(), wolfy.getHeight(), cr.getResult().getItemStack(),
								nk.getKey(), UUID.randomUUID(), RecipeCategory.getOrCreateCategory(CATEGORY)));
				FancyCrafting.getPlugin(FancyCrafting.class).getLogger()
						.info("Loaded shaped recipe " + nk.getKey() + " from CustomCrafting");
			} else if (cr instanceof CraftingRecipeShapeless) {
				CraftingRecipeShapeless crs = (CraftingRecipeShapeless) cr;
				List<List<SerializedItem>> ings = crs.getIngredients().stream().map(Ingredient::getBukkitChoices)
						.map(l -> l.stream().map(SerializedItem::of).collect(Collectors.toList()))
						.collect(Collectors.toList());
				List<SerializedItem> input = Arrays.asList(matrix.getArray()).stream().filter(s -> s != null)
						.collect(Collectors.toList());
				List<SerializedItem> converted = new ArrayList<>();
				for (SerializedItem in : input) {
					Iterator<List<SerializedItem>> iter = ings.iterator();
					boolean match = false;
					while (iter.hasNext()) {
						List<SerializedItem> comp = iter.next();
						Optional<SerializedItem> opt = comp.stream().filter(c -> c.areEqualIgnoreAmount(in))
								.findFirst();
						if (opt.isPresent()) {
							iter.remove();
							match = true;
							converted.add(opt.get());
							break;
						}
					}

					if (!match)
						return;
				}
				FancyCrafting.registerRecipe(new IShapelessRecipe(
						converted.stream().map(SerializedItem::toItem).collect(Collectors.toList()),
						crs.getResult().getItemStack(), nk.getKey(), UUID.randomUUID(),
						RecipeCategory.getOrCreateCategory(CATEGORY)));
				FancyCrafting.getPlugin(FancyCrafting.class).getLogger()
						.info("Loaded shapeless recipe " + nk.getKey() + " from CustomCrafting");
			}
		}
	}

}
