package com.nchroniaris.ucinjector.io;

import java.io.*;

/**
 * This class deals with the input and output of the "usercache.json" file. In particular, the program requires to be able to read and rewrite the file.
 */
public class UsercacheFile {

    private static final String EXPECTED_FILENAME = "usercache.json";

    private final File file;

    public UsercacheFile(String filePath) {

        if (filePath == null)
            throw new IllegalArgumentException("The file path cannot be null!");

        this.file = new File(filePath);

        if (!this.file.exists())
            throw new IllegalArgumentException("The file specified does not exist!");

        // Let the user know if they have potentially specified the wrong file.
        if (!this.file.getName().equals(UsercacheFile.EXPECTED_FILENAME))
            System.err.printf("[WARNING]: The usercache file \"%s\" passed in is expected to be named \"%s\". If you're getting this error it usually means that you have specified the wrong file. The program will continue regardless.%n", this.file.getName(), UsercacheFile.EXPECTED_FILENAME);

        // By "short circuit", we know the file exists already.
        if (!this.file.isFile())
            throw new IllegalArgumentException(String.format("The file path specified (%s) is valid, but it is not a file! Please choose another file path.", filePath));

        // Check read permissions
        if (!this.file.canRead())
            throw new IllegalArgumentException(String.format("The file path specified (%s) is valid, but does not have correct read permissions! Please use something like `chmod` to change the file permissions.", filePath));

        // Check if the file is writable
        if (!this.file.canWrite())
            throw new IllegalArgumentException(String.format("The file path specified (%s) is valid, but does not have correct write permissions! Please use something like `chmod` to change the file permissions.", filePath));


    }

    /**
     * This method reads the provided usercache.json file and appends all the lines to one string. This is done for safety, but it should always be the case that usercache.json is always one line.
     *
     * @return A <code>String</code> of the entire file. This will almost always be a JSON-parsable string (unless you give it some random file).
     */
    public String readData() {

        // For safety, we'll add all the lines of the file into one string, but
        StringBuilder builder = new StringBuilder();

        // try-with-resources on a new buffered reader.
        try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {

            // Read all the lines of the file and add to the string.
            while (true) {

                // Read one line from the file
                String line = reader.readLine();

                // If null, we've reached the end of the file so we break
                if (line == null)
                    break;

                builder.append(line);

            }

        } catch (IOException e) {

            // TODO: 2020-11-23 Do something useful here
            e.printStackTrace();

        }

        return builder.toString();

    }

    /**
     * This method **overwrites**, (no append) the file specified by <code>this.file</code> with the contents from string passed into the method.
     *
     * @param json The text to overwrite, serialized as a JSON array.
     */
    public void overwriteFile(String json) {

        // Open the file for overwriting (note the second parameter: false)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file, false))) {

            // Write the serialized json string back to the file
            writer.write(json);

        } catch (IOException e) {

            // TODO: 2020-11-23 Do something useful here
            e.printStackTrace();

        }

    }

}
