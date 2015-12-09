// package com.clyde.image;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class for analysing jpeg images
 *
 * @author Clyde M. Velasquez
 * @version 0.1
 * @since December 03, 2015
 */
public class JpegImageAnalysis {
    public static int threshold = 25;
    public static int range = 50;

    // Jpg signature
    // 255 216 255
    private final byte[] JPG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
    // End bytes
    // 255 217
    private final byte[] JPG_END_A = {(byte) 0xFF, (byte) 0xD9};
    // 255 217
    private final byte[] JPG_END_B = {(byte) 0xFF, (byte) 0xD9, (byte) 0xFF, (byte) 0xFF};

    private String fileName = "";
    private boolean isJpg = false;
    private boolean isDistorted = false;
    private boolean isFileComplete = false;
	private int endBytesLength;

    public static void main(String[] args) {
        if (args.length != 0 && !args[0].isEmpty()) {
            long start = System.currentTimeMillis();
            System.out.println("Scanning " + args[0] + "....");
            int ctr = 0;
            File directory = new File(args[0]);

            if (directory.exists()) {
                File[] shuppins = directory.listFiles();
                for (File shuppin : shuppins) {
                    // If only a directory
                    if (shuppin.isDirectory()) {
                        try {
                            File[] images = shuppin.listFiles((dir, name) ->
                                    name.equals("front.jpg") ||
									name.equals("back.jpg") ||
									name.equals("interior.jpg") ||
									name.equals("sheet_plain.jpg") ||
									name.equals("sheet.jpg")
                            );
                            for (File image : images) {
                                JpegImageAnalysis jpg = new JpegImageAnalysis(image.getAbsolutePath());
                                if (jpg.isDistorted) {
                                    System.out.println("Distorted: " + shuppin.getName());
                                    ctr++;
                                    break;
                                }
                            }
                        } catch (IOException | NullPointerException e) {
                            System.err.println(shuppin.getAbsolutePath());
                            e.printStackTrace();
                        }
                    }
                }

            }
            long end = System.currentTimeMillis();
            System.out.println("Time(secs): " + ((double)(end - start)/1000));
            System.out.println("Number of units with distorted images: " + ctr);
        } else {
            System.out.println("Usage: java JpegImageAnalysis /directory/");
			System.out.println();
            System.out.println("Example: ");
            System.out.println("\t\tjava JpegImageAnalysis /usr/local/iDirectStorage/Cache/USS/Tokyo/20151203/");
        }
    }

    public JpegImageAnalysis(String fileName) throws IOException {
        this.fileName = fileName;
        // if (needsTrim())
        //    trimFile();

        setIsJpg();
        setIsFileComplete(JPG_END_A);
        setIsFileComplete(JPG_END_B);
        // setIsDistorted();
        setIsDistorted2();
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
        isJpg = true;
        return true;
    }

    private boolean matchEndBytes(byte[] buffer, byte[] comp) {
        for (int i = 1; i < comp.length; i++) {
            if (buffer[buffer.length - i] != comp[comp.length - i])
                return false;
        }
        isFileComplete = true;
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
    public String getFileName() {
        return fileName;
    }

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
