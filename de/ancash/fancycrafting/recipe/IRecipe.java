package de.ancash.fancycrafting.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import de.ancash.minecraft.IItemStack;
import de.ancash.minecraft.XMaterial;

public abstract class IRecipe {

	private final ItemStack result;
	private final String name;
	private final boolean vanilla;
	private UUID uuid;
	
	public IRecipe(ItemStack result, String name, UUID uuid) {
		this(result, name, false);
		this.uuid = uuid;
	}
	
	public IRecipe(ItemStack result, String name, boolean vanilla) {
		if(name == null && !vanilla)
			throw new IllegalArgumentException("Custom recipe must have a name");
		this.result = result;
		this.vanilla = vanilla;
		if(vanilla) 
			this.name = XMaterial.matchXMaterial(result).toString();
		else 
			this.name = name;
	}
		
	public abstract int getHeight();
	
	public abstract int getWidth();
	
	public abstract int getIngredientsSize();
	
	public ItemStack getResult() {
		return result;
	}

	public boolean isVanilla() {
		return vanilla;
	}

	public String getName() {
		return name;
	}

	public static boolean matchesShaped(IShapedRecipe recipe, ItemStack[] ings, int width, int height) {
		if(recipe.getWidth() != width || recipe.getHeight() != height) 
			return false;
		for(int i = 0; i<ings.length; i++) {
			if((recipe.getIngredientsArray()[i] == null) != (ings[i] == null))
				return false;
			if(ings[i] == null) continue;
			if(!new IItemStack(recipe.getIngredientsArray()[i]).isSimilar(ings[i]) || ings[i].getAmount() < recipe.getIngredientsArray()[i].getAmount())
				return false;
		}
		return true;
	}
	
	public static boolean matchesShapeless(ItemStack[] originals, ItemStack[] ingredients) {
		List<ItemStack> origs = Arrays.asList(originals).stream().filter(i -> i != null).collect(Collectors.toList());
		List<ItemStack> ings = Arrays.asList(ingredients).stream().filter(i -> i != null).collect(Collectors.toList());
		
		if(ings.size() != origs.size()) return false;
		
		for(int i1 = 0; i1<origs.size(); i1++) {
			boolean matches = false;
			for(int i2 = 0; i2<ings.size() ;i2++) {
				if(new IItemStack(origs.get(i1)).isSimilar(ings.get(i2)) && origs.get(i1).getAmount() <= ings.get(i2).getAmount()) {
					matches = true;
					ings.remove(i2);
					break;
				}
			}
			if(!matches) return false;
		}
		return true;
	}

	public static IRecipe fromVanillaRecipe(Recipe v) {
		if(v == null)
			return null;
		IRecipe r = null;
		try {
			if(v instanceof ShapedRecipe) {
				ShapedRecipe s = (ShapedRecipe) v;
				ItemStack[] ings = new ItemStack[9];
				for(int a = 0; a<s.getShape().length; a++) {
					String str = s.getShape()[a];
					for(int b = 0; b<str.length(); b++)
						ings[a * 3 + b] = s.getIngredientMap().get(str.charAt(b));
				}
				r = new IShapedRecipe(ings, 3, 3, v.getResult(), null, true);
			} else if(v instanceof ShapelessRecipe) {
				ShapelessRecipe s = (ShapelessRecipe) v;
				r = new IShapelessRecipe(s.getIngredientList(), v.getResult(), null, true);
			}
		} catch(Exception e) {
			List<ItemStack> ings = null;
			if(v instanceof ShapedRecipe) {
				ings = new ArrayList<>(((ShapedRecipe) v).getIngredientMap().values());
			} else if(v instanceof ShapelessRecipe) {
				ings = ((ShapelessRecipe) v).getIngredientList();
			}
			System.err.println("Could not load Bukkit recipe w/ result: " + v.getResult() + " & ingredients: " + ings);
			r = null;
		}
		return r;
	}

	public UUID getUUID() {
		return uuid;
	}
	
	public abstract List<ItemStack> getIngredients();
	
	@Override
	public String toString() {
		return "IRecipe{name=" + name + ";result=" + result + ";ingredients=" + getIngredients() + "}";
	}
}