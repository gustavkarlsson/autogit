package se.gustavkarlsson.autogit.repository.git;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import se.gustavkarlsson.autogit.repository.*;

import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

public class JGitRepository implements Repository {

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
				throw new GitDirectoryInUseException();
			} catch (IOException e) {
				// Exception expected here.
			}
			git = Git.init().setGitDir(gitDir.toFile()).setDirectory(workDir.toFile()).call();
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		} catch (IllegalStateException e) {
			if (e.getMessage().contains("both folders should not point to the same location")) {
				throw new SameGitAndWorkingDirectoryException(e);
			}
			throw e;
		}
	}

	@Override
	public void save(String author) {
		checkNotNull(author);
		try {
			addAll();
			commitAll(author);
		} catch (GitAPIException e) {
			throw new RepositoryException(e);
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
