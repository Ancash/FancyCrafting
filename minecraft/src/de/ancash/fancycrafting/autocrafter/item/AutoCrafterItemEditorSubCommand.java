package de.ancash.fancycrafting.autocrafter.item;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.commands.FancyCraftingSubCommand;
import de.ancash.nbtnexus.serde.SerializedItem;

public class AutoCrafterItemEditorSubCommand extends FancyCraftingSubCommand {

	private final FancyCrafting pl;

	public AutoCrafterItemEditorSubCommand(FancyCrafting pl, String... str) {
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

		if (!sender.isOp() && !sender.hasPermission(FancyCrafting.AUTO_CRAFTER_EDITOR_PERM)) {
			sender.sendMessage(this.pl.getResponse().NO_PERMISSION);
			return true;
		}
		ItemStack item = ((Player) sender).getItemInHand();
		if (item == null || item.getType() == Material.AIR) {
			sender.sendMessage(pl.getResponse().NO_ITEM_IN_HAND);
			return true;
		}

		if (!AutoCrafterItemEditor.isValid(SerializedItem.of(item))) {
			sender.sendMessage(pl.getResponse().NO_AUTO_CRAFTER);
			return true;
		}

		new AutoCrafterItemEditor(pl, ((Player) sender).getUniqueId(), item);
		return true;
	}

}