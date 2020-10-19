package top.someones.cardmatch.core;

public class GameResources {

    private final String gameName;
    private final String resPath;
    private final String frontResName;
    private final String[] backResName;

    public GameResources(String gameName, String resPath, String frontResName, String[] backResName) {
        this.gameName = gameName;
        this.resPath = resPath;
        this.frontResName = frontResName;
        this.backResName = backResName;
    }

    public String getGameName() {
        return gameName;
    }

    public String getResPath() {
        return resPath;
    }

    public String getFrontResName() {
        return frontResName;
    }

    public String[] getBackResName() {
        return backResName;
    }

    public int getBackResLength() {
        return backResName.length;
    }

}
