package com.vclyde.cjd;

/**
 * Contains different JPEG Markers
 *
 * @author Clyde M. Velasquez
 * @version 1.0
 * @since 9/23/2016
 * @deprecated 
 * Not effective in detecting all corrupted jpeg images. 
 * Use https://github.com/vclyde/BadPics instead.
 */
@Deprecated
public final class JPEGMarker {

	public static final byte[] START_OF_IMAGE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}; // SOI 255 216 255
	public static final byte[] END_OF_IMAGE = {(byte) 0xFF, (byte) 0xD9}; // EOI 255 217
}
