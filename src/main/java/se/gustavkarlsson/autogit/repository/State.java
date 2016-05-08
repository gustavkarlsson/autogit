package se.gustavkarlsson.autogit.repository;

import java.time.Instant;

public interface State extends Comparable<State> {

	Instant getTime();

	@Override
	default int compareTo(State o) {
		return getTime().compareTo(o.getTime());
	}
}
