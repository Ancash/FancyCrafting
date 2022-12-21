package de.ancash.fancycrafting.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.base.gui.WorkspaceTemplate;

public class OpenSubCommand extends FancyCraftingSubCommand {

	private final Map<Integer[], Permission> openPerms = new HashMap<>();

	public OpenSubCommand(AbstractFancyCrafting pl, String... str) {
		super(pl, str);
	}

	@SuppressWarnings("nls")
	@Override
	public Boolean apply(CommandSender sender, String[] args) {
		if (args.length > 4)
			return false;

		Player player = sender instanceof Player ? (Player) sender : null;
		if ((args.length == 3 || args.length == 1) && player == null) {
			sender.sendMessage(this.pl.getResponse().NO_CONSOLE_COMMAND);
			return true;
		}

		if (args.length == 4) {
			int width = -1;
			int height = -1;
			try {
				width = Integer.valueOf(args[2]);
				height = Integer.valueOf(args[3]);
			} catch (NumberFormatException nfe) {
				sender.sendMessage(
						this.pl.getResponse().INVALID_CRAFTING_DIMENSION.replace("%w", args[2]).replace("%h", args[3]));
				return true;
			}
			if (width < 1 || width > 8 || height < 1 || height > 6) {
				sender.sendMessage(
						this.pl.getResponse().INVALID_CRAFTING_DIMENSION.replace("%w", args[2]).replace("%h", args[3]));
				return true;
			}
			if (!sender.isOp() && !sender.hasPermission(getOpenOtherCustomPerm(width, height))) {
				sender.sendMessage(this.pl.getResponse().NO_PERMISSION);
				return true;
			}

			Player target = Bukkit.getPlayer(args[1]);
			if (target == null || !target.isOnline()) {
				sender.sendMessage(this.pl.getResponse().PLAYER_NOT_FOUND.replace("%player%", args[1]));
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
				player.sendMessage(
						this.pl.getResponse().INVALID_CRAFTING_DIMENSION.replace("%w", args[1]).replace("%h", args[2]));
				return true;
			}
			if (width < 1 || width > 8 || height < 1 || height > 6) {
				player.sendMessage(
						this.pl.getResponse().INVALID_CRAFTING_DIMENSION.replace("%w", args[1]).replace("%h", args[2]));
				return true;
			}
			if (!player.isOp() && !player.hasPermission(getOpenCustomPerm(width, height))) {
				sender.sendMessage(this.pl.getResponse().NO_PERMISSION);
				return true;
			}
			openCraftingWorkspace(player, WorkspaceTemplate.get(width, height));
			return true;
		} else if (args.length == 2) {
			if (!sender.isOp() && !sender.hasPermission(AbstractFancyCrafting.OPEN_OTHER_DEFAULT_PERM)) {
				sender.sendMessage(this.pl.getResponse().NO_PERMISSION);
				return true;
			}
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null || !target.isOnline()) {
				sender.sendMessage(this.pl.getResponse().PLAYER_NOT_FOUND.replace("%player%", args[1]));
				return true;
			}
			openCraftingWorkspace(target);
			return true;
		}
		if (!player.isOp() && !player.hasPermission(AbstractFancyCrafting.OPEN_DEFAULT_PERM)) {
			sender.sendMessage(this.pl.getResponse().NO_PERMISSION);
			return true;
		}
		openCraftingWorkspace(player);
		return true;
	}

	private void openCraftingWorkspace(Player player) {
		openCraftingWorkspace(player, WorkspaceTemplate.get(this.pl.getDefaultDimension().getWidth(),
				this.pl.getDefaultDimension().getHeight()));
	}

	private void openCraftingWorkspace(Player player, WorkspaceTemplate template) {
		pl.openCraftingWorkspace(player, template);
	}

	@SuppressWarnings("nls")
	private Permission getOpenCustomPerm(int width, int height) {
		if (width == 3 && height == 3)
			return AbstractFancyCrafting.OPEN_DEFAULT_PERM;
		return openPerms.computeIfAbsent(new Integer[] { width, height },
				key -> new Permission("fancycrafting.open." + width + "x" + height, PermissionDefault.FALSE));
	}

	@SuppressWarnings("nls")
	private Permission getOpenOtherCustomPerm(int width, int height) {
		if (width == 3 && height == 3)
			return AbstractFancyCrafting.OPEN_OTHER_DEFAULT_PERM;
		return openPerms.computeIfAbsent(new Integer[] { width, height },
				key -> new Permission("fancycrafting.open.other." + width + "x" + height, PermissionDefault.FALSE));
	}
}
