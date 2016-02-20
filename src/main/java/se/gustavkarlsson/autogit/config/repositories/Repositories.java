package se.gustavkarlsson.autogit.config.repositories;

import java.nio.file.Path;
import java.util.Set;

public interface Repositories {

	Set<Path> get() throws RepositoriesException;

	boolean add(Path path) throws RepositoriesException;

	boolean remove(Path path) throws RepositoriesException;
}
