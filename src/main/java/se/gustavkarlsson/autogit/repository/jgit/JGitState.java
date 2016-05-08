package se.gustavkarlsson.autogit.repository.jgit;

import org.eclipse.jgit.revwalk.RevCommit;
import se.gustavkarlsson.autogit.repository.State;

import java.time.Instant;

import static com.google.common.base.Preconditions.checkNotNull;

public class JGitState implements State {

	private final RevCommit commit;

	JGitState(RevCommit commit) {
		this.commit = checkNotNull(commit);
	}

	@Override
	public Instant getTime() {
		return Instant.ofEpochSecond(commit.getCommitTime());
	}

	RevCommit getCommit() {
		return commit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		JGitState jGitState = (JGitState) o;

		return commit.equals(jGitState.commit);

	}

	@Override
	public int hashCode() {
		return commit.hashCode();
	}

	@Override
	public String toString() {
		return "JGitState{" +
				"commit=" + commit +
				'}';
	}
}
