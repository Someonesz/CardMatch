package top.someones.cardmatch.core;

public class GameResource {

    private final String gameName;
    private final String UUID;
    private final String version;
    private final String resPath;
    private final String frontResName;
    private final String[] backResName;

    public GameResource(String gameName, String UUID, String version, String resPath, String frontResName, String[] backResName) {
        this.gameName = gameName;
        this.UUID = UUID;
        this.version = version;
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

    public String getUUID() {
        return UUID;
    }

    public String getVersion() {
        return version;
    }

    public String getFrontResName() {
        return frontResName;
    }

    public String[] getBackResName() {
        return backResName;
    }

}
