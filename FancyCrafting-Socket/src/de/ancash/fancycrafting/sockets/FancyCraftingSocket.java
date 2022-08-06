package de.ancash.fancycrafting.sockets;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import de.ancash.fancycrafting.sockets.cache.FancyCraftingCache;
import de.ancash.fancycrafting.sockets.cache.FancyCraftingFile;
import de.ancash.fancycrafting.sockets.listeners.FancyCraftingConnectionListener;
import de.ancash.fancycrafting.sockets.listeners.FancyCraftingPacketListener;
import de.ancash.libs.org.bukkit.event.EventManager;
import de.ancash.loki.impl.SimpleLokiPluginImpl;
import de.ancash.misc.IPrintStream.ConsoleColor;
import de.ancash.sockets.async.client.AbstractAsyncClient;

@SuppressWarnings("nls")
public class FancyCraftingSocket extends SimpleLokiPluginImpl{

	public final String PREFIX = "FancyCrafting - ";
	public final String BASE_DIR = "plugins/FancyCrafting";
	public final String RECIPE_FILE = BASE_DIR + "/recipes.yml";
	public final String PLAYER_DIR = BASE_DIR + "/players";
	
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final FancyCraftingCache cache = new FancyCraftingCache(this);
	private final Map<AbstractAsyncClient, FancyCraftingClientConnection> connectionsByKey = new HashMap<>();
	private final Logger logger = Logger.getLogger(getClass().getCanonicalName());
	
	private FancyCraftingFile recipeFile;
	
	@Override
	public void onEnable() {
		loadFiles();
		EventManager.registerEvents(new FancyCraftingPacketListener(this), this);
		EventManager.registerEvents(new FancyCraftingConnectionListener(this), this);
		threadPool.execute(cache);
		info("Enabled!");
	}
	
	private void loadFiles() {
		recipeFile = cache.getTemplateFile(RECIPE_FILE);
		recipeFile.setDisposable(false);
	}
	
	@Override
	public void onDisable() {
		cache.disposeAll();
		threadPool.shutdownNow();
	}
	
	public FancyCraftingClientConnection removeConnection(AbstractAsyncClient cl) {
		synchronized (connectionsByKey) {
			return connectionsByKey.remove(cl);
		}
	}
	
	public boolean addConnection(AbstractAsyncClient cl, UUID id, String name) {
		synchronized (connectionsByKey) {
			if(connectionsByKey.containsKey(cl))
				return false;
			this.connectionsByKey.put(cl, new FancyCraftingClientConnection(cl, id, name));
			return true;
		}
	}
	
	public FancyCraftingClientConnection getConnectionByClient(AbstractAsyncClient a) {
		synchronized (connectionsByKey) {
			return connectionsByKey.get(a);
		}
	}
	
	public Collection<FancyCraftingClientConnection> getAllConnections() {
		synchronized (connectionsByKey) {
			return connectionsByKey.values();
		}
	}
	
	public void info(String str) {
		System.out.println(ConsoleColor.GREEN_BOLD_BRIGHT + PREFIX + str + ConsoleColor.RESET);
	}
	
	public void warn(String str) {
		System.out.println(ConsoleColor.YELLOW_BOLD_BRIGHT + PREFIX + str + ConsoleColor.RESET);
	}
	
	public void severe(String str) {
		System.out.println(ConsoleColor.RED_BOLD_BRIGHT + PREFIX + str + ConsoleColor.RESET);
	}

	public FancyCraftingCache getCache() {
		return cache;
	}

	public Logger getLogger() {
		return logger;
	}
}