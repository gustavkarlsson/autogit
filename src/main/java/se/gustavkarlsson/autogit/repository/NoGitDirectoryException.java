package se.gustavkarlsson.autogit.repository;

public class NoGitDirectoryException extends RepositoryException {

	public NoGitDirectoryException(Throwable cause) {
		super(cause);
	}
}
