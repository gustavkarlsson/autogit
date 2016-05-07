package se.gustavkarlsson.autogit.repository;

import java.util.List;

public interface Repository<T extends State> {

	boolean save(String author);

	List<T> list();

	void revert(T state);
}
