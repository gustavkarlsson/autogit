package se.gustavkarlsson.autogit.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultConfiguration implements Configuration {

	public static final String CONFIGURATION_DIRECTORY_NAME = ".autogit";

	public static final String homePath = System.getenv("HOMEPATH");

	public static final String userName = System.getenv("USERNAME");

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public Path getRepositoriesFile() throws IOException {
		return create(getConfigurationDirectory().resolve("repositories.cfg"));
	}

	private static Path create(Path file) throws IOException {
		Files.createDirectories(file.getParent());
		if (!Files.exists(file)) {
			Files.createFile(file);
		}
		return file;
	}

	private static Path getConfigurationDirectory() {
		return Paths.get(homePath, CONFIGURATION_DIRECTORY_NAME);
	}
}
