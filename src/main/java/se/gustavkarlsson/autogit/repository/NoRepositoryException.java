package se.gustavkarlsson.autogit.repository;

public class NoRepositoryException extends RepositoryException {

	public NoRepositoryException(Throwable cause) {
		super(cause);
	}
}
