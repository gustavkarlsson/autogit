package se.gustavkarlsson.autogit.config.repositories;

import java.nio.file.Path;
import java.util.List;

public interface Repositories {

	List<Path> get() throws RepositoriesException;

	boolean add(Path path) throws RepositoriesException;

	boolean remove(Path path) throws RepositoriesException;
}
