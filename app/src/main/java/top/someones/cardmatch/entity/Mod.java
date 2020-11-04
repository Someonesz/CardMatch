package top.someones.cardmatch.entity;

import android.graphics.Bitmap;

public class Mod {
    private final String uuid;
    private final String name;
    private final Bitmap image;
    private final String author;
    private final Double version;
    private final String show;

    public Mod(String uuid, String name, Bitmap image, String author, Double version, String show) {
        this.uuid = uuid;
        this.name = name;
        this.image = image;
        this.author = author;
        this.version = version;
        this.show = show;
    }

    public String getUUID() {
        return uuid;
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

    public Double getVersion() {
        return version;
    }

    public String getShow() {
        return show;
    }
}
