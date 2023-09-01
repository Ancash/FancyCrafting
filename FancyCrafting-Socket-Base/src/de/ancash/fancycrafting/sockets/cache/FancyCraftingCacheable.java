package de.ancash.fancycrafting.sockets.cache;

public interface FancyCraftingCacheable {

	public <T extends FancyCraftingCacheable> T updateLastAccess();

	public long getLastAccess();

	public <T extends FancyCraftingCacheable> T onAccess();

	public boolean isDisposable();

	public <T extends FancyCraftingCacheable> T setDisposable(boolean b);

	public void dispose() throws Exception;
}