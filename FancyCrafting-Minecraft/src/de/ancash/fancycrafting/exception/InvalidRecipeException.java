package de.ancash.fancycrafting.exception;

public class InvalidRecipeException extends Throwable {

	private static final long serialVersionUID = 3773546387137518122L;

	public InvalidRecipeException(String str) {
		super(str);
	}

	public InvalidRecipeException(Throwable str) {
		super(str);
	}

	public InvalidRecipeException(String str, Throwable t) {
		super(str, t);
	}
}