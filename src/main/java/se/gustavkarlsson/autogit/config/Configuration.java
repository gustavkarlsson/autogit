package se.gustavkarlsson.autogit.config;

import java.io.IOException;
import java.nio.file.Path;

public interface Configuration {

	String getUserName();

	Path getRepositoriesFile() throws IOException;
}
