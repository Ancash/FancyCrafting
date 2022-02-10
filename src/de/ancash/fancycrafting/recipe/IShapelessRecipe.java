package de.ancash.fancycrafting.recipe;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;

public class IShapelessRecipe extends IRecipe{

	private final List<ItemStack> ings;
	private final int matrix;
	
	public IShapelessRecipe(Collection<ItemStack> ings , ItemStack result, String name, UUID uuid) {
		super(result, name, uuid);
		this.ings = Collections.unmodifiableList(ings.stream().filter(i -> i != null).collect(Collectors.toList()));
		int tmp = 1;
		while(tmp * tmp < ings.size())
			tmp++;
		matrix = tmp;
	}
	
	public IShapelessRecipe(Collection<ItemStack> ings, ItemStack result, String name, boolean vanilla) {
		super(result, name, vanilla);
		this.ings = Collections.unmodifiableList(ings.stream().filter(i -> i != null).collect(Collectors.toList()));
		int tmp = 1;
		while(tmp * tmp < ings.size())
			tmp++;
		matrix = tmp;
	}
	
	public List<ItemStack> getIngredients() {
		return ings;
	}

	@Override
	public int getIngredientsSize() {
		return ings.size();
	}

	@Override
	public int getMatrix() {
		return matrix;
	}
}		