package de.ancash.fancycrafting.exception;

public class RecipeDeleteException extends Throwable{

	private static final long serialVersionUID = 3773546387137518122L;

	public RecipeDeleteException(String str) {
		super(str);
	}

	public RecipeDeleteException(Throwable str) {
		super(str);
	}
	
	public RecipeDeleteException(String str, Throwable t) {
		super(str, t);
	}
}