package se.gustavkarlsson.autogit.config.repositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

public class PlainFileRepositories implements Repositories<Path> {

	private final Path file;

	public PlainFileRepositories(Path file) {
		this.file = checkNotNull(file);
	}

	@Override
	public Set<Path> get() throws RepositoriesIOException, RepositoriesInvalidPathException {
		try {
			return unmodifiableSet(read());
		} catch (IOException e) {
			throw new RepositoriesIOException(e);
		} catch (InvalidPathException e) {
			throw new RepositoriesInvalidPathException(e);
		}
	}

	@Override
	public boolean add(Path path) throws RepositoriesIOException, RepositoriesInvalidPathException {
		try {
			Set<Path> paths = read();
			if (paths.contains(path)) {
				return false;
			}
			paths.add(path);
			write(paths);
			return true;
		} catch (IOException e) {
			throw new RepositoriesIOException(e);
		} catch (InvalidPathException e) {
			throw new RepositoriesInvalidPathException(e);
		}
	}

	private Set<Path> read() throws IOException {
		try (Stream<String> stream = Files.lines(file)) {
			return stream.map(PlainFileRepositories::parsePath)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(toSet());
		}
	}

	private static Optional<Path> parsePath(String line) {
		String trimmed = line.trim();
		if (trimmed.isEmpty()) {
			return Optional.empty();
		}
		Path path = Paths.get(trimmed);
		return Optional.of(path);
	}

	private void write(Set<Path> paths) throws IOException {
		List<String> pathStrings = paths.stream()
				.map(Path::toAbsolutePath)
				.map(Path::toString)
				.collect(Collectors.toList());

		Files.write(file, pathStrings);
	}

	@Override
	public boolean remove(Path path) {
		return false;
	}
}
