package se.gustavkarlsson.autogit.file.watcher;

import com.google.common.eventbus.EventBus;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.util.Collections.unmodifiableSet;

public final class NativeFileWatcher implements FileWatcher, AutoCloseable {

	private final Map<WatchKey, Path> watched = new HashMap<>();
	private final EventBus eventBus;
	private final WatchService watchService;
	private boolean closed = false;

	public NativeFileWatcher(EventBus eventBus, FileSystem fileSystem) throws IOException {
		checkNotNull(eventBus);
		checkNotNull(fileSystem);
		this.eventBus = eventBus;
		watchService = fileSystem.newWatchService();
		new WatchThread().start();
	}

	public NativeFileWatcher(EventBus eventBus) throws IOException {
		this(eventBus, FileSystems.getDefault());
	}

	@Override
	public void close() throws Exception {
		watchService.close();
		closed = true;
	}

	@Override
	public boolean watch(Path path) throws IOException {
		checkNotNull(path);
		checkClosed();

		if (watched.containsValue(path)) {
			return false;
		}

		WatchKey key = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		watched.put(key, path);
		return true;
	}

	@Override
	public boolean unwatch(Path path) {
		checkNotNull(path);
		checkClosed();

		if (!watched.containsValue(path)) {
			return false;
		}

		Iterator<Map.Entry<WatchKey, Path>> iterator = watched.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<WatchKey, Path> keyPathEntry = iterator.next();
			if (Objects.equals(path, keyPathEntry.getValue())) {
				keyPathEntry.getKey().cancel();
				iterator.remove();
				return true;
			}
		}
		throw new IllegalStateException("No path was not found: " + path);
	}

	private void checkClosed() {
		if (closed) {
			throw new ClosedFileWatcherException();
		}
	}

	@Override
	public Set<Path> getWatched() {
		return unmodifiableSet(new HashSet<>(watched.values()));
	}

	@Override
	public void clear() {
		watched.keySet().stream().forEach(WatchKey::cancel);
		watched.clear();
	}

	public boolean isClosed() {
		return closed;
	}

	private final class WatchThread extends Thread {

		public WatchThread() {
			super("NativeFileWatcher Thread");
		}

		@Override
		public void run() {
			while (true) {
				try {
					WatchKey key = watchService.take();
					key.pollEvents(); // Poll to allow reset
					boolean valid = key.reset();
					if (!valid) {
						continue;
					}
					Path path = watched.get(key);
					checkNotNull(path);
					PathChangedEvent event = new PathChangedEvent(path);
					eventBus.post(event);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} catch (ClosedWatchServiceException e) {
					break;
				}
			}
		}
	}
}
