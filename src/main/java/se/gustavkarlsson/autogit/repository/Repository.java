package se.gustavkarlsson.autogit.repository;

import se.gustavkarlsson.autogit.state.State;

import java.util.List;

public interface Repository {

	void save(String author);

	List<State> list();
}
