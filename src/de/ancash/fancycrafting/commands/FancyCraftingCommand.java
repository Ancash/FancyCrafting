package de.ancash.fancycrafting.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;

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
		case "open":
			if(!player.hasPermission("fancycrafting.open")) {
				sender.sendMessage("§cYou have permission to do that!");
				return true;
			}
			plugin.getWorkbenchGUI().open(player);
			return true;
		case "create":
			if(!player.hasPermission("fancycrafting.create")) {
				sender.sendMessage("§cYou have permission to do that!");
				return true;
			}
			plugin.getRecipeCreateGUI().open(player);
			return true;
		case "edit":
			if(!player.hasPermission("fancycrafting.edit")) {
				sender.sendMessage("§cYou have permission to do that!");
				return true;
			}
			plugin.getRecipeEditGUI().open(player);
			return true;
		case "view":
			if(args.length == 2) {
				if(!sender.hasPermission("fancycrafting.view." + args[1].toLowerCase())) {
					sender.sendMessage("§cYou have permission to do that!");
					return true;
				}
				IRecipe recipe = plugin.getRecipeManager().getCustomRecipe(args[1]);
				if(recipe == null) {
					sender.sendMessage("§cThat's not a recipe: " + args[1]);
					return true;
				}
				plugin.getRecipeViewGUI().open(player, recipe);
				return true;
			}
		default:
			break;
		}
		
		toSend.forEach(str -> player.sendMessage(str));
		
		return true;
	}	
}
