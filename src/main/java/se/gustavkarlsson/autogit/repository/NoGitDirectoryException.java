package se.gustavkarlsson.autogit.repository;

import java.nio.file.Path;

public class NoGitDirectoryException extends RepositoryException {

	public NoGitDirectoryException(Throwable cause) {
		super(cause);
	}

	public NoGitDirectoryException(Path absolutePath) {
		super("Path: " + absolutePath);
	}
}
