package se.gustavkarlsson.autogit.config.repositories;

import java.nio.file.InvalidPathException;

public class RepositoriesInvalidPathException extends RepositoriesException {
	public RepositoriesInvalidPathException(InvalidPathException e) {
		super(e);
	}
}
