package de.ancash.fancycrafting;

public final class Response {

	public final String NO_PERMISSION;
	public final String INVALID_RECIPE;
	public final String INVALID_CRAFTING_DIMENSION;

	public Response(FancyCrafting fc) {
		NO_PERMISSION = fc.getConfig().getString("no-permission");
		INVALID_RECIPE = fc.getConfig().getString("invalid-recipe");
		INVALID_CRAFTING_DIMENSION = fc.getConfig().getString("invalid-crafting-dimension");
	}

}