package se.gustavkarlsson.autogit.file.watcher;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

public final class PathChangedEvent {

	private final Path path;

	public PathChangedEvent(Path path) {
		this.path = checkNotNull(path);
	}

	public Path getPath() {
		return path;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PathChangedEvent that = (PathChangedEvent) o;

		return path.equals(that.path);

	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PathChangedEvent{");
		sb.append("path=").append(path);
		sb.append('}');
		return sb.toString();
	}
}
