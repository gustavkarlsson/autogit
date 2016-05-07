package se.gustavkarlsson.autogit.repository.jgit.exceptions;

import se.gustavkarlsson.autogit.repository.RepositoryException;

public class NoDirectoryException extends RepositoryException {

	public NoDirectoryException(Throwable cause) {
		super(cause);
	}
}
