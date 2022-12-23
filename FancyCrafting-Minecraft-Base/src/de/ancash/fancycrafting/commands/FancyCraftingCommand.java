package de.ancash.fancycrafting.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import de.ancash.fancycrafting.FancyCrafting;

@SuppressWarnings("nls")
public class FancyCraftingCommand implements CommandExecutor {

	private final FancyCrafting plugin;
	private final List<String> toSend;
	private final Map<String, FancyCraftingSubCommand> subCmds = new HashMap<>();

	@SuppressWarnings({ "unchecked" })
	public FancyCraftingCommand(FancyCrafting plugin) {
		this.plugin = plugin;
		toSend = (List<String>) this.plugin.getDescription().getCommands().get("fc").get("usage");
	}

	public void addSubCommand(FancyCraftingSubCommand s) {
		for (String str : s.getSubCommand())
			subCmds.put(str, s);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if (args.length == 0) {
			toSend.forEach(str -> sender.sendMessage(str));
			return true;
		}

		String command = args[0].toLowerCase(Locale.ENGLISH);

		if (subCmds.containsKey(command) && subCmds.get(command).apply(sender, args))
			return true;

		toSend.forEach(str -> sender.sendMessage(str));
		return true;
	}
}