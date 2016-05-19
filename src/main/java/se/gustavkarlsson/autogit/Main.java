package se.gustavkarlsson.autogit;

import se.gustavkarlsson.autogit.config.Configuration;
import se.gustavkarlsson.autogit.config.EnvironmentConfigurationFactory;
import se.gustavkarlsson.autogit.config.repositories.Repositories;
import se.gustavkarlsson.autogit.config.repositories.RepositoriesException;
import se.gustavkarlsson.autogit.saver.JGitRepositorySaver;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

	public static void main(String[] args) throws IOException, RepositoriesException {
		Configuration<Path> config = new EnvironmentConfigurationFactory().makeConfiguration();
		JGitRepositorySaver saver = new JGitRepositorySaver(config.getUserName());
		Repositories<Path> repositories = config.getRepositories();
		repositories.get().forEach(saver::register);

		Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
			@Override
			public void run() {
				try {
					saver.close();
				} catch (Exception e) {
					throw new RuntimeException("Failed to close saver", e);
				}
			}
		});
	}
}
