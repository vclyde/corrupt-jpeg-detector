package com.clyde.image;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class for detecting corrupt JPEG image
 *
 * @author Clyde M. Velasquez
 * @version 0.2
 * @since December 03, 2015
 */
public class JpegImageFile {
	// Jpeg signature or markers
    // 255 216 255
    public static final byte[] START_OF_IMAGE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
    // 255 217
    public static final byte[] END_OF_IMAGE = {(byte) 0xFF, (byte) 0xD9};

    private File jpegFile;
    private boolean isJpeg;
    private boolean isCorrupt;
    private boolean isFileComplete;
	private int endBytesLength;

	public JpegImageFile(File jpegFile) {
		this.jpegFile = file;
		this.isJpeg = false;
		this.isCorrupt = false;
		this.isFileComplet = false;
		this.endBytesLength = 0;
	}

    private void setIsJpg() throws IOException {
        if (new File(this.fileName).exists()) {
            byte[] buffer = new byte[20];

            try (RandomAccessFile file = new RandomAccessFile(this.fileName, "r")) {
                if (file.length() > 20)
                    file.read(buffer, 0, 20);
                else
                    file.read(buffer, 0, (int) file.length());
            }
            matchBytes(buffer, JPG_SIGNATURE);
        }
    }

    private void setIsFileComplete(byte[] endBits) throws IOException {
        if (new File(this.fileName).exists()) {
            byte[] buffer = new byte[endBits.length];
            try (RandomAccessFile file = new RandomAccessFile(this.fileName, "r")) {
                if (file.length() > endBits.length) {
                    // Set the file pointer to the last value position minus the length of endBits
                    file.seek((int) file.length() - endBits.length);
                    file.read(buffer, 0, endBits.length);
                } else {
                    file.read(buffer, 0, (int) file.length());
                }
                matchEndBytes(buffer, endBits);
            }
        }
    }

    private void setIsDistorted() throws IOException {
        if (new File(this.fileName).exists()) {
            try (RandomAccessFile file = new RandomAccessFile(this.fileName, "r")) {
                int ctr = 0;
                int temp = 0;
                int length = (int) (file.length() > range ? (file.length() - range) : 0);
                for (int i = (int) file.length() - 1; i > length; i--) {
                    file.seek(i);
                    int temp2 = file.read();

                    if (temp == temp2) {
                        ctr++;
                    }
                    temp = temp2;
                }
                file.close();
                if (ctr > threshold) {
                    isDistorted = true;
                }
            }
        }
    }

    private void setIsDistorted2() throws IOException {
        if (new File(this.fileName).exists()) {
            int ctr = 0;
            int temp1;
            int temp2;
			int line = 3;
			int numBytes = 16;
            try (RandomAccessFile file = new RandomAccessFile(fileName, "r")) {
				int start = (int) file.length() - 1 - endBytesLength;
				int length = start - (line * numBytes);
                for (int i = start; i > length; i-=2) {
                    file.seek(i);
                    temp1 = file.read();
                    file.seek(i-1);
                    temp2 = file.read();
                    if (temp1 == temp2) {
                        ctr++;
                    }
                }
                file.close();

                if (ctr >= 16)
                    this.isDistorted = true;
            }
        }
    }

    private boolean matchBytes(byte[] buffer, byte[] comp) {
        for (int i = 0; i < comp.length; i++) {
            if (buffer[i] != comp[i])
                return false;
        }
    	
        return true;
    }

    private boolean matchEndBytes(byte[] buffer, byte[] comp) {
        for (int i = 1; i < comp.length; i++) {
            if (buffer[buffer.length - i] != comp[comp.length - i])
                return false;
        }
    
		endBytesLength = comp.length;

        return true;
    }

    private boolean needsTrim() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(this.fileName, "r")) {
            if (file.length() > 0) {
                file.seek(file.length() - 1);
                byte b = file.readByte();
                return b == 0;
            }
        }

        return false;
    }

    private void trimFile() throws IOException {
        byte[] bufferIn;
        try (RandomAccessFile file = new RandomAccessFile(this.fileName, "r")) {
            bufferIn = new byte[(int) file.length()];
            file.read(bufferIn, 0, (int) file.length());
        }

        int index = findFirstNull(bufferIn);
        if (index < 0)
            return;

        byte[] bufferOut = new byte[index];
        System.arraycopy(bufferIn, 0, bufferOut, 0, index);
        try (RandomAccessFile file = new RandomAccessFile(this.fileName, "rw")) {
            for(byte b : bufferOut) {
                file.writeByte(b);
            }
        }
    }

    private int findFirstNull(byte[] buffer) {
        for (int i = buffer.length - 1; i > 0; i--) {
            if (buffer[i] != 0)
                return i + 1;
        }
        return -1;
    }

    // Getters
    public boolean isJpg() {
        return isJpg;
    }

    public boolean isDistorted() {
        return isDistorted;
    }

    public boolean isFileComplete() {
        return isFileComplete;
    }
}
