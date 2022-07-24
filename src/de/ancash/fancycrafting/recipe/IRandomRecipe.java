package de.ancash.fancycrafting.recipe;

import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import de.ancash.datastructures.tuples.Duplet;
import de.ancash.minecraft.IItemStack;

public interface IRandomRecipe {

	public List<Duplet<IItemStack, Integer>> getProbabilityList();

	public Map<ItemStack, Integer> getProbabilityMap();

	public int getProbabilitySum();

	public IItemStack getRandom();
}