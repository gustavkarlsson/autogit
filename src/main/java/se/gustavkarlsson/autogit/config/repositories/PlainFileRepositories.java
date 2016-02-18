package se.gustavkarlsson.autogit.config.repositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

public class PlainFileRepositories implements Repositories {

	public static final String COMMENT_PREFIX = "#";

	private final Path file;

	public PlainFileRepositories(Path file) {
		this.file = checkNotNull(file);
	}

	@Override
	public List<Path> get() throws RepositoriesIOException, RepositoriesInvalidPathException {
		try {
			List<Path> read = read();
			return unmodifiableList(read);
		} catch (IOException e) {
			throw new RepositoriesIOException(e);
		} catch (InvalidPathException e) {
			throw new RepositoriesInvalidPathException(e);
		}
	}

	private List<Path> read() throws IOException {
		try (Stream<String> stream = Files.lines(file)) {
			return  stream.map(PlainFileRepositories::parsePath)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(toList());
		}
	}

	private static Optional<Path> parsePath(String line) {
		String trimmed = line.trim();
		if (trimmed.isEmpty() || trimmed.startsWith(COMMENT_PREFIX)) {
			return Optional.empty();
		}
		Path path = Paths.get(trimmed);
		return Optional.of(path);
	}

	@Override
	public boolean add(Path path) {
		return false;
	}

	@Override
	public boolean remove(Path path) {
		return false;
	}
}
