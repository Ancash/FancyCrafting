package de.ancash.fancycrafting.commands;

import org.bukkit.command.CommandSender;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;

public class ReloadSubCommand extends FancyCraftingSubCommand {

	private final AbstractFancyCrafting pl;

	public ReloadSubCommand(AbstractFancyCrafting pl, String...str) {
		super(pl, str);
		this.pl = pl;
	}

	@Override
	public Boolean apply(CommandSender sender, String[] arg1) {
		if(!sender.isOp() && !sender.hasPermission(AbstractFancyCrafting.RELOAD)) {
			sender.sendMessage(this.pl.getResponse().NO_PERMISSION);
			return true;
		}
		pl.reload();
		return true;
	}

}