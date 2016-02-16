package se.gustavkarlsson.autogit.file.watcher;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.WatchServiceConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.jimfs.Configuration.*;
import static com.google.common.jimfs.Jimfs.newFileSystem;
import static java.nio.file.Files.createFile;
import static org.assertj.core.api.Assertions.assertThat;

public class NativeFileWatcherTest {

	private FileSystem fileSystem;
	private Path path;
	private EventBus eventBus;
	private EventHandler eventHandler;
	private NativeFileWatcher watcher;

	@Before
	public void setUp() throws Exception {
		Configuration configuration = unix().toBuilder()
				.setWatchServiceConfiguration(WatchServiceConfiguration.polling(10, TimeUnit.MILLISECONDS)).build();
		fileSystem = newFileSystem(configuration);
		path = fileSystem.getPath(".");
		eventBus = new EventBus();
		eventHandler = new EventHandler();
		eventBus.register(eventHandler);
		watcher = new NativeFileWatcher(eventBus, fileSystem);
	}

	@After
	public void tearDown() throws Exception {
		watcher.close();
	}

	@Test(expected = NullPointerException.class)
	public void createWithNullEventBusThrowsNullPointerException() throws Exception {
		new NativeFileWatcher(null);
	}

	@Test
	public void createWithDefaultFileSystem() throws Exception {
		new NativeFileWatcher(eventBus);
	}

	@Test
	public void createWithUnixFileSystem() throws Exception {
		new NativeFileWatcher(eventBus, newFileSystem(unix()));
	}

	@Test
	public void createWithWindowsFileSystem() throws Exception {
		new NativeFileWatcher(eventBus, newFileSystem(windows()));
	}

	@Test
	public void createWithOsXFileSystem() throws Exception {
		new NativeFileWatcher(eventBus, newFileSystem(osX()));
	}

	@Test(expected = NullPointerException.class)
	public void watchNullThrowsNullPointerException() throws Exception {
		watcher.watch(null);
	}

	@Test
	public void watchPathReturnsTrue() throws Exception {
		assertThat(watcher.watch(path)).isTrue();
	}

	@Test
	public void watchPreviouslyWatchedPathReturnsTrue() throws Exception {
		watcher.watch(path);
		assertThat(watcher.watch(path)).isFalse();
	}

	@Test(expected = NullPointerException.class)
	public void unwatchNullThrowsNullPointerException() throws Exception {
		watcher.unwatch(null);
	}

	@Test
	public void unwatchReturnsFalse() throws Exception {
		assertThat(watcher.unwatch(path)).isFalse();
	}

	@Test
	public void unwatchPreviouslyWatchedReturnsTrue() throws Exception {
		watcher.watch(path);
		assertThat(watcher.unwatch(path)).isTrue();
	}

	@Test
	public void clearEmptyDoesNothing() throws Exception {
		watcher.clear();
	}

	@Test
	public void clearNonEmptyUnwatchesAll() throws Exception {
		watcher.watch(path);
		watcher.clear();
		assertThat(watcher.getWatched()).isEmpty();
	}

	@Test
	public void emptyGetWatchReturnsEmpty() throws Exception {
		assertThat(watcher.getWatched()).isEmpty();
	}

	@Test
	public void watchAddsPath() throws Exception {
		watcher.watch(path);
		assertThat(watcher.getWatched()).containsOnly(path);
	}

	@Test
	public void unwatchRemovesPath() throws Exception {
		watcher.watch(path);
		watcher.unwatch(path);
		assertThat(watcher.getWatched()).isEmpty();
	}

	@Test
	public void closeCloses() throws Exception {
		watcher.close();
		assertThat(watcher.isClosed()).isTrue();
	}

	@Test
	public void closeTwiceDoesNothing() throws Exception {
		watcher.close();
		watcher.close();
	}

	@Test
	public void isClosedOnOpenedReturnsFalse() throws Exception {
		assertThat(watcher.isClosed()).isFalse();
	}

	@Test(expected = ClosedFileWatcherException.class)
	public void watchClosedThrowsClosedFileWatcherException() throws Exception {
		watcher.close();
		watcher.watch(path);
	}

	@Test(expected = ClosedFileWatcherException.class)
	public void unwatchClosedThrowsClosedFileWatcherException() throws Exception {
		watcher.close();
		watcher.unwatch(path);
	}

	@Test
	public void changedDirectoryFiresEvent() throws Exception {
		watcher.watch(path);
		Path file = path.resolve("file.txt");
		createFile(file);
		long startTime = System.currentTimeMillis();
		while (eventHandler.events.size() == 0) {
			Thread.yield();
			if (System.currentTimeMillis() - startTime > 1000) {
				throw new TimeoutException("Gave up waiting for watch service");
			}
		}
		assertThat(eventHandler.events).containsExactly(new PathChangedEvent(path));
	}

	private static class EventHandler {

		private Queue<PathChangedEvent> events = new LinkedList<>();

		@Subscribe
		public void receiveEvent(PathChangedEvent event) {
			events.add(event);
		}
	}
}
