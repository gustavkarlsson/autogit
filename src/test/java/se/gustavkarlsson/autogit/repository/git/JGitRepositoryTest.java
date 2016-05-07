package se.gustavkarlsson.autogit.repository.git;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.gustavkarlsson.autogit.repository.GitDirectoryInUseException;
import se.gustavkarlsson.autogit.repository.NoGitDirectoryException;
import se.gustavkarlsson.autogit.repository.SameGitAndWorkingDirectoryException;
import se.gustavkarlsson.autogit.state.State;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static java.nio.file.Files.*;
import static org.assertj.core.api.Assertions.assertThat;
import static se.gustavkarlsson.autogit.repository.git.JGitRepository.init;
import static se.gustavkarlsson.autogit.repository.git.JGitRepository.open;

public class JGitRepositoryTest {

	public static final byte[] NO_BYTES = new byte[0];

	private Path tempDir;
	private Path gitDir;
	private Path workDir;

	@Before
	public void setUp() throws Exception {
		tempDir = createTempDirectory(null);
		gitDir = tempDir.resolve(".git");
		workDir = tempDir.resolve("work");
	}

	@After
	public void tearDown() throws Exception {
		deleteRecursive(tempDir);
	}

	@Test(expected = NullPointerException.class)
	public void initWithNullGitDirThrowsNullPointerException() throws Exception {
		init(null, workDir);
	}

	@Test(expected = NullPointerException.class)
	public void initWithNullWorkDirThrowsNullPointerException() throws Exception {
		init(gitDir, null);
	}

	@Test
	public void initWithExistingGitDir() throws Exception {
		createDirectories(gitDir);

		init(gitDir, workDir);
	}

	@Test
	public void initWithNonEmptyGitDir() throws Exception {
		createDirectories(gitDir);
		createFile(gitDir.resolve("file"));

		init(gitDir, workDir);
	}

	@Test
	public void initWithExistingWorkDir() throws Exception {
		createDirectories(workDir);

		init(gitDir, workDir);
	}

	@Test
	public void initWithNonEmptyWorkDir() throws Exception {
		createDirectories(workDir);
		createFile(workDir.resolve("file"));

		init(gitDir, workDir);
	}

	@Test
	public void initWithDeepGitDir() throws Exception {
		init(gitDir.resolve("deep").resolve("deeper"), workDir);
	}

	@Test
	public void initWithDeepWorkDir() throws Exception {
		init(gitDir, workDir.resolve("deep").resolve("deeper"));
	}

	@Test(expected = SameGitAndWorkingDirectoryException.class)
	public void initWithSameGitDirAndWorkDir() throws Exception {
		init(gitDir, gitDir);
	}

	@Test(expected = GitDirectoryInUseException.class)
	public void initAlreadyInitedGitDirThrowsGitDirectoryInUseException() throws Exception {
		init(gitDir, workDir);
		init(gitDir, tempDir.resolve("otherGitDir"));
	}

	@Test(expected = NullPointerException.class)
	public void openWithNullGitDirThrowsNullPointerException() throws Exception {
		open(null);
	}

	@Test(expected = NoGitDirectoryException.class)
	public void openWithEmptyGitDirThrowsNoGitDirectoryException() throws Exception {
		createDirectories(workDir);
		createDirectories(gitDir);

		open(gitDir);
	}

	@Test(expected = NoGitDirectoryException.class)
	public void openWithNonExistingGitDirThrowsNoGitDirectoryException() throws Exception {
		createDirectories(workDir);
		open(gitDir);
	}

	@Test
	public void openWithNonExistingWorkDir() throws Exception {
		createNewRepo(gitDir, workDir);
		deleteRecursive(workDir);

		open(gitDir);
	}

	@Test(expected = NoGitDirectoryException.class)
	public void saveWithDeletedGitDirThrowsException() throws Exception {
		createNewRepo(gitDir, workDir);
		JGitRepository repo = JGitRepository.open(gitDir);
		deleteRecursive(gitDir);

		repo.save("user");
	}

	@Test(expected = NoGitDirectoryException.class)
	public void listWithDeletedGitDirThrowsException() throws Exception {
		createNewRepo(gitDir, workDir);
		JGitRepository repo = JGitRepository.open(gitDir);
		deleteRecursive(gitDir);

		repo.list();
	}

	@Test(expected = NullPointerException.class)
	public void saveWithNullAuthorThrowsNullPointerException() throws Exception {
		createNewRepo(gitDir, workDir);
		JGitRepository repo = JGitRepository.open(gitDir);

		repo.save(null);
	}

	@Test
	public void saveNewRepositoryCreatesNoState() throws Exception {
		Repository jgitRepo = createNewRepo(gitDir, workDir);
		JGitRepository repo = JGitRepository.open(gitDir);

		assertThat(repo.save("user")).isFalse();
		assertThat(new Git(jgitRepo).reflog().call()).isEmpty();
	}

