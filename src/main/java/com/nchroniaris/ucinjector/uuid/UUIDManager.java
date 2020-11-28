package com.nchroniaris.ucinjector.uuid;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

/**
 * This class is responsible for generating UUIDs, and checking both usernames and UUIDs for existence using Mojang's REST API. You cannot instantiate this class.
 */
public class UUIDManager {

    // 5 second timeout for all REST calls
    private static final int TIMEOUT_MS = 5000;

    // Disable construction
    private UUIDManager() {
    }

    /**
     * This is a "generic" (not Java generics) method that sends a GET request to a URL and ONLY obtains its status code. That status code is analyzed and if it's OK (200) then this returns true. If it's NO_CONTENT (204), it returns false. If it gets any other status code or encounters some error along the way it will print to stderr and also return false.
     *
     * @param urlString A valid URL as a string. This method will make a GET request to it.
     * @return true iff URL status code is OK (200), false otherwise.
     */
    private static boolean httpGetRequest(String urlString) {

        HttpsURLConnection connection = null;

        try {

            // Create a URL and connection
            URL url = new URL(urlString);
            connection = (HttpsURLConnection) url.openConnection();

            // Set connection parameters. This is always a GET call, for both UUID and username (just different URL)
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(UUIDManager.TIMEOUT_MS);
            connection.setReadTimeout(UUIDManager.TIMEOUT_MS);

            // Get status code from the GET call
            int statusCode = connection.getResponseCode();

            // Depending on the status code we can determine if a user exists. i.e. we don't need to analyze the actual content, just the response code. Mojang's API will return a 204 if the user/UUID doesn't exist.
            if (statusCode == HttpsURLConnection.HTTP_OK)
                return true;
            else if (statusCode == HttpsURLConnection.HTTP_NO_CONTENT)
                return false;
            else
                System.err.printf("[WARNING] There was an error checking for UUID/Username existence! HTTP Status code: (%d). The program will continue but note that you are not guaranteed to have a unique UUID/username!", statusCode);

        } catch (MalformedURLException e) {

            System.err.printf("[WARNING] The url passed in (%s) is malformed! The program will continue but note that you are not guaranteed to have a unique UUID/username!%n", urlString);

        } catch (IOException e) {

            System.err.println("[WARNING] There was some sort of issue connecting to the server! The program will continue but note that you are not guaranteed to have a unique UUID/username!");
            e.printStackTrace();

        } finally {

            if (connection != null)
                connection.disconnect();

        }

        // If any of the catch clauses get triggered we return false. Assuming that the HTTP calls never work, this **should** be fine most of the time, but there is an incredibly slim chance that we will generate a UUID that actually already exists, which can cause some problems.
        return false;

    }

    /**
     * Checks if a Minecraft UUID belongs to a real player
     *
     * @param uuid A Minecraft UUID, can be stylized (with dashes) or not
     * @return true if the UUID belongs to a real player, false otherwise
     */
    public static boolean uuidExists(String uuid) {

        // https://wiki.vg/Mojang_API#UUID_-.3E_Name_history
        return UUIDManager.httpGetRequest(String.format("https://api.mojang.com/user/profiles/%s/names", uuid));

    }

    /**
     * Checks if a Minecraft username belongs to a real player
     *
     * @param username A Minecraft username. Formatting is not checked in this method
     * @return true if the username belongs to a real player, false otherwise
     */
    public static boolean usernameExists(String username) {

        // https://wiki.vg/Mojang_API#Username_-.3E_UUID_at_time
        return UUIDManager.httpGetRequest(String.format("https://api.mojang.com/users/profiles/minecraft/%s?", username));

    }

    /**
     * Generates a random UUID based on the usual format. Generated UUIDs should be checked for conflicts with real players using the other methods in this class but is not strictly required. The generation space is so large, [0, 2^128 -1] (!) that it's unlikely (but not impossible!) to have a conflict.
     *
     * @return A random, stylized UUID string. Likely (not guaranteed) to be fake (no real player has this UUID).
     */
    public static String generateUUID() {

        // According to the docs this randomly generates a BigInteger in the range [0, 2^128 - 1] uniformly. The reason that we need such a big number is because Minecraft UUIDs are 32-bit hex numbers. That means that the maximum UUID range is `16^32 - 1`, which equals `2^128 - 1`.
        // It's important to note that we convert this number to a HEXADECIMAL string (note the argument of 16 in the .toString() -- this is the radix).
        String unpaddedHex = new BigInteger(128, new Random()).toString(16);

        // We potentially need to pad this new string so a StringBuilder is the best choice here, as far as I can tell. There might be a cleaner way to accomplish this, but I didn't want to take any risks with respect to String.format() interpretation of BigIntegers so I chose to do it the "manual" way.
        StringBuilder uuidString = new StringBuilder();

        // This for loop runs for n iterations, where n is the number of left-padded 0s to make the length of this string 32.
        for (int i = 0; i < 32 - unpaddedHex.length(); i++)
            uuidString.append('0');

        // After padding, add the hex part of the string. At this point the length is guaranteed to be a length of 32.
        uuidString.append(unpaddedHex);

        // UUIDs are stylized in this format (8-4-4-4-12) [in terms of number of chars] so we slice the string according to that. We return the stylized uuid string.
        return String.format(
                "%s-%s-%s-%s-%s",
                uuidString.substring(0, 8),
                uuidString.substring(8, 12),
                uuidString.substring(12, 16),
                uuidString.substring(16, 20),
                uuidString.substring(20)
        );

    }

}
