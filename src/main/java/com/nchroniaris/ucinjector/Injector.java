package com.nchroniaris.ucinjector;

import com.google.gson.Gson;
import com.nchroniaris.ucinjector.io.FakeNamesFile;
import com.nchroniaris.ucinjector.io.UsercacheFile;
import com.nchroniaris.ucinjector.model.User;
import com.nchroniaris.ucinjector.uuid.UUIDManager;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is the main driver class for the program. It's responsible for deserializing the JSON string in <code>usercache.json</code>, editing it as required, and then writing it back to the file.
 */
public class Injector {

    // The date format found in usercache.json
    private static final DateTimeFormatter FORMAT_EXPIRY = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xx");

    private String pathUsercache;
    private String pathFakeNames;

    private final InjectorProperties properties;

    /**
     * This is the main properties holder for this class. This makes adding more boolean arguments way easier. All the values are set to default upon instantiation
     */
    public static class InjectorProperties {

        public boolean checkUsernames = false;

        public InjectorProperties() {
        }

    }

    /**
     * Constructs an instance of Injector. Use this when you have a custom path for the fake name list.
     * @param pathUsercache The path to the <code>usercache.json</code> file.
     * @param pathFakeNames The path to the file that holds all the usernames you wish to inject.
     */
    public Injector(InjectorProperties properties, String pathUsercache, String pathFakeNames) {

        this(properties, pathUsercache);

        if (pathFakeNames == null)
            throw new IllegalArgumentException("The argument pathFakeNames cannot be null!");

        this.pathFakeNames = pathFakeNames;

    }

    /**
     * Constructs an instance of Injector. Use this when you want to use the default path for the fake name list.
     * @param pathUsercache The path to the <code>usercache.json</code> file.
     */
    public Injector(InjectorProperties properties, String pathUsercache) {

        if (pathUsercache == null)
            throw new IllegalArgumentException("The argument pathUsercache cannot be null!");

        if (properties == null)
            throw new IllegalArgumentException("The properties argument cannot be null! To specify default values, instantiate an InjectorProperties and pass it in.");

        this.properties = properties;

        this.pathUsercache = pathUsercache;
        this.pathFakeNames = null;

    }

    /**
     * Main driver method. In summary, it reads both files, and updates the model of the JSON object, then writes it back to the file.
     */
    public void inject() {

        // Create appropriate objects for each file. If there is an error with the files they will be propagated here, so this serves as a double check before we start doing anything.
        UsercacheFile usercacheFile = new UsercacheFile(this.pathUsercache);
        FakeNamesFile fakeNamesFile = new FakeNamesFile(this.pathFakeNames);

        // Get the serialized JSON string from the usercache and get the list of the fake names from the fake names file.
        String jsonString = usercacheFile.readData();
        List<String> fakeNames = fakeNamesFile.readNames();

        Gson gson = new Gson();

        // The reason why we use User[].class instead of a more high level list implementation is only for the fact that it more readable to do so. If I were to use a List<> in the classOfT argument I would instead have to specify a java.lang.reflect.Type. It's just easier to convert a primitive array to a List for modification.
        List<User> userList = new ArrayList<>(Arrays.asList(gson.fromJson(jsonString, User[].class)));

        // Add all fake users to the userList, and update the existing entries as necessary.
        this.updateFakeUsers(fakeNames, userList);

        // After we have updated the userList, we have to serialize it again and write it back to the file.
        usercacheFile.overwriteFile(gson.toJson(userList));

    }

    /**
     * This method scans the fake names list and figures out which to add and which to update. It then either modifies the entry in the list or it adds new entries. Usernames will be checked against Mojang's servers for conflicts with real usernames
     * @param fakeNames A list of fake usernames to add/update.
     * @param userList A list of users. This will be modified during the course of this method call.
     */
    private void updateFakeUsers(List<String> fakeNames, List<User> userList) {

        // Mojang arbitrarily set the default expiry time for the usercache to 1 month from the last time that user logged in. For our fake players, we ideally want that to be longer because if a fake player (i.e. the name does NOT exist in the Mojang name registry) takes more than 1 month to log in and the date of login surpasses the exiry date a call will be made to Mojang's API and the entry will be removed from the usercache.
        // Therefore, we set it to a really long time from now to decrease the chances of this happening. HOWEVER, note that if you log in with the fake player the expiresOn tag will get reset to +1 month (regardless of its previous value) and you then have one month to run this program again before logging in with that player will no longer work.
        String newExpiry = ZonedDateTime.now().plusYears(2).format(Injector.FORMAT_EXPIRY);

        // The goal of this program is to get every fake player name in the usercache with some uuid (can be random) and some expiry date.
        for (String fakeUser : fakeNames) {
            
            boolean userFound = false;

            // If the fake name exists, don't do anything as it can be handled in the normal way.
            if (this.properties.checkUsernames && UUIDManager.usernameExists(fakeUser)) {
                System.err.printf("[WARNING] The username (%s) is actually registered to a real account on Mojang's servers, so it will be skipped. Good news, you don't have to use this program for that username. Because of this, please remove it from the fake name list to avoid unnecessary API calls.%n", fakeUser);
                continue;
            }

            // Search the userList for the same playername. We do this because regenerating the UUID every time (especially if you use this script often) can potentially screw up some fake player actions (would be missing an inventory, for example). So essentially, if it's in the list, just update the expiresOn tag.
            for (User user : userList) {

                // If the username matches, refresh the expiry, flag the user as found and then break the loop (we don't need to search any more)
                if (user.name.equals(fakeUser)) {

                    user.expiresOn = newExpiry;
                    userFound = true;
                    break;

                }

            }

            // If the user wasn't found in that entire list, we need to add this fake player as a new one.
            // Here we generate a new UUID and keep it as long as it's not tied to any account.
            String fakeUUID;

            do {

                // Generate a new fake UUID. This is not guaranteed to be unique (contrary to the name :P) so we check it with Mojang's servers to make sure. 99.99999% of the time this loop will only execute once, but who knows, you might get lucky.
                fakeUUID = UUIDManager.generateUUID();

            } while (this.properties.checkUsernames && UUIDManager.uuidExists(fakeUUID));

            // Add a new User with a fake UUID and a new expiry date. This is potentially inefficient because on the next run we have n + 1 users to search through but I don't imagine the use cases of this program involving adding a very large number of users to the usercache.
            if (!userFound)
                userList.add(new User(
                        fakeUser,
                        fakeUUID,
                        newExpiry
                ));

        }       // End for loop

    }

}
