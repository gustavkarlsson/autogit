package se.gustavkarlsson.autogit.config.repositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

public class PlainFileRepositories implements Repositories {

	private final Path file;

	public PlainFileRepositories(Path file) {
		this.file = checkNotNull(file);
	}

	@Override
	public List<Path> get() throws RepositoriesIOException, RepositoriesInvalidPathException {
		try {
			return unmodifiableList(read());
		} catch (IOException e) {
			throw new RepositoriesIOException(e);
		} catch (InvalidPathException e) {
			throw new RepositoriesInvalidPathException(e);
		}
	}

	private List<Path> read() throws IOException {
		try (Stream<String> stream = Files.lines(file)) {
			return stream.map(s -> Paths.get(s)).collect(toList());
		}
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
