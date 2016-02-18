package se.gustavkarlsson.autogit.config.repositories;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static org.assertj.core.api.Assertions.assertThat;

public class PlainFileRepositoriesTest {

	private Path nonExistingPath;
	private Path file;
	private Path directory;

	@Before
	public void setUp() throws Exception {
		FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
		nonExistingPath = fs.getPath("non_existing_path");
		file = createFile(fs.getPath("file"));
		directory = createDirectories(fs.getPath("directory"));
	}

	@Test(expected = NullPointerException.class)
	public void createWithNullFileThrowsNullPointerException() throws Exception {
		new PlainFileRepositories(null);
	}

	@Test
	public void createWithNonExistingPathSucceeds() throws Exception {
		new PlainFileRepositories(nonExistingPath);
	}

	@Test
	public void createWithFileSucceeds() throws Exception {
		new PlainFileRepositories(file);
	}

	@Test
	public void createWithDirectorySucceeds() throws Exception {
		new PlainFileRepositories(directory);
	}

	@Test
	public void getWithEmptyFileReturnsEmptySet() throws Exception {
		PlainFileRepositories repos = new PlainFileRepositories(file);

		assertThat(repos.get()).isEmpty();
	}

	@Test(expected = RepositoriesIOException.class)
	public void getWithNonExistingPathThrowsRepositoriesIOException() throws Exception {
		PlainFileRepositories repos = new PlainFileRepositories(nonExistingPath);

		repos.get();
	}

	@Test(expected = RepositoriesIOException.class)
	public void getWithDirectoryThrowsRepositoriesIOException() throws Exception {
		PlainFileRepositories repos = new PlainFileRepositories(directory);

		repos.get();
	}


}
