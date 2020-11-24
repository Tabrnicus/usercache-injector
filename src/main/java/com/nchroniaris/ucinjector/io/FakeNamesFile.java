package com.nchroniaris.ucinjector.io;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FakeNamesFile {

    private final File file;

    public FakeNamesFile(String filePath) {

        if (filePath == null)
            throw new IllegalArgumentException("The file path cannot be null!");

        this.file = new File(filePath);

        if (!this.file.exists())
            throw new IllegalArgumentException("The file specified does not exist!");

        // By "short circuit", we know the file exists already.
        if (!this.file.isFile())
            throw new IllegalArgumentException(String.format("The file path specified (%s) is valid, but it is not a file! Please choose another file path.", file));

        // Check read permissions
        if (!this.file.canRead())
            throw new IllegalArgumentException(String.format("The file path specified (%s) is valid, but does not have correct read permissions! Please use something like `chmod` to change the file permissions.", filePath));

    }

    /**
     * Gets all the names from the file specified in the constructor. It is assumed that this file has one username per line. Any lines that start with a `#` will be ignored.
     * @return A <code>List</code> of <code>String</code>s that represent all the (valid) usernames to be injected.
     */
    public List<String> readNames() {

        List<String> nameList = new ArrayList<>();

        // try-with-resources on a new buffered reader.
        try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {

            // Read all the lines of the file and append them to the nameList array.
            while (true) {

                // Read one line from the file
                String line = reader.readLine();

                // If null, we've reached the end of the file so we break
                if (line == null)
                    break;

                // Trim spaces in front and back in case they were accidentally left in
                line = line.trim();

                // Skip over comments
                if (line.charAt(0) == '#')
                    continue;

                // https://help.minecraft.net/hc/en-us/articles/360034636712-Minecraft-Usernames
                // Usernames are 3-16 characters, alphanumeric, no spaces, with underscores allowed.
                if (!line.matches("[A-Za-z0-9_]{3,16}")) {
                    System.err.printf("[WARNING]: Ignoring username \"%s\" as it does not adhere to the proper username format. Refer to the README for details.%n", line);
                    continue;
                }

                nameList.add(line);

            }

        } catch (IOException e) {

            // TODO: 2020-11-23 Do something useful here
            e.printStackTrace();

        }

        return nameList;

    }

}
