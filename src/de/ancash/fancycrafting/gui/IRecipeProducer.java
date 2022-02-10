package de.ancash.fancycrafting.gui;

import org.bukkit.inventory.ItemStack;

import de.ancash.minecraft.ItemBuilder;
import de.ancash.minecraft.XMaterial;

public interface IRecipeProducer {


	static ItemStack SHAPELESS = new ItemBuilder(XMaterial.BLUE_STAINED_GLASS_PANE).setDisplayname("§bShapeless Recipe").build();
	static ItemStack SHAPED = new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).setDisplayname("§aShaped recipe").build();
	static ItemStack SAVE = new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).setDisplayname("§aSave Recipe").build();
	static ItemStack EDIT = new ItemBuilder(XMaterial.WRITABLE_BOOK).setDisplayname("§aEdit Recipe").build();
	static ItemStack DELETE = new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE).setDisplayname("§cDelete Recipe").build();
}
