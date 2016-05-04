package se.gustavkarlsson.autogit.config.repositories;

import java.nio.file.Path;
import java.util.Set;

public interface Repositories<T> {

	Set<T> get() throws RepositoriesException;

	boolean add(T uri) throws RepositoriesException;

	boolean remove(T uri) throws RepositoriesException;
}
