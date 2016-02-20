package se.gustavkarlsson.autogit.repository.git;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import se.gustavkarlsson.autogit.repository.NoRepositoryException;
import se.gustavkarlsson.autogit.repository.Repository;
import se.gustavkarlsson.autogit.repository.RepositoryException;

import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

public class JGitRepository implements Repository {

	private static final String DEFAULT_MESSAGE = "";
	private static final String DEFAULT_EMAIL = "";

	private final Git git;

	public JGitRepository(Path path) throws IOException {
		checkNotNull(path);

		org.eclipse.jgit.lib.Repository repository = getRepository(path);
		git = new Git(repository);
	}

	private static org.eclipse.jgit.lib.Repository getRepository(Path path) throws IOException {
		try {
			return new FileRepositoryBuilder()
					.setWorkTree(path.toFile())
					.setMustExist(true)
					.build();
		} catch (RepositoryNotFoundException e) {
			throw new NoRepositoryException(e);
		}
	}

	@Override
	public void save(String author) {
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
