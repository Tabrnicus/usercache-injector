package com.nchroniaris.ucinjector.model;

/**
 * This class serves as a model (for use with GSON) of the JSON objects in usercache.json. In particular, usercache.json has an array of these objects, where every field is strictly a string. Technically, expiresOn is a date but it's been serialized to a string so instead of letting GSON interpret it we leave it to be grabbed as a string here.
 */
public class User {

    public String name;
    public String uuid;
    public String expiresOn;

    public User(String name, String uuid, String expiresOn) {
        this.name = name;
        this.uuid = uuid;
        this.expiresOn = expiresOn;
    }

}
