package de.ancash.fancycrafting.sockets.cache;

import java.io.IOException;
import java.util.logging.Level;

import de.ancash.fancycrafting.sockets.FancyCraftingSocket;
import de.ancash.libs.org.apache.commons.io.FileUtils;
import de.ancash.libs.org.simpleyaml.configuration.file.YamlFile;

@SuppressWarnings("nls")
public class FancyCraftingFile extends YamlFile implements FancyCraftingCacheable {

	private long lastAccess = System.currentTimeMillis();
	private boolean disposable = true;

	public FancyCraftingFile(FancyCraftingSocket pl, String path, String resource) {
		super(path);
		if (!exists())
			try {
				if (resource != null) {
					FileUtils.copyInputStreamToFile(pl.getResource(resource), getConfigurationFile());
					pl.getLogger().log(Level.FINE, "Copied resource " + resource + " to " + path);
				} else {
					createNewFile(true);
					pl.getLogger().log(Level.FINE, "Created new file " + path);
				}
			} catch (IOException e1) {
				pl.getLogger().log(Level.SEVERE,
						String.format("Could not load file %s and resource %s. Using empty file", path, resource), e1);
				try {
					deleteFile();
					createNewFile(true);
					pl.getLogger().log(Level.SEVERE, String.format("%s deleted and new file created", getFilePath()));
				} catch (IOException e) {
					throw new FancyCraftingCacheException(
							String.format("Could not delete and create new file %s", getFilePath()), e);
				}
			}
		try {
			super.load();
			pl.getLogger().log(Level.FINE, String.format("Loaded file %s", getFilePath()));
		} catch (IOException e) {
			throw new FancyCraftingCacheException(String.format("Could not load file %s.", getFilePath()), e);
		}
	}

	@Override
	public long getLastAccess() {
		return lastAccess;
	}

	@Override
	public String toString() {
		return getConfigurationFile().getPath();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized FancyCraftingFile updateLastAccess() {
		this.lastAccess = System.currentTimeMillis();
		return this;
	}

	@Override
	public void dispose() throws Exception {
		save();
	}

	@Override
	public boolean isDisposable() {
		return disposable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FancyCraftingFile setDisposable(boolean b) {
		this.disposable = b;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FancyCraftingFile onAccess() {
		// nothing to do
		return this;
	}
}