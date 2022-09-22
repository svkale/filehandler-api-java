package test.com.filehandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import main.java.com.filehandler.FileHandler;

/**
 * Test all functionalities of FileHandler.
 */
public class FileHandlerTest {
	/**
	 * Tests following functions:-
	 * <ol>
	 * <li>setCHRoot</li>
	 * <li>exists</li>
	 * <li>createFile</li>
	 * <li>readFile</li>
	 * <li>write</li>
	 * <li>delete</li>
	 * <li>getInfo</li>
	 * </ol>
	 */
	@Test
	public void main() {
		FileHandler fh = new FileHandler();

		assertTrue(fh.setCHRoot("D:\\Siddharth\\JavaOOPProject\\test-files"));
		assertTrue(!fh.setCHRoot("E:\\Siddharth\\JavaOOPProject\\test-files"));

		assertEquals(fh.getCHRoot(), "D:\\Siddharth\\JavaOOPProject\\test-files");

		assertTrue(fh.exists("test.txt"));
		assertTrue(fh.exists("testf"));
		assertTrue(!fh.exists("non-exist.txt"));
		assertTrue(!fh.exists("non-exist"));

		assertTrue(fh.createFile("test2.txt"));
		assertTrue(!fh.createFile("test.txt"));

		assertEquals(new String(fh.readFile("test.txt")), "test");

		assertTrue(fh.write("test2.txt", "Hi! Just testing...\n".getBytes(), false));
		assertTrue(fh.write("test2.txt", "This line is appended.\nAnd this too....".getBytes(), true));

		assertTrue(fh.delete("test2.txt"));
		assertTrue(!fh.delete("not-exist.txt"));

		assertEquals(fh.getInfo("../logs.txt"), null);
		assertEquals(fh.getInfo("."), null);
		assertEquals(fh.getInfo("not-exists.txt"), null);

		assertEquals(fh.getInfo("test.txt").path, "test.txt");
		assertEquals(fh.getInfo("test.txt").size, 4);
		assertEquals(fh.getInfo("test.txt").lastModified, "2022-03-07T18:22:22.562908Z");

		assertEquals(fh.getFileSize("test.txt"), 4);

		assertEquals(fh.getInfo("testf").path, "testf");
		assertEquals(fh.getInfo("testf").size, 0);
	}
}
