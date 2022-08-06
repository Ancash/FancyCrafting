package de.ancash.fancycrafting.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import de.ancash.fancycrafting.gui.base.CreateRecipeMenuGUI;
import de.ancash.fancycrafting.gui.base.PagedRecipesViewGUI;
import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.base.WorkspaceTemplate;
import de.ancash.fancycrafting.recipe.IRecipe;

@SuppressWarnings("nls")
public class FancyCraftingCommand implements CommandExecutor {

	private final AbstractFancyCrafting plugin;
	private final List<String> toSend;
	private final Map<Integer[], Permission> openPerms = new HashMap<>();

	@SuppressWarnings({ "unchecked" })
	public FancyCraftingCommand(AbstractFancyCrafting plugin) {
		this.plugin = plugin;
		toSend = (List<String>) this.plugin.getDescription().getCommands().get("fc").get("usage");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if (args.length == 0) {
			toSend.forEach(str -> sender.sendMessage(str));
			return true;
		}

		String command = args[0];

		switch (command.toLowerCase(Locale.ENGLISH)) {
		case "reload":
			if(reload(sender))
				return true;
			break;
		case "open":
			if (open(sender, args))
				return true;
			break;
		case "create":
			if (create(sender))
				return true;
			break;
		case "edit":
			if (edit(sender, args))
				return true;
			break;
		case "view":
			if (view(sender, args))
				return true;
			break;
		default:
			break;
		}

		toSend.forEach(str -> sender.sendMessage(str));
		return true;
	}
	
	private boolean reload(CommandSender sender) {
		if(!sender.isOp() && !sender.hasPermission(AbstractFancyCrafting.RELOAD)) {
			sender.sendMessage(this.plugin.getResponse().NO_PERMISSION);
			return true;
		}
		plugin.reload();
		return true;
	}

	private boolean isPlayer(CommandSender sender) {
		if (sender instanceof Player)
			return true;
		sender.sendMessage(this.plugin.getResponse().NO_CONSOLE_COMMAND);
		return false;
	}

	private boolean view(CommandSender sender, String[] args) {
		if (!isPlayer(sender))
			return true;

		if (args.length == 1 && (sender.hasPermission(AbstractFancyCrafting.VIEW_ALL_PERM) || sender.isOp())) {
			new PagedRecipesViewGUI(this.plugin, (Player) sender,
					new ArrayList<>(this.plugin.getRecipeManager().getCustomRecipes()));
			return true;
		}
		if (args.length == 2) {
			if (!sender.isOp() && !sender.hasPermission(
					new Permission("fancycrafting.view." + args[1].toLowerCase(), PermissionDefault.FALSE))) {
				sender.sendMessage(this.plugin.getResponse().NO_PERMISSION);
				return true;
			}

			Set<IRecipe> recipes = this.plugin.getRecipeManager().getRecipeByName(args[1]);
			if (recipes == null || recipes.isEmpty()) {
				sender.sendMessage(this.plugin.getResponse().INVALID_RECIPE.replace("%r", args[1]));
				return true;
			}
			plugin.viewRecipe((Player) sender, recipes);
			return true;
		}
		return false;
	}

