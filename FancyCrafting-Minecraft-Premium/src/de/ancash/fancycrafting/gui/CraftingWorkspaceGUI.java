package de.ancash.fancycrafting.gui;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.base.gui.AbstractCraftingWorkspace;
import de.ancash.fancycrafting.base.gui.WorkspaceTemplate;
import de.ancash.fancycrafting.recipe.IRecipe;

public class CraftingWorkspaceGUI extends de.ancash.fancycrafting.base.gui.CraftingWorkspaceGUI {

	private final FancyCrafting pl;
	
	public CraftingWorkspaceGUI(FancyCrafting pl, Player player, WorkspaceTemplate template) {
		super(pl, player, template, true, new de.ancash.fancycrafting.BlacklistAutoRecipeMatcher(pl, player, pl.getRecipeManager().getAutoMatchingRecipes()));
		this.pl = pl;
		setRecipeMatcherCallable(new RecipeMatcherCallable(this));
	}

	static class RecipeMatcherCallable
			extends de.ancash.fancycrafting.base.gui.AbstractCraftingWorkspace.RecipeMatcherCallable {
		
		public RecipeMatcherCallable(AbstractCraftingWorkspace workspace) {
			super(workspace);
		}

		@Override
		protected IRecipe match() {
			IRecipe match = null;
			if (workspace.getMatrix().getArray().length == 0)
				return match;

			if (((CraftingWorkspaceGUI) workspace).pl.getRecipeManager().isBlacklisted(Stream.of(workspace.getMatrix().getArray())
					.map(i -> i != null ? i.hashCode() : null).collect(Collectors.toList()))) {
				workspace.getRecipeMatchCompletionHandler().onNoRecipeMatch();
				workspace.setCurrentRecipe(null);
				return null;
			}

			return super.match();
		}

	}
}