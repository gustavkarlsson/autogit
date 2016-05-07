package se.gustavkarlsson.autogit.repository.jgit.exceptions;

import se.gustavkarlsson.autogit.repository.RepositoryException;

import java.nio.file.Path;

public class GitDirectoryInUseException extends RepositoryException {

	public GitDirectoryInUseException(Path path) {
		super("Path: " + path);
	}
}
