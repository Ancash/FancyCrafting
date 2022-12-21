package de.ancash.fancycrafting.commands;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;

public abstract class FancyCraftingSubCommand implements BiFunction<CommandSender, String[], Boolean> {

	private final String[] subCmd;
	protected final AbstractFancyCrafting pl;

	public FancyCraftingSubCommand(AbstractFancyCrafting pl, String... str) {
		this.subCmd = Stream.of(str).map(s -> s.toLowerCase(Locale.ENGLISH)).toArray(String[]::new);
		this.pl = pl;
	}

	public String[] getSubCommand() {
		return subCmd;
	}

	public boolean isPlayer(CommandSender sender) {
		if (sender instanceof Player)
			return true;
		sender.sendMessage(this.pl.getResponse().NO_CONSOLE_COMMAND);
		return false;
	}
}