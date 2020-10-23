package top.someones.cardmatch.entity;

import android.graphics.Bitmap;

@Deprecated
public class GameResource {

    private final String UUID;
    private final String name;
    private final String author;
    private final Double version;
    private final String resPath;
    private final Bitmap cover;
    private final String frontResName;
    private final String[] backResName;

    public GameResource(String uuid, String name, String author, Double version, String resPath, Bitmap cover, String frontResName, String[] backResName) {
        UUID = uuid;
        this.name = name;
        this.author = author;
        this.version = version;
        this.resPath = resPath;
        this.cover = cover;
        this.frontResName = frontResName;
        this.backResName = backResName;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getResPath() {
        return resPath;
    }

    public String getUUID() {
        return UUID;
    }

    public Double getVersion() {
        return version;
    }

    public String getFrontResName() {
        return frontResName;
    }

    public String[] getBackResName() {
        return backResName;
    }

    public Bitmap getCover() {
        return cover;
    }
}
