package de.ancash.fancycrafting.gui;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.CraftingTemplate;
import de.ancash.fancycrafting.FancyCrafting;

public class ICraftingGUI extends AbstractCraftingGUI{
		
	public ICraftingGUI(FancyCrafting pl, Player player, CraftingTemplate template) {
		super(pl, player, template);
	}

	@Override
	public void onNoPermission() {
		onNoRecipeMatch();
	}

	@Override
	public void onRecipeMatch() {
		setItem(getCurrentRecipe().getResult(), getResultSlot());
		for(int i : template.getCraftStateSlots())
			setItem(pl.getValidItem(), i);
	}

	@Override
	public void onNoRecipeMatch() {
		setItem(pl.getInvalid(), getResultSlot());
		for(int i : template.getCraftStateSlots())
			setItem(pl.getInvalid(), i);
	}
}