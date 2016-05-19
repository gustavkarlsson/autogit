package se.gustavkarlsson.autogit.config;

import java.nio.file.Path;

public class EnvironmentConfigurationFactory {

	public Configuration<Path> makeConfiguration() {
		String osName = System.getProperty("os.name");

		if (osName.toLowerCase().startsWith("windows")) {
			return new WindowsEnvironmentConfiguration();
		}
		if (osName.toLowerCase().startsWith("linux")) {
			return new LinuxEnvironmentConfiguration();
		}

		throw new UnsupportedOperationException("OS not yet supported: " + osName);
	}
}
