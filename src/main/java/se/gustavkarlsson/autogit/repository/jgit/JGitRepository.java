package se.gustavkarlsson.autogit.repository.jgit;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.revwalk.RevCommit;
import se.gustavkarlsson.autogit.repository.Repository;
import se.gustavkarlsson.autogit.repository.RepositoryException;
import se.gustavkarlsson.autogit.repository.jgit.exceptions.GitDirectoryInUseException;
import se.gustavkarlsson.autogit.repository.jgit.exceptions.NoGitDirectoryException;
import se.gustavkarlsson.autogit.repository.jgit.exceptions.SameGitAndWorkingDirectoryException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;

public class JGitRepository implements Repository<JGitState> {

	private static final String DEFAULT_MESSAGE = "";
	private static final String DEFAULT_EMAIL = "";

	private final Git git;

	public static JGitRepository open(Path gitDir) {
		checkNotNull(gitDir);
		return new JGitRepository(gitDir);
	}

	public static JGitRepository init(Path gitDir, Path workDir) {
		checkNotNull(gitDir);
		checkNotNull(workDir);
		return new JGitRepository(gitDir, workDir);
	}

	private JGitRepository(Path gitDir) {
		try {
			git = Git.open(gitDir.toFile());
		} catch (RepositoryNotFoundException e) {
			throw new NoGitDirectoryException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private JGitRepository(Path gitDir, Path workDir) {
		try {
			try {
				Git.open(gitDir.toFile());
				throw new GitDirectoryInUseException(gitDir);
			} catch (RepositoryNotFoundException e) {
				// Exception expected here.
			}
			git = Git.init().setGitDir(gitDir.toFile()).setDirectory(workDir.toFile()).call();
		} catch (GitAPIException | IOException e) {
			throw new RuntimeException(e);
		} catch (IllegalStateException e) {
			if (e.getMessage().contains("both folders should not point to the same location")) {
				throw new SameGitAndWorkingDirectoryException(e);
			}
			throw e;
		}
	}

	@Override
	public boolean save(String author) {
		checkNotNull(author);
		checkGitDirExists();
		try {
			if (!git.status().call().isClean()) {
				addAll();
				commitAll(author);
				return true;
			}
		} catch (GitAPIException e) {
			throw new RepositoryException(e);
		}
		return false;
	}

	@Override
	public List<JGitState> list() {
		checkGitDirExists();
		try {
			LogCommand log = git.log();
			Iterable<RevCommit> commits = log.call();
			Stream<RevCommit> stream = StreamSupport.stream(commits.spliterator(), false);
			return stream.map(JGitState::new).collect(Collectors.toList());
		} catch (NoHeadException e) {
			return Collections.emptyList();
		} catch (GitAPIException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void revert(JGitState state) {
		checkNotNull(state);
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	private void checkGitDirExists() {
		Path gitDir = git.getRepository().getDirectory().toPath();
		if (!Files.exists(gitDir) || !Files.isDirectory(gitDir)) {
			throw new NoGitDirectoryException(gitDir);
		}
	}

	private void addAll() throws GitAPIException {
		AddCommand add = git.add();
		add.addFilepattern(".");
		add.call();
	}

	private void commitAll(String author) throws GitAPIException {
		CommitCommand commit = git.commit();
		commit.setMessage(DEFAULT_MESSAGE);
		commit.setAuthor(author, DEFAULT_EMAIL);
		commit.setAll(true);
		commit.call();
	}
}
