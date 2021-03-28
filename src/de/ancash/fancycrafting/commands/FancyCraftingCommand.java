package de.ancash.fancycrafting.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;

public class FancyCraftingCommand implements CommandExecutor{
	
	private final FancyCrafting plugin;
	private final List<String> toSend;
	
	@SuppressWarnings("unchecked")
	public FancyCraftingCommand(FancyCrafting plugin) {
		this.plugin = plugin;
		toSend = (List<String>) plugin.getDescription().getCommands().get("fc").get("usage");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if(!(sender instanceof Player)) return true;
		Player player = (Player) sender;
		
		if(args.length == 0) {
			toSend.forEach(str -> player.sendMessage(str));
			return true;
		}
		
		String command = args[0];
		
		switch (command.toLowerCase()) {
		case "create":
			if(!player.hasPermission("fancycrafting.create") && !player.isOp()) return true;
			plugin.getRecipeCreateGUI().open(player);
			return true;
		case "edit":
			if(!player.hasPermission("fancycrafting.edit") && !player.isOp()) return true;
			plugin.getRecipeEditGUI().open(player);
			return true;
		default:
			break;
		}
		
		toSend.forEach(str -> player.sendMessage(str));
		
		return true;
	}	
}
