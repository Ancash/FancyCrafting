package de.ancash.fancycrafting.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.base.gui.manage.RecipeCreateMenuGUI;

public class CreateSubCommand extends FancyCraftingSubCommand {

	public CreateSubCommand(AbstractFancyCrafting pl, String... str) {
		super(pl, str);
	}

	@Override
	public Boolean apply(CommandSender sender, String[] u) {
		if (!isPlayer(sender))
			return true;

		if (!sender.isOp() && !sender.hasPermission(AbstractFancyCrafting.CREATE_PERM)) {
			sender.sendMessage(this.pl.getResponse().NO_PERMISSION);
			return true;
		}
		RecipeCreateMenuGUI.open(this.pl, (Player) sender);
		return true;
	}

}
