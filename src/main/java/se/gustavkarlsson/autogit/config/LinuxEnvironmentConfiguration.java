package se.gustavkarlsson.autogit.config;

public class LinuxEnvironmentConfiguration extends AbstractEnvironmentConfiguration {

	public static final String USER_NAME_ENVIRONMENT_VARIABLE = "USER";
	public static final String HOME_PATH_ENVIRONMENT_VARIABLE = "HOME";

	@Override
	protected String getUserNameEnvironmentVariable() {
		return USER_NAME_ENVIRONMENT_VARIABLE;
	}

	@Override
	protected String getHomePathEnvironmentVariable() {
		return HOME_PATH_ENVIRONMENT_VARIABLE;
	}
}
