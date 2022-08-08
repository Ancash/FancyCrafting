package de.ancash.fancycrafting.commands;

import java.util.Locale;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;

public class ViewSubCommand extends FancyCraftingSubCommand{

	public ViewSubCommand(AbstractFancyCrafting pl, String...str) {
		super(pl, str);
	}

	@SuppressWarnings("nls")
	@Override
	public Boolean apply(CommandSender sender, String[] args) {
		if (!isPlayer(sender))
			return true;

		if (args.length == 1 && (sender.hasPermission(AbstractFancyCrafting.VIEW_ALL_PERM) || sender.isOp())) {
			pl.viewRecipeCollection((Player) sender, pl.getRecipeManager().getCustomRecipes());
			return true;
		}
		if (args.length == 2) {
			if (!sender.isOp() && !sender.hasPermission(
					new Permission("fancycrafting.view." + args[1].toLowerCase(Locale.ENGLISH), PermissionDefault.FALSE))) {
				sender.sendMessage(this.pl.getResponse().NO_PERMISSION);
				return true;
			}

			Set<IRecipe> recipes = this.pl.getRecipeManager().getRecipeByName(args[1]);
			if (recipes == null || recipes.isEmpty()) {
				sender.sendMessage(this.pl.getResponse().INVALID_RECIPE.replace("%recipe%", args[1]));
				return true;
			}
			pl.viewRecipeSingle((Player) sender, recipes);
			return true;
		}
		return false;
	}

}
