package top.someones.cardmatch.entity;

import android.graphics.Bitmap;

public class Mod {
    private final String UUID;
    private final String name;
    private final Bitmap image;
    private final String author;
    private final String version;


    public Mod(String uuid, String name, Bitmap image, String author, String version) {
        UUID = uuid;
        this.name = name;
        this.image = image;
        this.author = author;
        this.version = version;
    }

    public String getUUID() {
        return UUID;
    }

    public String getName() {
        return name;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }
}
