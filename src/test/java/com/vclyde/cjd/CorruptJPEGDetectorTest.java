package com.vclyde.cjd;

import java.io.File;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for CorruptJPEGDetector class
 *
 * @author Clyde M. Velasquez
 * @version 1.1
 * @since 10/24/16.
 */
public class CorruptJPEGDetectorTest {

	private static final File ROOT_DIR = new File("").getAbsoluteFile();
	private static final File RESOURCES_DIR = new File(ROOT_DIR, "src/main/resources/");

	@Before
	public void init() {
		assertTrue(RESOURCES_DIR.exists());
	}

	@Test
	public void isJPEG() throws Exception {

		// Only jpeg files
		File[] testFiles = RESOURCES_DIR.listFiles((dir, name) -> !name.contains(".png") && !name.contains(".gif"));
		CorruptJPEGDetector cjd;

		assertNotNull(testFiles);
		for (File jpg : testFiles) {
			cjd = new CorruptJPEGDetector(jpg.toPath());
			assertTrue(cjd.isJPEG());
		}

		File png = new File(RESOURCES_DIR, "image6.png");
		cjd = new CorruptJPEGDetector(png.toPath(), true);
		assertFalse(cjd.isJPEG());
	}

	@Test
	public void isFileComplete() throws Exception {
		File image1 = new File(RESOURCES_DIR, "image1.jpg");
		CorruptJPEGDetector cjd = new CorruptJPEGDetector(image1.toPath());
		assertFalse(cjd.isFileComplete());

		File image2 = new File(RESOURCES_DIR, "image2.jpg");
		cjd = new CorruptJPEGDetector(image2.toPath());
		assertTrue(cjd.isFileComplete());

		File image3 = new File(RESOURCES_DIR, "image3.jpg");
		cjd = new CorruptJPEGDetector(image3.toPath());
		assertTrue(cjd.isFileComplete());
	}

	@Test
	public void isCorrupt() throws Exception {
		File image1 = new File(RESOURCES_DIR, "image1.jpg");
		CorruptJPEGDetector cjd = new CorruptJPEGDetector(image1.toPath());
		assertTrue(cjd.isCorrupt());

		File image2 = new File(RESOURCES_DIR, "image2.jpg");
		cjd = new CorruptJPEGDetector(image2.toPath());
		assertTrue(cjd.isCorrupt());

		File image3 = new File(RESOURCES_DIR, "image3.jpg");
		cjd = new CorruptJPEGDetector(image3.toPath());
		assertFalse(cjd.isCorrupt());

		File image4 = new File(RESOURCES_DIR, "image4.jpg");
		cjd = new CorruptJPEGDetector(image4.toPath());
		assertTrue(cjd.isCorrupt());

		File image5 = new File(RESOURCES_DIR, "image5.jpg");
		cjd = new CorruptJPEGDetector(image5.toPath());
		assertTrue(cjd.isCorrupt());

		File image7 = new File(RESOURCES_DIR, "image7.jpg");
		cjd = new CorruptJPEGDetector(image7.toPath());
		assertTrue(cjd.isCorrupt());

		File image8 = new File(RESOURCES_DIR, "image8.jpeg");
		cjd = new CorruptJPEGDetector(image8.toPath());
		assertTrue(cjd.isCorrupt());
	}

	@Test
	public void printHexDump() throws Exception {
		File image3 = new File(RESOURCES_DIR, "image3.jpg");
		CorruptJPEGDetector cjd = new CorruptJPEGDetector(image3.toPath());
		assertNotNull(cjd.getHexDump());
		assertNotEquals("", cjd.getHexDump());
		System.out.println(cjd.getHexDump());
	}
}
