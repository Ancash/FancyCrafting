package de.ancash.fancycrafting.autocrafter.item;

import static de.ancash.fancycrafting.NBTKeys.*;
import static de.ancash.nbtnexus.serde.access.MapAccessUtil.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.commands.FancyCraftingSubCommand;
import de.ancash.nbtnexus.serde.ItemSerializer;
import de.ancash.nbtnexus.serde.SerializedItem;

public class AutoCrafterItemInitSubCommand extends FancyCraftingSubCommand {

	private final FancyCrafting pl;

	public AutoCrafterItemInitSubCommand(FancyCrafting pl, String... str) {
		super(pl, str);
		this.pl = pl;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Boolean apply(CommandSender sender, String[] arg1) {
		if (!isPlayer(sender)) {
			sender.sendMessage(pl.getResponse().NO_CONSOLE_COMMAND);
			return true;
		}

		if (!sender.isOp() && !sender.hasPermission(FancyCrafting.AUTO_CRAFTER_INIT_PERM)) {
			sender.sendMessage(this.pl.getResponse().NO_PERMISSION);
			return true;
		}
		ItemStack item = ((Player) sender).getItemInHand();
		if (item == null || item.getType() == Material.AIR) {
			sender.sendMessage(pl.getResponse().NO_ITEM_IN_HAND);
			return true;
		}

		SerializedItem autoCrafter = SerializedItem.of(ItemSerializer.INSTANCE.serializeItemStack(item), false);

		if (!exists(autoCrafter.getMap(), BASE_COMPOUND_TAG))
			autoCrafter.getMap().put(BASE_COMPOUND_TAG, new HashMap<>());

		if (!exists(autoCrafter.getMap(), AUTO_RECIPES_COMPOUND_PATH))
			autoCrafter.getMap(BASE_COMPOUND_TAG).put(AUTO_RECIPES_COMPOUND_TAG, new HashMap<>());

		if (!exists(autoCrafter.getMap(), AUTO_RECIPES_RESULTS_PATH))
			autoCrafter.getMap(AUTO_RECIPES_COMPOUND_PATH).put(AUTO_RECIPES_RESULTS_TAG, new ArrayList<>());

		if (!exists(autoCrafter.getMap(), AUTO_RECIPES_SLOTS_PATH))
			autoCrafter.getMap(AUTO_RECIPES_COMPOUND_PATH).put(AUTO_RECIPES_SLOTS_TAG, 54);

		((Player) sender).getInventory().setItemInHand(autoCrafter.toItem());
		return true;
	}

}