package se.gustavkarlsson.autogit.state;

import org.eclipse.jgit.revwalk.RevCommit;

import static com.google.common.base.Preconditions.checkNotNull;

public class GitCommit implements State {

	private final RevCommit commit;

	public GitCommit(RevCommit commit) {
		this.commit = checkNotNull(commit);
	}
}