	@Test
	public void saveNewFile() throws Exception {
		Repository jgitRepo = createNewRepo(gitDir, workDir);
		JGitRepository repo = JGitRepository.open(gitDir);
		String fileName = "file.txt";
		createFile(workDir.resolve(fileName));

		assertThat(repo.save("user")).isTrue();
		assertThat(getCommitFileContents(jgitRepo, "HEAD", fileName)).isEqualTo(NO_BYTES);
		assertThat(isRepoClean(jgitRepo)).isTrue();
	}

	@Test
	public void saveNewFolder() throws Exception {
		Repository jgitRepo = createNewRepo(gitDir, workDir);
		JGitRepository repo = JGitRepository.open(gitDir);
		String folderName = "folder";
		createDirectories(workDir.resolve(folderName));

		assertThat(repo.save("user")).isFalse();
		assertThat(new Git(jgitRepo).reflog().call()).isEmpty();
		assertThat(isRepoClean(jgitRepo)).isTrue();
	}

	@Test
	public void saveModifiedFile() throws Exception {
		Repository jgitRepo = createNewRepo(gitDir, workDir);
		JGitRepository repo = JGitRepository.open(gitDir);
		String fileName = "file.txt";
		Path file = createFile(workDir.resolve(fileName));
		commitFile(jgitRepo, file);
		byte[] contents = "text".getBytes();
		write(file, contents);

		assertThat(repo.save("user")).isTrue();
		assertThat(getCommitFileContents(jgitRepo, "HEAD", fileName)).isEqualTo(contents);
		assertThat(getCommitFileContents(jgitRepo, "HEAD^1", fileName)).isEqualTo(NO_BYTES);
		assertThat(isRepoClean(jgitRepo)).isTrue();
	}

	@Test
	public void saveDeletedFile() throws Exception {
		Repository jgitRepo = createNewRepo(gitDir, workDir);
		JGitRepository repo = JGitRepository.open(gitDir);
		String fileName = "file.txt";
		Path file = createFile(workDir.resolve(fileName));
		commitFile(jgitRepo, file);
		delete(file);

		assertThat(repo.save("user")).isTrue();
		assertThat(getCommitFileContents(jgitRepo, "HEAD", fileName)).isNull();
		assertThat(getCommitFileContents(jgitRepo, "HEAD^1", fileName)).isEqualTo(NO_BYTES);
		assertThat(isRepoClean(jgitRepo)).isTrue();
	}

	@Test
	public void listEmpty() throws Exception {
		createNewRepo(gitDir, workDir);
		JGitRepository repo = JGitRepository.open(gitDir);

		List<State> list = repo.list();

		assertThat(list).isEmpty();
	}

	@Test
	public void listSingleState() throws Exception {
		Repository jgitRepo = createNewRepo(gitDir, workDir);
		JGitRepository repo = JGitRepository.open(gitDir);
		Path file = gitDir.resolve("file.txt");
		commitFile(jgitRepo, file);

		List<State> list = repo.list();

		assertThat(list).hasSize(1);
	}

	private static Repository createNewRepo(Path gitDir, Path workDir) throws IOException, GitAPIException {
		return Git.init()
				.setGitDir(gitDir.toFile())
				.setDirectory(workDir.toFile())
				.call()
				.getRepository();
	}

	private static void commitFile(Repository jgitRepo, Path file) throws IOException, GitAPIException {
		if (!exists(file)) {
			createFile(file);
		}
		Git git = new Git(jgitRepo);
		AddCommand add = git.add();
		add.addFilepattern(file.getFileName().toString());
		add.call();
		CommitCommand commit = git.commit();
		commit.setMessage("Added file: " + file);
		commit.call();
	}

	private static boolean isRepoClean(Repository jgitRepo) throws GitAPIException, IOException {
		return new Git(jgitRepo).status().call().isClean();
	}

	private static byte[] getCommitFileContents(Repository jgitRepo, String commitish, String fileName) throws IOException {
		ObjectId lastCommitId = jgitRepo.resolve(commitish);

		try (RevWalk revWalk = new RevWalk(jgitRepo)) {
			RevCommit commit = revWalk.parseCommit(lastCommitId);
			RevTree tree = commit.getTree();

			try (TreeWalk treeWalk = new TreeWalk(jgitRepo)) {
				treeWalk.addTree(tree);
				treeWalk.setRecursive(true);
				treeWalk.setFilter(PathFilter.create(fileName));
				if (!treeWalk.next()) {
					return null;
				}
				ObjectId objectId = treeWalk.getObjectId(0);
				ObjectLoader loader = jgitRepo.open(objectId);

				byte[] contents = loader.getBytes();
				revWalk.dispose();
				return contents;
			}
		}
	}

	private static void deleteRecursive(final Path path) throws IOException {
		walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
				attemptToDelete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
				if (exception == null) {
					attemptToDelete(dir);
					return FileVisitResult.CONTINUE;
				}
				throw exception;
			}

			private void attemptToDelete(Path path) throws IOException {
				if (!path.toFile().delete()) {
					delete(path);
				}
			}
		});
	}
}
