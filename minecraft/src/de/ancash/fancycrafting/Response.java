package de.ancash.fancycrafting;

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
	public final String INVALID_CATEGORY_NAME;
	public final String NO_ITEM_IN_HAND;
	public final String NO_AUTO_CRAFTER;

	@SuppressWarnings("nls")
	public Response(FancyCrafting fc) {
		NO_AUTO_CRAFTER = fc.getConfig().getString("no-auto-crafter");
		NO_ITEM_IN_HAND = fc.getConfig().getString("no-item-in-hand");
		CRAFTING_COOLDOWN_MESSAGE = fc.getConfig().getString("crafting.cooldown-message");
		NO_PERMISSION = fc.getConfig().getString("no-permission");
		INVALID_RECIPE = fc.getConfig().getString("invalid-recipe");
		INVALID_CRAFTING_DIMENSION = fc.getConfig().getString("invalid-crafting-dimension");
		PLAYER_NOT_FOUND = fc.getConfig().getString("player-not-found");
		NO_CONSOLE_COMMAND = fc.getConfig().getString("no-console-command");
		INVALID_RECIPE_NAME = fc.getConfig().getString("invalid-recipe-name");
		INVALID_CATEGORY_NAME = fc.getConfig().getString("invalid-category-name");
		RECIPE_SAVED = fc.getConfig().getString("recipe-saved");
		RECIPE_DELETED = fc.getConfig().getString("recipe-deleted");
	}

}