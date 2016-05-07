package se.gustavkarlsson.autogit.state;

import org.eclipse.jgit.revwalk.RevCommit;

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
