package top.someones.cardmatch.entity;

import android.graphics.Bitmap;

public class Mod {
    private final String uuid;
    private final String name;
    private final Bitmap cover;
    private final String author;
    private final Double version;
    private final String show;


    public Mod(String uuid, String name, Bitmap cover, String author, Double version, String show) {
        this.uuid = uuid;
        this.name = name;
        this.cover = cover;
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

    public Bitmap getCover() {
        return cover;
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
