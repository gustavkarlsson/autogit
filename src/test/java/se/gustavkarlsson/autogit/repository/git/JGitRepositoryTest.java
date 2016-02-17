package se.gustavkarlsson.autogit.repository.git;

import org.junit.Before;
import org.junit.Test;
import se.gustavkarlsson.autogit.repository.NoRepositoryException;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JGitRepositoryTest {

	private Path nonExistingPath;
	private Path emptyDirectory;
	private Path existingFile;
	private Path existingRepo;

	@Before
	public void setUp() throws Exception {
		Class<?> klass = this.getClass();
		nonExistingPath = Paths.get("does", "not", "exist");
		URL empty = klass.getResource("empty");
		emptyDirectory = Paths.get(empty.toURI());
		existingFile = Paths.get(klass.getResource("file").toURI());
		existingRepo = Paths.get(klass.getResource("repo").toURI());
	}

	@Test(expected = NullPointerException.class)
	public void createWithNullPathThrowsNullPointerException() throws Exception {
		new JGitRepository(null);
	}

	@Test(expected = NoRepositoryException.class)
	public void createWithNonExistingPathThrowsNoRepositoryException() throws Exception {
		new JGitRepository(nonExistingPath);
	}

	@Test(expected = NoRepositoryException.class)
	public void createWithEmptyDirectoryThrowsNoRepositoryException() throws Exception {
		new JGitRepository(emptyDirectory);
	}

	@Test(expected = NoRepositoryException.class)
	public void createWithExistingFileThrowsNoRepositoryException() throws Exception {
		new JGitRepository(existingFile);
	}

	@Test
	public void createWithExistingRepo() throws Exception {
		new JGitRepository(existingRepo);
	}
}