	private boolean edit(CommandSender sender, String[] args) {
		if (!isPlayer(sender))
			return true;

		if (!sender.isOp() && !sender.hasPermission(AbstractFancyCrafting.EDIT_PERM)) {
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
				new PagedRecipesViewGUI(this.plugin, (Player) sender, new ArrayList<>(recipes));
			} else {
				plugin.editRecipe((Player) sender, recipes.stream().findAny().get());
			}
			return true;
		}
		return false;
	}

	private boolean create(CommandSender sender) {
		if (!isPlayer(sender))
			return true;

		if (!sender.isOp() && !sender.hasPermission(AbstractFancyCrafting.CREATE_PERM)) {
			sender.sendMessage(this.plugin.getResponse().NO_PERMISSION);
			return true;
		}
		CreateRecipeMenuGUI.open(this.plugin, (Player) sender);
		return true;
	}

	private Permission getOpenCustomPerm(int width, int height) {
		if (width == 3 && height == 3)
			return AbstractFancyCrafting.OPEN_DEFAULT_PERM;
		return openPerms.computeIfAbsent(new Integer[] { width, height },
				key -> new Permission("fancycrafting.open." + width + "x" + height, PermissionDefault.FALSE));
	}

	private Permission getOpenOtherCustomPerm(int width, int height) {
		if (width == 3 && height == 3)
			return AbstractFancyCrafting.OPEN_OTHER_DEFAULT_PERM;
		return openPerms.computeIfAbsent(new Integer[] { width, height },
				key -> new Permission("fancycrafting.open.other." + width + "x" + height, PermissionDefault.FALSE));
	}

	private boolean open(CommandSender sender, String[] args) {
		if (args.length > 4)
			return false;

		Player player = sender instanceof Player ? (Player) sender : null;
		if ((args.length == 3 || args.length == 1) && player == null) {
			sender.sendMessage(this.plugin.getResponse().NO_CONSOLE_COMMAND);
			return true;
		}

		if (args.length == 4) {
			int width = -1;
			int height = -1;
			try {
				width = Integer.valueOf(args[2]);
				height = Integer.valueOf(args[3]);
			} catch (NumberFormatException nfe) {
				sender.sendMessage(this.plugin.getResponse().INVALID_CRAFTING_DIMENSION.replace("%w", args[2])
						.replace("%h", args[3]));
				return true;
			}
			if (width < 1 || width > 8 || height < 1 || height > 6) {
				sender.sendMessage(this.plugin.getResponse().INVALID_CRAFTING_DIMENSION.replace("%w", args[2])
						.replace("%h", args[3]));
				return true;
			}
			if (!sender.isOp() && !sender.hasPermission(getOpenOtherCustomPerm(width, height))) {
				sender.sendMessage(this.plugin.getResponse().NO_PERMISSION);
				return true;
			}

			Player target = Bukkit.getPlayer(args[1]);
			if (target == null || !target.isOnline()) {
				sender.sendMessage(this.plugin.getResponse().PLAYER_NOT_FOUND.replace("%player%", args[1]));
				return true;
			}

			openCraftingWorkspace(target, WorkspaceTemplate.get(width, height));
			return true;
		} else if (args.length == 3) {
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
			if (!player.isOp() && !player.hasPermission(getOpenCustomPerm(width, height))) {
				sender.sendMessage(this.plugin.getResponse().NO_PERMISSION);
				return true;
			}
			openCraftingWorkspace(player, WorkspaceTemplate.get(width, height));
			return true;
		} else if (args.length == 2) {
			if (!sender.isOp() && !sender.hasPermission(AbstractFancyCrafting.OPEN_OTHER_DEFAULT_PERM)) {
				sender.sendMessage(this.plugin.getResponse().NO_PERMISSION);
				return true;
			}
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null || !target.isOnline()) {
				sender.sendMessage(this.plugin.getResponse().PLAYER_NOT_FOUND.replace("%player%", args[1]));
				return true;
			}
			openCraftingWorkspace(target);
			return true;
		}
		if (!player.isOp() && !player.hasPermission(AbstractFancyCrafting.OPEN_DEFAULT_PERM)) {
			sender.sendMessage(this.plugin.getResponse().NO_PERMISSION);
			return true;
		}
		openCraftingWorkspace(player);
		return true;
	}

	private void openCraftingWorkspace(Player player) {
		openCraftingWorkspace(player, WorkspaceTemplate.get(this.plugin.getDefaultDimension().getWidth(),
				this.plugin.getDefaultDimension().getHeight()));
	}

	private void openCraftingWorkspace(Player player, WorkspaceTemplate template) {
		plugin.openCraftingWorkspace(player, template);
	}
}