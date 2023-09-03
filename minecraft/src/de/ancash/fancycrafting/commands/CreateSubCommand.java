package de.ancash.fancycrafting.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.manage.RecipeCreateMenuGUI;

public class CreateSubCommand extends FancyCraftingSubCommand {

	public CreateSubCommand(FancyCrafting pl, String... str) {
		super(pl, str);
	}

	@Override
	public Boolean apply(CommandSender sender, String[] u) {
		if (!isPlayer(sender))
			return true;

		if (!sender.isOp() && !sender.hasPermission(FancyCrafting.CREATE_PERM)) {
			sender.sendMessage(this.pl.getResponse().NO_PERMISSION);
			return true;
		}
		RecipeCreateMenuGUI.open(this.pl, (Player) sender);
		return true;
	}

}
