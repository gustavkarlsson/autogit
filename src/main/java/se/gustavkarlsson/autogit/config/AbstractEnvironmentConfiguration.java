package se.gustavkarlsson.autogit.config;

import se.gustavkarlsson.autogit.config.repositories.PlainFileRepositories;
import se.gustavkarlsson.autogit.config.repositories.Repositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractEnvironmentConfiguration implements Configuration<Path> {

	public static final String CONFIGURATION_DIRECTORY_NAME = ".autogit";
	public static final String REPOSITORIES_FILE_NAME = "repositories.cfg";

	@Override
	public String getUserName() {
		return System.getenv(getUserNameEnvironmentVariable());
	}

	@Override
	public Repositories<Path> getRepositories() throws IOException {
		Path path = create(getConfigurationDirectory().resolve(REPOSITORIES_FILE_NAME));
		return new PlainFileRepositories(path);
	}

	private static Path create(Path file) throws IOException {
		Path parent = file.getParent();
		if (parent == null) {
			throw new RuntimeException("Could not get parent of file: " + file);
		}
		Files.createDirectories(parent);
		if (!Files.exists(file)) {
			Files.createFile(file);
		}
		return file;
	}

	private Path getConfigurationDirectory() {
		String homePath = System.getenv(getHomePathEnvironmentVariable());
		return Paths.get(homePath, CONFIGURATION_DIRECTORY_NAME);
	}

	protected abstract String getHomePathEnvironmentVariable();

	protected abstract String getUserNameEnvironmentVariable();
}
