package se.gustavkarlsson.autogit.repository.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.gustavkarlsson.autogit.repository.GitDirectoryInUseException;
import se.gustavkarlsson.autogit.repository.NoGitDirectoryException;
import se.gustavkarlsson.autogit.repository.SameGitAndWorkingDirectoryException;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.Files.*;
import static org.assertj.core.api.Assertions.assertThat;
import static se.gustavkarlsson.autogit.repository.git.JGitRepository.init;
import static se.gustavkarlsson.autogit.repository.git.JGitRepository.open;

public class JGitRepositoryTest {

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
		recursiveDelete(tempDir);
	}

	// Init

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

	// Open

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
		recursiveDelete(workDir);

		open(gitDir);
	}

	@Test(expected = NullPointerException.class)
	public void saveWithNullAuthorThrowsNullPointerException() throws Exception {
		createNewRepo(gitDir, workDir);
		JGitRepository jGitRepo = JGitRepository.open(gitDir);

		jGitRepo.save(null);
	}

	@Test
	public void saveWithNoChanges() throws Exception {
		createNewRepo(gitDir, workDir);
		JGitRepository jGitRepo = JGitRepository.open(gitDir);

		jGitRepo.save("user");
	}

	@Test
	public void saveNewFile() throws Exception {
		Repository repo = createNewRepo(gitDir, workDir);
		JGitRepository jGitRepo = JGitRepository.open(gitDir);
		createFile(gitDir.resolve("file.txt"));

		jGitRepo.save("user");

		assertRepoClean(repo);
	}

	@Test
	public void saveModifiedFile() throws Exception {
		Repository repo = createNewRepo(gitDir, workDir);
		JGitRepository jGitRepo = JGitRepository.open(gitDir);
		Path file = createFile(gitDir.resolve("file.txt"));

		jGitRepo.save("user");
		write(file, "text".getBytes());
		jGitRepo.save("user");

		assertRepoClean(repo);
	}

	@Test
	public void saveDeletedFile() throws Exception {
		Repository repo = createNewRepo(gitDir, workDir);
		JGitRepository jGitRepo = JGitRepository.open(gitDir);
		Path file = createFile(gitDir.resolve("file.txt"));

		jGitRepo.save("user");
		delete(file);
		jGitRepo.save("user");

		assertRepoClean(repo);
	}

	private static Repository createNewRepo(Path gitDir, Path workDir) throws IOException, GitAPIException {
		return Git.init()
				.setGitDir(gitDir.toFile())
				.setDirectory(workDir.toFile())
				.call()
				.getRepository();
	}

	private static void assertRepoClean(Repository repo) throws GitAPIException, IOException {
		assertThat(new Git(repo).status().call().isClean()).isTrue();
	}

	public static void recursiveDelete(final Path path) throws IOException {
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
