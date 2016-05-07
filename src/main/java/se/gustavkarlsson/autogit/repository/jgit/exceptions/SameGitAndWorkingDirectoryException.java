package se.gustavkarlsson.autogit.repository.jgit.exceptions;

import se.gustavkarlsson.autogit.repository.RepositoryException;

public class SameGitAndWorkingDirectoryException extends RepositoryException {

	public SameGitAndWorkingDirectoryException(Throwable cause) {
		super(cause);
	}
}
