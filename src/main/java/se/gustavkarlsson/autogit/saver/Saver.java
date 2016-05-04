package se.gustavkarlsson.autogit.saver;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import se.gustavkarlsson.autogit.file.watcher.FileWatcher;
import se.gustavkarlsson.autogit.file.watcher.NativeFileWatcher;
import se.gustavkarlsson.autogit.file.watcher.PathChangedEvent;
import se.gustavkarlsson.autogit.repository.Repository;
import se.gustavkarlsson.autogit.repository.git.JGitRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class Saver {

	private final Map<Path, Repository> repositories = new HashMap<>();

	private final FileWatcher watcher;
	private final String author;

	public Saver(String author) throws IOException {
		final EventBus bus = new EventBus();
		bus.register(this);
		this.watcher = new NativeFileWatcher(bus);
		this.author = checkNotNull(author);
	}

	public void register(Path repositoryPath) {
		try {
			Repository repository = new JGitRepository(repositoryPath);
			repositories.put(repositoryPath, repository);
			watcher.watch(repositoryPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Subscribe
	public void receiveEvent(PathChangedEvent event) {
		Path path = event.getPath();
		Repository repository = repositories.get(path);
		repository.save(author);
	}

}
