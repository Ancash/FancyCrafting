package de.ancash.fancycrafting.commands;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;

public class EditSubCommand extends FancyCraftingSubCommand {

	public EditSubCommand(AbstractFancyCrafting pl, String... str) {
		super(pl, str);
	}

	@SuppressWarnings("nls")
	@Override
	public Boolean apply(CommandSender sender, String[] args) {
		if (!isPlayer(sender))
			return true;

		if (!sender.isOp() && !sender.hasPermission(AbstractFancyCrafting.EDIT_PERM)) {
			sender.sendMessage(this.pl.getResponse().NO_PERMISSION);
			return true;
		}
		if (args.length == 2) {

			Set<IRecipe> recipes = this.pl.getRecipeManager().getRecipeByName(args[1]);
			if (recipes != null)
				recipes = recipes.stream().filter(r -> !r.isVanilla()).collect(Collectors.toSet());
			if (recipes == null || recipes.isEmpty()) {
				sender.sendMessage(this.pl.getResponse().INVALID_RECIPE.replace("%recipe%", args[1]));
				return true;
			}
			if (recipes.size() > 1) {
				pl.viewRecipeSingle((Player) sender, recipes);
			} else {
				pl.editRecipe((Player) sender, recipes.stream().findAny().get());
			}
			return true;
		}
		return false;
	}

}
