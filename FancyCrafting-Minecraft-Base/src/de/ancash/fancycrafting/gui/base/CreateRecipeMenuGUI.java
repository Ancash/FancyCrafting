package de.ancash.fancycrafting.gui.base;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import de.ancash.datastructures.tuples.Tuple;
import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.minecraft.inventory.IGUI;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;
import de.ancash.minecraft.inventory.input.StringInputGUI;

public class CreateRecipeMenuGUI extends IGUI {

	public CreateRecipeMenuGUI(AbstractFancyCrafting pl, Player player, String name) {
		super(player.getUniqueId(), 9, pl.getWorkspaceObjects().getCreateRecipeTitle());
		for (int i = 0; i < getSize(); i++)
			setItem(pl.getWorkspaceObjects().getBackgroundItem().getOriginal(), i);

		addInventoryItem(new InventoryItem(this, pl.getWorkspaceObjects().getCreateNormalRecipeItem().getOriginal(), 2,
				(a, b, c, top) -> Optional.ofNullable(top ? this : null)
						.ifPresent(self -> pl.createNormalRecipe(player, name))));

		addInventoryItem(new InventoryItem(this, pl.getWorkspaceObjects().getCreateRandomRecipeItem().getOriginal(), 6,
				(a, b, c, top) -> Optional.ofNullable(top ? this : null)
						.ifPresent(self -> pl.createRandomRecipe(player, name))));

		IGUIManager.register(this, getId());
		open();
	}

	public static void open(AbstractFancyCrafting plugin, Player owner) {
		new StringInputGUI(plugin, owner,
				(text) -> Bukkit.getScheduler().runTaskLater(plugin, () -> new CreateRecipeMenuGUI(plugin, owner, text),
						1),
				(text) -> Tuple.of(text != null && !text.isEmpty(),
						text == null || text.isEmpty() ? plugin.getResponse().INVALID_RECIPE_NAME : null))
				.setLeft(plugin.getWorkspaceObjects().getInputRecipeNameLeftItem().getOriginal())
				.setRight(plugin.getWorkspaceObjects().getInputRecipeNameRightItem().getOriginal())
				.setTitle(plugin.getWorkspaceObjects().getInputRecipeNameTitle()).setText("").open(); //$NON-NLS-1$
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
		IGUIManager.remove(getId());
	}

	@Override
	public void onInventoryDrag(InventoryDragEvent event) {
		event.setCancelled(true);
	}
}
