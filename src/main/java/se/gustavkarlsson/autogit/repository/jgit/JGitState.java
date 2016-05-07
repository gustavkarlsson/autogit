package se.gustavkarlsson.autogit.repository.jgit;

import org.eclipse.jgit.revwalk.RevCommit;
import se.gustavkarlsson.autogit.repository.State;

import java.time.Instant;

import static com.google.common.base.Preconditions.checkNotNull;

public class JGitState implements State {

	private final RevCommit commit;

	public JGitState(RevCommit commit) {
		this.commit = checkNotNull(commit);
	}

	@Override
	public Instant getTime() {
		return Instant.ofEpochSecond(commit.getCommitTime());
	}
}
