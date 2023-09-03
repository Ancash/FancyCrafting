package de.ancash.fancycrafting.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.manage.blacklist.ManageBlacklistCollectionGUI;

public class BlacklistSubCommand extends FancyCraftingSubCommand {

	public BlacklistSubCommand(FancyCrafting pl, String... str) {
		super(pl, str);
	}

	@Override
	public Boolean apply(CommandSender sender, String[] u) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(pl.getResponse().NO_CONSOLE_COMMAND);
			return true;
		}

		if (!sender.isOp() && !sender.hasPermission(FancyCrafting.BLACKLIST_MANAGE_PERM)) {
			sender.sendMessage(this.pl.getResponse().NO_PERMISSION);
			return true;
		}

		new ManageBlacklistCollectionGUI((FancyCrafting) pl, (Player) sender,
				new ArrayList<>((((FancyCrafting) pl).getRecipeManager()).getBlacklistedRecipes().values())).open();
		return true;
	}

}