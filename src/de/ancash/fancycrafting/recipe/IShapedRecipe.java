package de.ancash.fancycrafting.recipe;

import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;

import de.ancash.minecraft.SerializableItemStack;

public class IShapedRecipe extends IRecipe {

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(!(obj instanceof IShapedRecipe)) return false;
		return serializableResult.equals(((IShapedRecipe)obj).serializableResult) &&
				IRecipe.matchesShaped(ingredientsMap, ((IShapedRecipe) obj).getIngredientsMap());
	}
	
	private final Map<Integer, SerializableItemStack> ingredientsMap;
	
	public IShapedRecipe(SerializableItemStack result, Map<Integer, SerializableItemStack> ingredientsMap, String id) {
		super(result.restore(), ingredientsMap.values(), id);
		this.ingredientsMap = ingredientsMap;
	}
	
	public IShapedRecipe(ItemStack result, Map<Character, ItemStack> ingredientsMap, String[] shapes) {
		super(result, ingredientsMap.values().stream().filter(entry -> entry != null).map(SerializableItemStack::new).collect(Collectors.toList()), null);
		this.ingredientsMap = toMap(ingredientsMap, shapes);
	}

	public Map<Integer, SerializableItemStack> getIngredientsMap() {
		return ingredientsMap;
	}
	
	@Override
	public ItemStack getResult() {
		return result.clone();
	}
}