package se.gustavkarlsson.autogit.saver;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import se.gustavkarlsson.autogit.file.watcher.NativeFileWatcher;
import se.gustavkarlsson.autogit.file.watcher.PathChangedEvent;
import se.gustavkarlsson.autogit.repository.Repository;
import se.gustavkarlsson.autogit.repository.jgit.JGitRepository;
import se.gustavkarlsson.autogit.repository.jgit.JGitState;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class JGitRepositorySaver implements AutoCloseable {

	private final Map<Path, Repository<JGitState>> repositories = new HashMap<>();

	private final NativeFileWatcher watcher;
	private final String author;

	public JGitRepositorySaver(String author) throws IOException {
		final EventBus bus = new EventBus();
		bus.register(this);
		this.watcher = new NativeFileWatcher(bus);
		this.author = checkNotNull(author);
	}

	public boolean register(Path gitDir) {
		try {
			Repository<JGitState> repository = JGitRepository.open(gitDir);
			repositories.put(gitDir, repository);
			return watcher.watch(gitDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean unregister(Path gitDir) {
		repositories.remove(gitDir);
		return watcher.unwatch(gitDir);
	}

	@Subscribe
	public void receiveEvent(PathChangedEvent event) {
		Path path = event.getPath();
		Repository<JGitState> repository = repositories.get(path);
		repository.save(author);
	}

	@Override
	public void close() throws Exception {
		watcher.close();
	}
}
