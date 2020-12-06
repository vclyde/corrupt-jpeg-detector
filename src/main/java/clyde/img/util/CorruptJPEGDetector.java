package clyde.img.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple utility class for detecting corrupt JPEG/JPG images based on its
 * byte pattern.
 *
 * Inspiration:
 * https://www.dreamincode.net/forums/topic/286802-detect-partially-corrupted-image/
 *
 * Copyright 2015-present Clyde M. Velasquez
 *
 * @author Clyde M. Velasquez
 * @version 1.0
 * @since 12/03/2015
 */
public class CorruptJPEGDetector {

	private static final int THRESHOLD = 50;

	private final Path jpegImg;
	private boolean isJPEG = false;
	private boolean isCorrupt = false;
	private boolean isFileComplete = false;
	private String hexDump = null;

	/**
	 * Default constructor that accepts a JPEG file Does not ignore extension
	 *
	 * @param jpegPath The JPEG image file
	 * @throws IOException If IOException occurs
	 * @since 1.0
	 */
	public CorruptJPEGDetector(Path jpegPath) throws IOException {
		this(jpegPath, false);
	}

	/**
	 * Constructor that accepts a JPEG file and check file extension if jpg or
	 * jpeg
	 *
	 * @param jpegPath The JPEG image file
	 * @param ignoreExtension If file extension is checked
	 * @throws IOException If IOException occurs
	 * @since 1.0
	 */
	public CorruptJPEGDetector(Path jpegPath, boolean ignoreExtension) throws IOException {
		// File must not be a directory
		if (Files.isDirectory(jpegPath)) {
			throw new IOException("File " + jpegPath.toRealPath() + " is a directory!");
		}

		if (!ignoreExtension) {
			String filename = jpegPath.getFileName().toString();
			if (filename.contains(".")) {
				String ext = filename.substring(filename.lastIndexOf("."));

				if (!(ext.equalsIgnoreCase(".jpeg") || ext.equalsIgnoreCase(".jpg"))) {
					throw new IOException("Not a jpeg extension");
				}
			}
		}

		if (Files.notExists(jpegPath)) {
			throw new FileNotFoundException("File " + jpegPath.toRealPath() + " does not exist!");
		}

		this.jpegImg = jpegPath;
		setIsJpeg();
		setIsFileComplete();
		setIsCorrupt();
	}

	/**
	 * Set isJpeg either true or false when Jpeg SOI marker is present
	 *
	 * @throws IOException If I/O error occurs
	 * @since 1.0
	 */
	private void setIsJpeg() throws IOException {
		int soiSize = JPEGMarker.START_OF_IMAGE.length;

		try (FileChannel fc = FileChannel.open(this.jpegImg, StandardOpenOption.READ)) {
			ByteBuffer bBuffer = ByteBuffer.allocate(soiSize);
			if (fc.size() > soiSize) {
				fc.read(bBuffer, 0);
			}

			this.isJPEG = Arrays.equals(JPEGMarker.START_OF_IMAGE, bBuffer.array());
		}
	}

	/**
	 * Set isFileComplete either true or false when Jpeg EOI marker is present
	 *
	 * @throws IOException If I/O error occurs
	 * @since 1.0
	 */
	private void setIsFileComplete() throws IOException {
		int eoiSize = JPEGMarker.END_OF_IMAGE.length;

		try (FileChannel fc = FileChannel.open(this.jpegImg, StandardOpenOption.READ)) {
			ByteBuffer bBuffer = ByteBuffer.allocate(eoiSize);

			if (fc.size() > eoiSize) {
				fc.read(bBuffer, fc.size() - eoiSize);
			}

			this.isFileComplete = Arrays.equals(JPEGMarker.END_OF_IMAGE, bBuffer.array());
		}
	}

	/**
	 * Set isCorrupt if distorted pattern matches with end bytes
	 *
	 * @throws IOException If I/O error occurs
	 * @since 1.0
	 */
	private void setIsCorrupt() throws IOException {
		// If file is not complete then it is considered automatically corrupt
		if (!this.isFileComplete) {
			this.isCorrupt = true;
			return;
		}

		try (FileChannel fc = FileChannel.open(this.jpegImg, StandardOpenOption.READ)) {
			ByteBuffer bBuffer = ByteBuffer.allocate(THRESHOLD);

			if (fc.size() > (THRESHOLD + JPEGMarker.END_OF_IMAGE.length)) {
				fc.read(bBuffer, fc.size() - (THRESHOLD + JPEGMarker.END_OF_IMAGE.length));
			}

			// Concatenate string
			StringJoiner joiner = new StringJoiner("");
			for (byte b : bBuffer.array()) {
				joiner.add(Integer.toHexString(b));
			}
			String fullStringRep = joiner.toString().toUpperCase();

			int first = bBuffer.array()[0];
			StringBuilder stringPattern = new StringBuilder();
			for (int i = 1; i < bBuffer.array().length - JPEGMarker.END_OF_IMAGE.length; i++) {
				stringPattern.append(Integer.toHexString(bBuffer.get(i)).toUpperCase());
				if (first == bBuffer.get(i)) {
					break;
				}
			}

			Pattern pattern = Pattern.compile(stringPattern.toString());
			Matcher matcher = pattern.matcher(fullStringRep);

			int matchCount = 0;
			while (matcher.find()) {
				matchCount++;
			}

			if (matchCount > 2) {
				isCorrupt = true;
			}
		}
	}

	/**
	 * Displays the Hex dump of the Jpeg image
	 *
	 * @return The hex dump
	 */
	public String getHexDump() {
		
		if (hexDump != null) {
			return hexDump;
		}
		
		StringBuilder sb = new StringBuilder();

		try {
			byte[] b = Files.readAllBytes(jpegImg);
			int i = 0;
			for (int pos = 0; pos < b.length; pos++) {
				String hex = Integer.toHexString(b[pos] < 0 ? (256 + b[pos]) : b[pos]).toUpperCase();
				sb.append(hex.length() == 1 ? ("0" + hex) : hex).append(" ");
				i++;
				if (i == 16) {
					i = 0;
					sb.append("\n");
				}
			}
			hexDump = sb.toString();
		} catch (IOException ex) {
			Logger.getLogger(CorruptJPEGDetector.class.getName()).log(Level.SEVERE, null, ex);
		}

		return hexDump;
	}

	/**
	 * Getter for isJpeg
	 *
	 * @return true if Jpeg otherwise false
	 * @since 1.0
	 */
	public boolean isJPEG() {
		return isJPEG;
	}

	/**
	 * Getter for isFileComplete
	 *
	 * @return true if EOI marker is found
	 * @since 1.0
	 */
	public boolean isFileComplete() {
		return isFileComplete;
	}

	/**
	 * Getter for isCorrupt
	 *
	 * @return true if image is corrupt otherwise false
	 * @since 1.0
	 */
	public boolean isCorrupt() {
		return isCorrupt;
	}
}
