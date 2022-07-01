
package de.ancash.fancycrafting.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

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
		toSend = (List<String>) this.plugin.getDescription().getCommands().get("fc").get("usage");
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

		final String command = args[0];

		switch (command.toLowerCase()) {
		case "open":
			if (args.length == 3) {
				int width = -1;
				int height = -1;
				try {
					width = Integer.valueOf(args[1]);
					height = Integer.valueOf(args[2]);
				} catch (NumberFormatException nfe) {
					player.sendMessage(this.plugin.getResponse().INVALID_CRAFTING_DIMENSION.replace("%w", args[1])
							.replace("%h", args[2]));
					return true;
				}
				if (width < 1 || width > 8 || height < 1 || height > 6) {
					player.sendMessage(this.plugin.getResponse().INVALID_CRAFTING_DIMENSION.replace("%w", args[1])
							.replace("%h", args[2]));
					return true;
				}
				if (!player.isOp() && !player.hasPermission(new Permission("fancycrafting.open." + width + "x" + height, PermissionDefault.FALSE))) {
					sender.sendMessage(this.plugin.getResponse().NO_PERMISSION);
					return true;
				}
				new CraftingWorkspaceGUI(this.plugin, player, WorkspaceTemplate.get(width, height));
				return true;
			}
			if (!player.isOp() && !player.hasPermission(FancyCrafting.OPEN_DEFAULT_PERM)) {
				sender.sendMessage(this.plugin.getResponse().NO_PERMISSION);
				return true;
			}
			new CraftingWorkspaceGUI(this.plugin, player, WorkspaceTemplate
					.get(this.plugin.getDefaultDimension().getWidth(), this.plugin.getDefaultDimension().getHeight()));
			return true;
		case "create":
			if (!player.isOp() && !player.hasPermission(FancyCrafting.CREATE_PERM)) {
				sender.sendMessage(this.plugin.getResponse().NO_PERMISSION);
				return true;
			}
			RecipeCreateGUI.open(this.plugin, player);
			return true;
		case "edit":
			if (!sender.isOp() &&  !sender.hasPermission(FancyCrafting.EDIT_PERM)) {
				sender.sendMessage(this.plugin.getResponse().NO_PERMISSION);
				return true;
			}
			if (args.length == 2) {

				Set<IRecipe> recipes = this.plugin.getRecipeManager().getRecipeByName(args[1]);
				if (recipes != null)
					recipes = recipes.stream().filter(r -> !r.isVanilla()).collect(Collectors.toSet());
				if (recipes == null || recipes.isEmpty()) {
					sender.sendMessage(this.plugin.getResponse().INVALID_RECIPE.replace("%r", args[1]));
					return true;
				}
				if (recipes.size() > 1) {
					new PagedRecipesViewGUI(this.plugin, player, new ArrayList<>(recipes));
				} else {
					new RecipeEditGUI(this.plugin, player, recipes.stream().findAny().get());
				}
				return true;
			}
			break;
		case "view":
			if (args.length == 1 && (sender.hasPermission(FancyCrafting.VIEW_ALL_PERM) || sender.isOp())) {
				new PagedRecipesViewGUI(this.plugin, player,
						new ArrayList<>(this.plugin.getRecipeManager().getCustomRecipes()));
				return true;
			}
			if (args.length == 2) {
				if (!player.isOp() && !sender.hasPermission(
						new Permission("fancycrafting.view." + args[1].toLowerCase(), PermissionDefault.FALSE))) {
					sender.sendMessage(this.plugin.getResponse().NO_PERMISSION);
					return true;
				}

				Set<IRecipe> recipes = this.plugin.getRecipeManager().getRecipeByName(args[1]);
				if (recipes == null || recipes.isEmpty()) {
					sender.sendMessage(this.plugin.getResponse().INVALID_RECIPE.replace("%r", args[1]));
					return true;
				}
				RecipeViewGUI.viewRecipe(this.plugin, recipes, player);
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
