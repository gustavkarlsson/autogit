package se.gustavkarlsson.autogit.repository.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.gustavkarlsson.autogit.repository.NoRepositoryException;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.assertj.core.api.Assertions.assertThat;

public class JGitRepositoryTest {

	private Path tempDir;

	@Before
	public void setUp() throws Exception {
		tempDir = Files.createTempDirectory(null);
	}

	@After
	public void tearDown() throws Exception {
		recursiveDelete(tempDir);
	}

	@Test(expected = NullPointerException.class)
	public void createWithNullPathThrowsNullPointerException() throws Exception {
		new JGitRepository(null);
	}

	@Test(expected = NoRepositoryException.class)
	public void createWithNonExistingPathThrowsNoRepositoryException() throws Exception {
		Path path = tempDir.resolve("does_not_exist");

		new JGitRepository(path);
	}

	@Test(expected = NoRepositoryException.class)
	public void createWithEmptyDirectoryThrowsNoRepositoryException() throws Exception {
		Path path = Files.createDirectories(tempDir.resolve("empty_directory"));

		new JGitRepository(path);
	}

	@Test(expected = NoRepositoryException.class)
	public void createWithExistingFileThrowsNoRepositoryException() throws Exception {
		Path path = Files.createDirectories(tempDir.resolve("file"));

		new JGitRepository(path);
	}

	@Test
	public void createWithExistingRepo() throws Exception {
		Path path = Files.createDirectories(tempDir.resolve("repo"));
		createNewRepo(path);

		new JGitRepository(path);
	}

	@Test(expected = NullPointerException.class)
	public void saveWithNullAuthorThrowsNullPointerException() throws Exception {
		Path path = Files.createDirectories(tempDir.resolve("repo"));
		createNewRepo(path);
		JGitRepository repo = new JGitRepository(path);

		repo.save(null);
	}

	@Test
	public void saveWithNoChanges() throws Exception {
		Path path = Files.createDirectories(tempDir.resolve("repo"));
		createNewRepo(path);
		JGitRepository repo = new JGitRepository(path);

		repo.save("user");
	}

	@Test
	public void saveNewFile() throws Exception {
		Path path = Files.createDirectories(tempDir.resolve("repo"));
		Repository jGitRepo = createNewRepo(path);
		JGitRepository repo = new JGitRepository(path);
		Files.createFile(path.resolve("file.txt"));

		repo.save("user");

		assertRepoClean(jGitRepo);
	}

	@Test
	public void saveModifiedFile() throws Exception {
		Path path = Files.createDirectories(tempDir.resolve("repo"));
		Repository jGitRepo = createNewRepo(path);
		JGitRepository repo = new JGitRepository(path);
		Path file = Files.createFile(path.resolve("file.txt"));

		repo.save("user");
		Files.write(file, "text".getBytes());
		repo.save("user");

		assertRepoClean(jGitRepo);
	}

	@Test
	public void saveDeletedFile() throws Exception {
		Path path = Files.createDirectories(tempDir.resolve("repo"));
		Repository jGitRepo = createNewRepo(path);
		JGitRepository repo = new JGitRepository(path);
		Path file = Files.createFile(path.resolve("file.txt"));

		repo.save("user");
		Files.delete(file);
		repo.save("user");

		assertRepoClean(jGitRepo);
	}

	private static Repository createNewRepo(Path path) throws IOException {
		Repository repo = new RepositoryBuilder()
				.setWorkTree(path.toFile())
				.setMustExist(false)
				.build();
		repo.create();
		return repo;
	}

	private static void assertRepoClean(Repository repo) throws GitAPIException, IOException {
		assertThat(new Git(repo).status().call().isClean()).isTrue();
	}

	public static void recursiveDelete(final Path path) {
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
					if (e == null) {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}
					throw e;
				}
			});
		} catch (IOException e) {
			throw new RuntimeException("Failed to delete " + path, e);
		}
	}
}
