package org.puhavik;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


//
//import net.lingala.zip4j.ZipFile;
//import net.lingala.zip4j.exception.ZipException;
//
//class ZipBruteforce {
//    private static final String DIGITS = "0123456789";
//    private static final int MAX_LENGTH = 10;
//
//    public static void main(String[] args) {
//        String zipFilePath = "/Users/vikpuhaev/Downloads/Document.zip"; // Path to the zip file
//        bruteforce(zipFilePath);
//    }
//
//    public static void bruteforce(String zipFilePath) {
//        long max = (long) Math.pow(10, MAX_LENGTH); // 10^10
//
//        for (long i = 0; i < max; i++) {
//            String current = String.format("%010d", i); // Formats the number with leading zeroes
//            System.out.println(current);
//            if (tryPassword(zipFilePath, current)) {
//                System.out.println("Password found: " + current);
//                System.exit(0);
//            }
//        }
//    }
//
//    public static boolean tryPassword(String zipFilePath, String password) {
//        try {
//            ZipFile zipFile = new ZipFile(zipFilePath);
//            if (zipFile.isEncrypted()) {
//                zipFile.setPassword(password.toCharArray());
//            }
//            zipFile.extractAll("/Users/vikpuhaev/Downloads/"); // Specify your extraction path
//            return true; // Password is correct
//        } catch (ZipException e) {
//            return false; // Password is incorrect
//        }
//    }
//}


class PasswordCracker implements Runnable {
    private long startRange;
    private long endRange;
    private String zipFilePath;
    private static AtomicBoolean found = new AtomicBoolean(false);

    public PasswordCracker(long startRange, long endRange, String zipFilePath) {
        this.startRange = startRange;
        this.endRange = endRange;
        this.zipFilePath = zipFilePath;
    }

    @Override
    public void run() {
        for (long i = startRange; i <= endRange && !found.get(); i++) {
            String current = String.format("%010d", i); // Fill gaps with 0
            System.out.println("Trying password: " + current); // Print every password guess
            if (tryPassword(zipFilePath, current)) {
                long endTime = System.currentTimeMillis();
                System.out.println("Time taken: " + (endTime - ZipBruteforce.startTime) + " ms");
                System.out.println("Password found: " + current);
                found.set(true);
                return;
            }
        }
    }

    private boolean tryPassword(String zipFilePath, String password) {
        try {
            ZipFile zipFile = new ZipFile(zipFilePath);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password.toCharArray());
            }
            zipFile.extractAll("/Users/vikpuhaev/Downloads/"); // Specify your extraction path
            return true; // Password is correct
        } catch (ZipException e) {
            return false; // Password is incorrect
        }
    }
}

class ZipBruteforce {
    public static long startTime;

    public static void main(String[] args) {
        startTime = System.currentTimeMillis();
        // Path to your encrypted ZIP
        String zipFilePath = "/Users/vikpuhaev/Downloads/Document.zip";

        if (!doesFileExist(zipFilePath)) {
            System.out.println("The file does not exist at the specified path: " + zipFilePath);
            return; // Exit the program if the file doesn't exist
        }

        int numberOfThreads = Runtime.getRuntime().availableProcessors(); //Get all available Threads
        System.out.println("Available Threads: " + numberOfThreads);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        long max = (long) Math.pow(10, 10); //Number of digits in the password
        long range = max / numberOfThreads;

        for (int i = 0; i < numberOfThreads; i++) {
            long startRange = i * range;
            long endRange = (i + 1) * range - 1;
            executor.execute(new PasswordCracker(startRange, endRange, zipFilePath));
        }
        executor.shutdown();
        // Wait for all threads to finish if necessary
    }

    private static boolean doesFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists() && !file.isDirectory();
    }
}
