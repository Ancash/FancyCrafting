package de.ancash.fancycrafting;

import de.ancash.nbtnexus.NBTNexus;
import de.ancash.nbtnexus.NBTTag;

public final class NBTKeys {

	public static final String FANCY_CRAFTING_NBT_COMPOUND_TAG = String.join(NBTNexus.SPLITTER, "FancyCrafting",
			NBTTag.COMPOUND.name());

	public static final String AUTO_RECIPES_TAG = String.join(".", FANCY_CRAFTING_NBT_COMPOUND_TAG,
			String.join(NBTNexus.SPLITTER, "AutoRecipes", NBTTag.LIST.name()));

}
