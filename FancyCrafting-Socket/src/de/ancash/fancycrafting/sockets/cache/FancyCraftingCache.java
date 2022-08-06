package de.ancash.fancycrafting.sockets.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import de.ancash.fancycrafting.sockets.FancyCraftingSocket;

import java.util.UUID;

@SuppressWarnings("nls")
public class FancyCraftingCache implements Runnable{

	private final FancyCraftingSocket pl;

	private final Map<String, FancyCraftingCacheable> cache = new HashMap<>();
	
	public FancyCraftingCache(FancyCraftingSocket pl) {
		this.pl = pl;
	}
	
	public FancyCraftingFile getTemplateFile(String path) {
		return getTemplateFile(path, null);
	}
	
	public FancyCraftingFile getTemplateFile(String path, String fallback) {
		synchronized (cache) {
			if(!cache.containsKey(path))
				cache.put(path, new FancyCraftingFile(pl, path, fallback));
			return cache.get(path).updateLastAccess().onAccess();
		}
	}
	
	public String getPlayerConfigFile(UUID uuid) {
		return getPlayerDirectory(uuid) + "/config.yml";
	}
	
	public String getPlayerDirectory(UUID uuid) {
		return pl.PLAYER_DIR + "/" + uuid.toString();
	}

	@Override
	public void run() {
		while(!Thread.interrupted()) {
			try {
				Thread.sleep(1_000);
			} catch (InterruptedException e) {
				pl.warn("Stopping");
				return;
			}
			
			checkFancyCraftingCacheables();
		}
	}
	
	private void checkFancyCraftingCacheables() {
		long now = System.currentTimeMillis();
		synchronized (cache) {
			Iterator<Entry<String, FancyCraftingCacheable>> keyIter = cache.entrySet().iterator();
			while(keyIter.hasNext()) {
				Entry<String, FancyCraftingCacheable> entry = keyIter.next();
				if(entry.getValue().isDisposable() && entry.getValue().getLastAccess() + 1_000 < now) {
					try {
						entry.getValue().dispose();
						pl.getLogger().info(String.format("Disposed of FancyCraftingCacheable %s", entry.getValue().getClass().getCanonicalName()));;
					} catch (Exception e) {
						pl.getLogger().log(Level.SEVERE, String.format("Could not dispose of FancyCraftingCacheable %s", entry.getValue().getClass().getCanonicalName()), e);;
					} finally {	
						keyIter.remove();	
					}
				}
			}
		}
	}

	public void disposeAll() {
		synchronized (cache) {
			Iterator<FancyCraftingCacheable> iter = cache.values().iterator();
			while(iter.hasNext()) {
				FancyCraftingCacheable c = iter.next();
				if(c instanceof FancyCraftingFile) {
					try {
						c.dispose();
						pl.getLogger().info(String.format("Disposed of FancyCraftingCacheable %s", c.getClass().getCanonicalName()));;
					} catch (Exception e) {
						pl.getLogger().log(Level.SEVERE, String.format("Could not dispose of FancyCraftingCacheable %s", c.getClass().getCanonicalName()), e);;
					} finally {	
						iter .remove();	
					}
				}
			}
			cache.clear();
		}
	}
}