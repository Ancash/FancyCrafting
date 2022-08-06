package de.ancash.fancycrafting.sockets;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TemplateThreadPool {

	private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 1);
	private final FancyCraftingSocket pl;
	
	public TemplateThreadPool(FancyCraftingSocket pl) {
		this.pl = pl;
	}
	
	public void execute(Runnable r) {
		threadPool.execute(r);
	}
	
	public synchronized void stop() {
		if(threadPool.isShutdown())
			return;
		pl.getLogger().info("ThreadPool shutdown! Unfinished tasks: " + threadPool.shutdownNow().size());
	}
}