package se.gustavkarlsson.autogit;

import se.gustavkarlsson.autogit.config.Configuration;
import se.gustavkarlsson.autogit.config.DefaultConfiguration;
import se.gustavkarlsson.autogit.config.repositories.PlainFileRepositories;
import se.gustavkarlsson.autogit.config.repositories.Repositories;
import se.gustavkarlsson.autogit.config.repositories.RepositoriesException;
import se.gustavkarlsson.autogit.saver.Saver;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

	public static void main(String[] args) throws IOException, RepositoriesException {
		Configuration config = new DefaultConfiguration();
		Path repositoriesFile = config.getRepositoriesFile();

		Saver saver = new Saver(config.getUserName());
		Repositories<Path> repositories = new PlainFileRepositories(repositoriesFile);
		repositories.get().forEach(saver::register);
	}
}
