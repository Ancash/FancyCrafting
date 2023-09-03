package de.ancash.fancycrafting;

import de.ancash.nbtnexus.NBTNexus;
import de.ancash.nbtnexus.NBTTag;

@SuppressWarnings("nls")
public final class NBTKeys {

	public static final String BASE_COMPOUND_TAG = String.join(NBTNexus.SPLITTER, "FancyCrafting",
			NBTTag.COMPOUND.name());

	public static final String AUTO_RECIPES_COMPOUND_TAG = String.join(NBTNexus.SPLITTER, "AutoRecipes",
			NBTTag.COMPOUND.name());

	public static final String AUTO_RECIPES_RESULTS_TAG = String.join(NBTNexus.SPLITTER, "Results",
			NBTTag.ITEM_STACK_LIST.name());

	public static final String AUTO_RECIPES_SLOTS_TAG = String.join(NBTNexus.SPLITTER, "Slots", NBTTag.INT.name());

	public static final String AUTO_RECIPES_COMPOUND_PATH = String.join(".", BASE_COMPOUND_TAG,
			AUTO_RECIPES_COMPOUND_TAG);

	public static final String AUTO_RECIPES_RESULTS_PATH = String.join(".", AUTO_RECIPES_COMPOUND_PATH,
			AUTO_RECIPES_RESULTS_TAG);

	public static final String AUTO_RECIPES_SLOTS_PATH = String.join(".", AUTO_RECIPES_COMPOUND_PATH,
			AUTO_RECIPES_SLOTS_TAG);
}
