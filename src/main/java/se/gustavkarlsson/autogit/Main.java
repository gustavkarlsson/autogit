package se.gustavkarlsson.autogit;

import se.gustavkarlsson.autogit.config.Configuration;
import se.gustavkarlsson.autogit.config.WindowsEnvironmentConfiguration;
import se.gustavkarlsson.autogit.config.repositories.Repositories;
import se.gustavkarlsson.autogit.config.repositories.RepositoriesException;
import se.gustavkarlsson.autogit.saver.Saver;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

	public static void main(String[] args) throws IOException, RepositoriesException {
		Configuration<Path> config = new WindowsEnvironmentConfiguration();

		Saver saver = new Saver(config.getUserName());
		Repositories<Path> repositories = config.getRepositories();
		repositories.get().forEach(saver::register);
	}
}
