
package de.ancash.fancycrafting.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.WorkspaceTemplate;
import de.ancash.fancycrafting.gui.CraftingWorkspaceGUI;
import de.ancash.fancycrafting.gui.PagedRecipesViewGUI;
import de.ancash.fancycrafting.gui.RecipeCreateGUI;
import de.ancash.fancycrafting.gui.RecipeEditGUI;
import de.ancash.fancycrafting.gui.RecipeViewGUI;
import de.ancash.fancycrafting.recipe.IRecipe;

public class FancyCraftingCommand implements CommandExecutor {

	private final FancyCrafting plugin;
	private final List<String> toSend;

	@SuppressWarnings("unchecked")
	public FancyCraftingCommand(FancyCrafting plugin) {
		this.plugin = plugin;
		toSend = (List<String>) plugin.getDescription().getCommands().get("fc").get("usage");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if (!(sender instanceof Player))
			return true;
		Player player = (Player) sender;

		if (args.length == 0) {
			toSend.forEach(str -> player.sendMessage(str));
			return true;
		}

		String command = args[0];

		switch (command.toLowerCase()) {
		case "open":
			if (args.length == 3) {
				int width = -1;
				int height = -1;
				try {
					width = Integer.valueOf(args[1]);
					height = Integer.valueOf(args[2]);
				} catch (NumberFormatException nfe) {
					player.sendMessage(plugin.getResponse().INVALID_CRAFTING_DIMENSION.replace("%w", args[1])
							.replace("%h", args[2]));
					return true;
				}
				if (width < 1 || width > 8 || height < 1 || height > 6) {
					player.sendMessage(plugin.getResponse().INVALID_CRAFTING_DIMENSION.replace("%w", args[1])
							.replace("%h", args[2]));
					return true;
				}
				if (!player.hasPermission("fancycrafting.open." + width + "x" + height)) {
					sender.sendMessage(plugin.getResponse().NO_PERMISSION);
					return true;
				}
				new CraftingWorkspaceGUI(plugin, player, WorkspaceTemplate.get(width, height));
				return true;
			}
			if (!player.hasPermission("fancycrafting.open.default")) {
				sender.sendMessage(plugin.getResponse().NO_PERMISSION);
				return true;
			}
			new CraftingWorkspaceGUI(plugin, player, WorkspaceTemplate.get(plugin.getDefaultDimension().getWidth(),
					plugin.getDefaultDimension().getHeight()));
			return true;
		case "create":
			if (!player.hasPermission("fancycrafting.create")) {
				sender.sendMessage(plugin.getResponse().NO_PERMISSION);
				return true;
			}
			RecipeCreateGUI.open(plugin, player);
			return true;
		case "edit":
			if (!sender.hasPermission("fancycrafting.edit")) {
				sender.sendMessage(plugin.getResponse().NO_PERMISSION);
				return true;
			}
			if (args.length == 2) {

				Set<IRecipe> recipes = plugin.getRecipeManager().getRecipeByName(args[1]);
				if (recipes != null)
					recipes = recipes.stream().filter(r -> !r.isVanilla()).collect(Collectors.toSet());
				if (recipes == null || recipes.isEmpty()) {
					sender.sendMessage(plugin.getResponse().INVALID_RECIPE.replace("%r", args[1]));
					return true;
				}
				if (recipes.size() > 1) {
					new PagedRecipesViewGUI(plugin, player, new ArrayList<>(recipes));
				} else {
					new RecipeEditGUI(plugin, player, recipes.stream().findAny().get());
				}
				return true;
			}
			break;
		case "view":
			if (args.length == 1 && sender.hasPermission("fancycrafting.admin.view")) {
				new PagedRecipesViewGUI(plugin, player, new ArrayList<>(plugin.getRecipeManager().getCustomRecipes()));
				return true;
			}
			if (args.length == 2) {
				if (!sender.hasPermission("fancycrafting.view." + args[1].toLowerCase())) {
					sender.sendMessage(plugin.getResponse().NO_PERMISSION);
					return true;
				}

				Set<IRecipe> recipes = plugin.getRecipeManager().getRecipeByName(args[1]);
				if (recipes == null || recipes.isEmpty()) {
					sender.sendMessage(plugin.getResponse().INVALID_RECIPE.replace("%r", args[1]));
					return true;
				}
				RecipeViewGUI.viewRecipe(plugin, recipes, player);
				return true;
			}
			break;
		default:
			break;
		}

		toSend.forEach(str -> player.sendMessage(str));
		return true;
	}
}
