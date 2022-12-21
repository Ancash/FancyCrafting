package de.ancash.fancycrafting.base;

public final class Response {

	public final String NO_PERMISSION;
	public final String INVALID_RECIPE;
	public final String INVALID_CRAFTING_DIMENSION;
	public final String PLAYER_NOT_FOUND;
	public final String NO_CONSOLE_COMMAND;
	public final String INVALID_RECIPE_NAME;
	public final String RECIPE_SAVED;
	public final String RECIPE_DELETED;
	public final String CRAFTING_COOLDOWN_MESSAGE;

	@SuppressWarnings("nls")
	public Response(AbstractFancyCrafting fc) {
		CRAFTING_COOLDOWN_MESSAGE = fc.getConfig().getString("crafting-cooldown-message");
		NO_PERMISSION = fc.getConfig().getString("no-permission");
		INVALID_RECIPE = fc.getConfig().getString("invalid-recipe");
		INVALID_CRAFTING_DIMENSION = fc.getConfig().getString("invalid-crafting-dimension");
		PLAYER_NOT_FOUND = fc.getConfig().getString("player-not-found");
		NO_CONSOLE_COMMAND = fc.getConfig().getString("no-console-command");
		INVALID_RECIPE_NAME = fc.getConfig().getString("invalid-recipe-name");
		RECIPE_SAVED = fc.getConfig().getString("recipe-saved");
		RECIPE_DELETED = fc.getConfig().getString("recipe-deleted");
	}

}