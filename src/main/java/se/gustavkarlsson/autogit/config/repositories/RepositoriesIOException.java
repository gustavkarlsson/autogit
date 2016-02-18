package se.gustavkarlsson.autogit.config.repositories;

import java.io.IOException;

public class RepositoriesIOException extends RepositoriesException {
	public RepositoriesIOException(IOException cause) {
		super(cause);
	}
}
