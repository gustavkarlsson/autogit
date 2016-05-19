package se.gustavkarlsson.autogit.config;

import se.gustavkarlsson.autogit.config.repositories.Repositories;

import java.io.IOException;

public interface Configuration<T> {

	String getUserName();

	Repositories<T> getRepositories() throws IOException;
}
