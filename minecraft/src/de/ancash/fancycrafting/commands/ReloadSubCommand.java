package de.ancash.fancycrafting.commands;

import org.bukkit.command.CommandSender;

import de.ancash.fancycrafting.FancyCrafting;

public class ReloadSubCommand extends FancyCraftingSubCommand {

	private final FancyCrafting pl;

	public ReloadSubCommand(FancyCrafting pl, String... str) {
		super(pl, str);
		this.pl = pl;
	}

	@Override
	public Boolean apply(CommandSender sender, String[] arg1) {
		if (!sender.isOp() && !sender.hasPermission(FancyCrafting.RELOAD_PERM)) {
			sender.sendMessage(this.pl.getResponse().NO_PERMISSION);
			return true;
		}
		pl.reload();
		return true;
	}

}