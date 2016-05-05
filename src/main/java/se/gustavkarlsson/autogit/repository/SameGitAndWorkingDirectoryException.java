package se.gustavkarlsson.autogit.repository;

public class SameGitAndWorkingDirectoryException extends RepositoryException {

	public SameGitAndWorkingDirectoryException(Throwable cause) {
		super(cause);
	}
}
