package top.someones.cardmatch.entity;

public class RankItemData {
    private final String timeOrName;
    private final int score;

    public RankItemData(String timeOrName, int score) {
        this.timeOrName = timeOrName;
        this.score = score;
    }

    public String getTimeOrName() {
        return timeOrName;
    }

    public int getScore() {
        return score;
    }
}
