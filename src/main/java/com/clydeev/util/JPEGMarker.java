package com.clydeev.util;

/**
 * Contains different JPEG Markers
 *
 * @author Clyde M. Velasquez
 * @version 0.1
 * @since 9/23/2016
 */
public final class JPEGMarker {

	public static final byte[] START_OF_IMAGE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}; // SOI 255 216 255
	public static final byte[] END_OF_IMAGE = {(byte) 0xFF, (byte) 0xD9}; // EOI 255 217
}
