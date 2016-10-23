package com.clyde.util;

import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;

import static org.junit.Assert.*;

/**
 * Unit test for CorruptJPEGDetector class
 *
 * @author Clyde M. Velasquez
 * @version 1.0
 * @since 10/24/16.
 */
public class CorruptJPEGDetectorTest {
    private static final File ROOT_DIR = new File("").getAbsoluteFile();

    @Test
    public void isJPEG() throws Exception {
        File resources = new File(ROOT_DIR, "src/main/resources/");
        assertTrue(resources.exists());

        File[] testFiles = resources.listFiles((dir, name) -> !name.contains(".png") && !name.contains(".gif"));
        CorruptJPEGDetector cjd;
        for (File jpg : testFiles) {
            cjd = new CorruptJPEGDetector(jpg);
            assertTrue(cjd.isJPEG());
        }

        File png = new File(resources, "image6.png");
        cjd = new CorruptJPEGDetector(png, true);
        assertFalse(cjd.isJPEG());
    }

    @Test
    public void isFileComplete() throws Exception {
        File resources = new File(ROOT_DIR, "src/main/resources/");

        File image1 = new File(resources, "image1.jpg");
        CorruptJPEGDetector cjd = new CorruptJPEGDetector(image1);
        assertFalse(cjd.isFileComplete());

        File image2 = new File(resources, "image2.jpg");
        cjd = new CorruptJPEGDetector(image2);
        assertTrue(cjd.isFileComplete());

        File image3 = new File(resources, "image3.jpg");
        cjd = new CorruptJPEGDetector(image3);
        assertTrue(cjd.isFileComplete());
    }

    @Test
    public void isCorrupt() throws Exception {
        File resources = new File(ROOT_DIR, "src/main/resources/");

        File image1 = new File(resources, "image1.jpg");
        CorruptJPEGDetector cjd = new CorruptJPEGDetector(image1);
        assertTrue(cjd.isCorrupt());

        File image2 = new File(resources, "image2.jpg");
        cjd = new CorruptJPEGDetector(image2);
        assertTrue(cjd.isCorrupt());

        File image3 = new File(resources, "image3.jpg");
        cjd = new CorruptJPEGDetector(image3);
        assertFalse(cjd.isCorrupt());

        File image4 = new File(resources, "image4.jpg");
        cjd = new CorruptJPEGDetector(image4);
        assertTrue(cjd.isCorrupt());

        File image5 = new File(resources, "image5.jpg");
        cjd = new CorruptJPEGDetector(image5);
        assertTrue(cjd.isCorrupt());
    }

    @Test
    public void printHexDump() throws Exception {
        File resources = new File(ROOT_DIR, "src/main/resources");
        File image3 = new File(resources, "image3.jpg");
        CorruptJPEGDetector cjd = new CorruptJPEGDetector(image3);
        cjd.printHexDump();
    }
}