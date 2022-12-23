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
	public Boolean apply(CommandSender t, String[] u) {
		if (!(t instanceof Player)) {
			t.sendMessage(pl.getResponse().NO_CONSOLE_COMMAND);
			return true;
		}
		new ManageBlacklistCollectionGUI((FancyCrafting) pl, (Player) t,
				new ArrayList<>((((FancyCrafting) pl).getRecipeManager()).getBlacklistedRecipes().values())).open();
		return true;
	}

}