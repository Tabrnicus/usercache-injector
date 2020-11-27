package com.nchroniaris.ucinjector;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final String USAGE_INFORMATION = "java -jar usercache-injector.jar [-h|--help] [-c|--check-usernames] <usercache.json> [fake_name_list]";

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

        // This includes the arguments from `args`, AFTER single arguments have been factored out. The reason we have a List<String> is because primitive lists aren't mutable wrt to elements.
        List<String> augmentedArgs = new ArrayList<>();

        Injector.InjectorProperties properties = new Injector.InjectorProperties();

        // If ANY of the arguments (from the original parameter list) are the single arguments (does not require a parameter), the appropriate action is taken or the appropriate flag is set.
        for (String arg : args) {

            switch (arg) {

                // We print the help text and exit the program if the help flag is ANYWHERE in the argument list
                case "-h":
                case "--help":
                    printHelpAndExit();
                    break;

                // Checking usernames with Mojang is disabled by default, so we enable it in the properties if this occurs anywhere in the list
                case "-c":
                case "--check-usernames":
                    properties.checkUsernames = true;
                    break;

                // Default case is to add the arguments to the augmented arg list
                default:
                    augmentedArgs.add(arg);

            }

        }

        // After all the single flags have been removed (keep in mind there are no parametrized args atm), the size of the augmented args should be the file paths. If this is empty, for example when you run `usercache-injector.jar -c`, then we know that a path hasn't been specified for the first argument.
        if (augmentedArgs.size() < 1)
            printUsageAndExit("Please provide a path to usercache.json.");

        Injector injector = null;

        // If the args is exactly 1 then this is the default case, if not, we take the first two args and ignore the rest.
        if (augmentedArgs.size() == 1)
            injector = new Injector(properties, augmentedArgs.get(0));
        else if (augmentedArgs.size() >= 2)
            injector = new Injector(properties, augmentedArgs.get(0), augmentedArgs.get(1));

        // Null check to make the compiler happy
        // Run program with the arguments in the constructor
        if (injector != null)
            injector.inject();

    }

}
