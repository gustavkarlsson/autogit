package se.gustavkarlsson.autogit.file.watcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public interface FileWatcher {

	boolean watch(Path path) throws IOException;

	boolean unwatch(Path path);

	Set<Path> getWatched();

	void clear();

}
