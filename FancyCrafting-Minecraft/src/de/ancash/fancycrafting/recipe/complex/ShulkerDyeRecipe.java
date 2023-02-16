package de.ancash.fancycrafting.recipe.complex;

import java.util.Collection;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.crafting.recipe.ComplexRecipeWrapper;
import de.ancash.minecraft.cryptomorin.xseries.XMaterial;

public class ShulkerDyeRecipe extends IShapelessRecipe implements IComplexRecipe {

	@SuppressWarnings("nls")
	public ShulkerDyeRecipe(Collection<ItemStack> ings, ItemStack result) {
		this(ings, result,
				"shulker-dye-recipe." + ItemStackUtils.getDisplayName(result).toLowerCase().replace(" ", "-"));
	}

	public ShulkerDyeRecipe(Collection<ItemStack> ings, ItemStack result, String name) {
		super(ings, result, name, true, false);
	}

	@Override
	public Set<XMaterial> getIgnoredMaterials() {
		return ComplexRecipeWrapper.ComplexRecipeType.SHULKER_DYE.getIgnoredMaterials();
	}

}
