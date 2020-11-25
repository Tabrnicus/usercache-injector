package com.nchroniaris.ucinjector;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Main {

    public static final String USAGE_INFORMATION = "java -jar usercache-injector.jar <usercache.json> [fake_name_list]";

    /**
     * Uses the location of the Main class as a reference to obtain the path to the directory that encloses the jar file being run. We must decode the path as a URL as the presence of any spaces in the path will result in a `%20` instead of an actual space.
     *
     * @return The full path (spaces are unescaped) of the directory that the .jar resides in.
     */
    public static String findJarWorkingDir() {

        // Adapted from: https://stackoverflow.com/questions/40317459/how-to-open-a-file-in-the-same-directory-as-the-jar-file-of-the-application

        // Get the location of the jar file, and then represent it as a file to get its parent.
        String dir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();

        // Try to decode the URL-esque string we get above. This is to remove spaces and other special URL characters.
        try {

            dir = URLDecoder.decode(dir, "UTF-8");

        } catch (UnsupportedEncodingException e) {

            System.err.println("[CRITICAL] Something went wrong with finding the location of the .jar file: " + e.getMessage());
            System.exit(1);

        }

        return dir;

    }

    /**
     * Prints the <code>Main.USAGE_INFORMATION</code> string and exits the program with a non zero error code
     */
    private static void printUsageAndExit() {

        System.err.printf("Usage: %s%n", Main.USAGE_INFORMATION);
        System.exit(1);

    }

    /**
     * Prints the error message passed in the argument, the <code>Main.USAGE_INFORMATION</code> string, and exits the program with a non zero error code
     */
    private static void printUsageAndExit(String errorMessage) {

        System.err.println(errorMessage);
        Main.printUsageAndExit();

    }

    /**
     * Prints help text and exits gracefully.
     */
    private static void printHelpAndExit() {

        System.out.println("This is a program that modifies the usercache.json of a Minecraft dedicated server in order to allow unregistered usernames to join the server.");
        System.out.println("This program takes as input your usercache.json file (first argument) and *optionally* a fake usernames file (plaintext, second argument). Not specifying the second argument will use the default path (next to the jar file).");
        System.out.println();
        System.out.println(Main.USAGE_INFORMATION);

        System.exit(0);

    }

    public static void main(String[] args) {

        if (args.length < 1)
            printUsageAndExit("Please provide a path to usercache.json.");

        // If ANY of the arguments are the help flag then print the help text and exit
        for (String arg : args)
            if (arg.toLowerCase().equals("-h") || arg.toLowerCase().equals("--help"))
                printHelpAndExit();

        Injector injector = null;

        // If the args is exactly 1 then this is the default case, if not, we take the first two args and ignore the rest.
        if (args.length == 1)
            injector = new Injector(args[0]);
        else if (args.length >= 2)
            injector = new Injector(args[0], args[1]);

        // Null check to make the compiler happy
        // Run program with the arguments in the constructor
        if (injector != null)
            injector.inject();

    }

}
