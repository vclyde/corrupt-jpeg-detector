package com.clydeev.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple utility class for detecting corrupt JPEG/JPG images based on its
 * byte pattern.
 *
 * Copyright 2015-present Clyde M. Velasquez
 *
 * @author Clyde M. Velasquez
 * @version 0.3
 * @since 12/03/2015
 */
public class CorruptJPEGDetector {

	private static final int THRESHOLD = 50;

	private File jpegFile;
	private boolean isJPEG = false;
	private boolean isCorrupt = false;
	private boolean isFileComplete = false;
	private String hexDump = null;

	/**
	 * Default constructor that accepts a JPEG file
	 *
	 * @param jpegFile The JPEG image file
	 * @throws IOException If IOException occurs
	 * @since 0.1
	 */
	public CorruptJPEGDetector(File jpegFile) throws IOException {
		this(jpegFile, false);
	}

	/**
	 * Constructor that accepts a JPEG file and check file extension if jpg or
	 * jpeg
	 *
	 * @param jpegFile The JPEG image file
	 * @param ignoreExtension If file extension is checked
	 * @throws IOException If IOException occurs
	 * @since 0.1
	 */
	public CorruptJPEGDetector(File jpegFile, boolean ignoreExtension) throws IOException {
		// File must not be a directory
		if (jpegFile.isDirectory()) {
			throw new IOException("File " + jpegFile.getCanonicalPath() + " is a directory!");
		}

		if (!ignoreExtension) {
			if (jpegFile.getName().contains(".")) {
				String ext = jpegFile.getName().substring(jpegFile.getName().lastIndexOf("."));

				if (!(ext.equalsIgnoreCase(".jpeg") || ext.equalsIgnoreCase(".jpg"))) {
					throw new IOException("Not a jpeg extension");
				}
			}
		}

		if (!jpegFile.exists()) {
			throw new FileNotFoundException("File " + jpegFile.getCanonicalPath() + " is not found!");
		}

		this.jpegFile = jpegFile;
		setIsJpeg();
		setIsFileComplete();
		setIsCorrupt();
	}

	/**
	 * Set isJpeg either true or false when Jpeg SOI marker is present
	 *
	 * @throws IOException If I/O error occurs
	 * @since 0.1
	 */
	private void setIsJpeg() throws IOException {
		int size = JPEGMarker.START_OF_IMAGE.length;
		byte[] buffer = new byte[size];

		try (RandomAccessFile file = new RandomAccessFile(this.jpegFile, "r")) {
			if (file.length() > size) {
				file.read(buffer, 0, size);
			} else {
				file.read(buffer, 0, (int) file.length());
			}
		}
		this.isJPEG = matchStartBytes(buffer);
	}

	/**
	 * Set isFileComplete either true or false when Jpeg EOI marker is present
	 *
	 * @throws IOException If I/O error occurs
	 * @since 0.1
	 */
	private void setIsFileComplete() throws IOException {
		int size = JPEGMarker.END_OF_IMAGE.length;
		byte[] buffer = new byte[size];
		try (RandomAccessFile file = new RandomAccessFile(this.jpegFile, "r")) {
			if (file.length() > size) {
				// Set the file pointer to the last value position minus the length of endBits
				file.seek((int) file.length() - size);
				file.read(buffer, 0, size);
			} else {
				file.read(buffer, 0, (int) file.length());
			}
		}
		this.isFileComplete = matchEndBytes(buffer);
	}

	/**
	 * Set isCorrupt if distorted pattern matches with end bytes
	 *
	 * @throws IOException If I/O error occurs
	 * @since 0.1
	 */
	private void setIsCorrupt() throws IOException {
		// If file is not complete then it is considered automatically corrupt
		if (!this.isFileComplete) {
			this.isCorrupt = true;
			return;
		}

		// Get the end bytes
		byte[] buffer = new byte[THRESHOLD];
		try (RandomAccessFile file = new RandomAccessFile(this.jpegFile, "r")) {
			if (file.length() > (THRESHOLD + JPEGMarker.END_OF_IMAGE.length)) {
				file.seek(file.length() - (THRESHOLD + JPEGMarker.END_OF_IMAGE.length));
				file.read(buffer, 0, buffer.length);
			} else {
				file.read(buffer, 0, (int) file.length());
			}

			// Concatenate string
			StringJoiner joiner = new StringJoiner("");
			for (byte b : buffer) {
				joiner.add(Integer.toHexString(b));
			}
			String fullStringRep = joiner.toString().toUpperCase();

			int first = buffer[0];
			StringBuilder stringPattern = new StringBuilder();
			for (int i = 1; i < buffer.length - JPEGMarker.END_OF_IMAGE.length; i++) {
				stringPattern.append(Integer.toHexString(buffer[i]).toUpperCase());
				if (first == buffer[i]) {
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
	 * Compare bytes
	 *
	 * @param buffer Byte array to check
	 * @return true if match otherwise false
	 * @since 0.1
	 */
	private boolean matchStartBytes(byte[] buffer) {
		for (int i = 0; i < JPEGMarker.START_OF_IMAGE.length; i++) {
			if (buffer[i] != JPEGMarker.START_OF_IMAGE[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compare end bytes
	 *
	 * @param buffer Byte array to check
	 * @return true if match otherwise false
	 * @since 0.1
	 */
	private boolean matchEndBytes(byte[] buffer) {
		for (int i = 1; i < JPEGMarker.END_OF_IMAGE.length; i++) {
			if (buffer[buffer.length - i] != JPEGMarker.END_OF_IMAGE[JPEGMarker.END_OF_IMAGE.length - i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Displays the Hex dump of the Jpeg image
	 *
	 * @return The hex dump
	 */
	public String getHexDump() {
		StringBuilder sb = new StringBuilder();
		try (RandomAccessFile file = new RandomAccessFile(jpegFile, "r")) {
			int i = 0;

			for (int pos = 0; pos < file.length(); pos++) {
				String hex = Integer.toHexString(file.read()).toUpperCase();
				sb.append(hex.length() == 1 ? ("0" + hex) : hex).append(" ");
				i++;

				if (i == 16) {
					i = 0;
					sb.append("\n");
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(CorruptJPEGDetector.class.getName()).log(Level.SEVERE, null, ex);
		}

		if (hexDump == null) {
			hexDump = sb.toString();
		}

		return hexDump;
	}

	/**
	 * Getter for isJpeg
	 *
	 * @return true if Jpeg otherwise false
	 * @since 0.1
	 */
	public boolean isJPEG() {
		return isJPEG;
	}

	/**
	 * Getter for isFileComplete
	 *
	 * @return true if EOI marker is found
	 * @since 0.1
	 */
	public boolean isFileComplete() {
		return isFileComplete;
	}

	/**
	 * Getter for isCorrupt
	 *
	 * @return true if image is corrupt otherwise false
	 * @since 0.1
	 */
	public boolean isCorrupt() {
		return isCorrupt;
	}
}
