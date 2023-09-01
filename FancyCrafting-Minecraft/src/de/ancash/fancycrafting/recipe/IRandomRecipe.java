package de.ancash.fancycrafting.recipe;

import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import de.ancash.datastructures.tuples.Duplet;
import de.ancash.nbtnexus.serde.SerializedItem;

public interface IRandomRecipe {

	public List<Duplet<SerializedItem, Integer>> getProbabilityList();

	public Map<ItemStack, Integer> getProbabilityMap();

	public int getProbabilitySum();

	public SerializedItem getRandom();
}