package se.gustavkarlsson.autogit.repository.jgit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.StreamSupport.stream;

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
			checkRepositoryNotInUse(gitDir);
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

	private void checkRepositoryNotInUse(Path gitDir) throws IOException {
		try {
			Git.open(gitDir.toFile());
			throw new GitDirectoryInUseException(gitDir);
		} catch (RepositoryNotFoundException e) {
			// Exception expected here.
		}
	}

	@Override
	public boolean save(String author) {
		checkNotNull(author);
		checkGitDirExists();
		try {
			if (isClean()) {
				return false;
			}
			commitAll(author);
			return true;
		} catch (GitAPIException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public List<JGitState> listStates() {
		checkGitDirExists();
		try {
			Iterable<RevCommit> commits = getAllCommits();
			Stream<RevCommit> stream = stream(commits.spliterator(), false);
			return stream.map(JGitState::new).collect(Collectors.toList());
		} catch (NoHeadException e) {
			return Collections.emptyList();
		} catch (GitAPIException | IOException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void load(JGitState state) {
		checkNotNull(state);
		checkGitDirExists();
		try {
			RevCommit commit = state.getCommit();
			Optional<Ref> branch = getBranch(commit);
			if (!branch.isPresent()) {
				branch = Optional.of(createBranch(commit));
			}
			checkout(branch.get());
		} catch (GitAPIException e) {
			throw new RepositoryException(e);
		}
	}

	private Optional<Ref> getBranch(RevCommit commit) throws GitAPIException {
		return stream(git.branchList().call().spliterator(), false)
				.filter(r -> Objects.equals(r.getObjectId(), commit.getId()))
				.findFirst();
	}

	private void checkGitDirExists() {
		Path gitDir = git.getRepository().getDirectory().toPath();
		if (!Files.exists(gitDir) || !Files.isDirectory(gitDir)) {
			throw new NoGitDirectoryException(gitDir);
		}
	}

	private boolean isClean() throws GitAPIException {
		return git.status().call().isClean();
	}

	private Ref createBranch(RevCommit commit) throws GitAPIException {
		return git.branchCreate()
				.setStartPoint(commit)
				.setName(nextBranchName())
				.call();
	}

	private String nextBranchName() throws GitAPIException {
		List<String> branchNames = listBranches().stream()
				.map(Ref::getName)
				.collect(Collectors.toList());
		int i = 0;
		String name;
		do {
			i++;
			name = "branch" + i;
		} while (branchNames.contains(name));
		return name;
	}

	private List<Ref> listBranches() throws GitAPIException {
		return stream(git.branchList().call().spliterator(), false)
				.collect(Collectors.toList());
	}

	private void commitAll(String author) throws GitAPIException {
		git.add()
				.addFilepattern(".")
				.call();
		git.commit()
				.setMessage(DEFAULT_MESSAGE)
				.setAuthor(author, DEFAULT_EMAIL)
				.setAll(true)
				.call();
	}

	private void checkout(Ref branch) throws GitAPIException {
		git.checkout()
				.setName(branch.getName())
				.call();
	}

	private Iterable<RevCommit> getAllCommits() throws GitAPIException, IOException {
		return git.log()
				.all()
				.call();
	}
}
