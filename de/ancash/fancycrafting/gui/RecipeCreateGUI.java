package de.ancash.fancycrafting.gui;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.fancycrafting.CraftingTemplate;
import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.anvilgui.AnvilGUI;
import de.ancash.minecraft.inventory.IGUI;
import de.ancash.minecraft.inventory.IGUIManager;

public class RecipeCreateGUI extends IGUI {
	
	private final CraftingTemplate template;
	private final FancyCrafting plugin;
	private final String recipeName;
	
	public RecipeCreateGUI(FancyCrafting pl, Player player, String name) {
		super(player.getUniqueId(), CraftingTemplate.get(8, 6).getSize(), pl.getCreateRecipeTitle());
		this.template = CraftingTemplate.get(8, 6);
		for(int i = 0; i<getSize(); i++)
			setItem(pl.getBackgroundItem(), i);
		for(int i : template.getCraftingSlots())
			setItem(null, i);
		setItem(null, template.getResultSlot());
		setItem(pl.getSaveItem(), template.getSaveSlot());
		setItem(pl.getShapedItem(), template.getRecipeTypeSlot());
		this.recipeName = name;
		this.plugin = pl;
		IGUIManager.register(this, getId());
		player.openInventory(getInventory());
	}
	
	private boolean isCraftingSlot(int a) {
		for(int i : template.getCraftingSlots()) if(i == a) return true;
		return a == template.getResultSlot();
	}
	
	public static void open(FancyCrafting plugin, Player owner) {
		new AnvilGUI.Builder()
			.itemLeft(XMaterial.DIAMOND_SWORD.parseItem().clone())
			.onComplete((player, text) ->{
				if(text != null) {
					new BukkitRunnable() {
						
						@Override
						public void run() {
							new RecipeCreateGUI(plugin, owner, text);
						}
					}.runTask(plugin);
				} else {
					player.sendMessage("§cInvalid recipe name!");
				}
				return AnvilGUI.Response.close();
			})
			.plugin(plugin)
			.open((Player) owner);
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		InventoryAction a = event.getAction();
		if(event.getClick().equals(ClickType.DOUBLE_CLICK) || (a.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && !event.getInventory().equals(event.getClickedInventory()))) {
			event.setCancelled(true);
			return;
		}
		if(event.getInventory().equals(event.getClickedInventory())) {
			int slot = event.getSlot();
			if(isCraftingSlot(slot)) {
				return;
			} else {
				event.setCancelled(true);
			}
			boolean isShaped = event.getInventory().getItem(template.getRecipeTypeSlot()).getItemMeta().getDisplayName().contains("Shaped");
			if(slot == template.getRecipeTypeSlot()) {
				if(!isShaped) 
					event.getInventory().setItem(template.getRecipeTypeSlot(), plugin.getShapedItem() );
				 else 
					event.getInventory().setItem(template.getRecipeTypeSlot(), plugin.getShapelessItem());	
			}
			
			if(slot == template.getSaveSlot()) {
				ItemStack result = event.getInventory().getItem(template.getResultSlot());
				if(result == null) {
					event.getWhoClicked().sendMessage("§cInvalid recipe!");
					return;
				}
				ItemStack[] ings = new ItemStack[template.getCraftingSlots().length];
				for(int i = 0; i<ings.length; i++)
					ings[i] = getItem(template.getCraftingSlots()[i]);
				if(!Arrays.asList(ings).stream().filter(s -> s != null && !s.getType().equals(XMaterial.AIR.parseMaterial())).findAny().isPresent()) {
					event.getWhoClicked().sendMessage("§cInvalid recipe!");
					return;
				}
				try {
					plugin.getRecipeManager().createRecipe(result, ings, isShaped, recipeName, UUID.randomUUID(), 8, 6);
					event.getWhoClicked().sendMessage("§aCreated new recipe!");
				} catch (IOException | InvalidConfigurationException e) {
					event.getWhoClicked().sendMessage("§cSomething went wrong while saving: " + e);
				} finally {
					event.getWhoClicked().closeInventory();
				}
			}	
		}
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
		for(int i : template.getCraftingSlots()) {
    		ItemStack is = event.getInventory().getItem(i);
    		if(is == null) continue;
    		event.getPlayer().getInventory().addItem(is);
    	}
    	ItemStack result = event.getInventory().getItem(template.getResultSlot());
    	if(result != null) event.getPlayer().getInventory().addItem(result);
    	IGUIManager.remove(getId());
	}

	@Override
	public void onInventoryDrag(InventoryDragEvent event) {
		for(int i : event.getInventorySlots())
			if(!isCraftingSlot(i)) {
				event.setCancelled(true);
				return;
			}
	}	
}
