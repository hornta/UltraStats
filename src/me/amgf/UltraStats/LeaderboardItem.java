package me.amgf.UltraStats;

public class LeaderboardItem implements Comparable<LeaderboardItem> {
    private String name;
    private Integer amount;

    LeaderboardItem(String name, Integer amount) {
        this.name = name;
        this.amount = amount;
    }

    String getName() {
        return name;
    }

    Integer getAmount() {
        return amount;
    }

    public int compareTo(LeaderboardItem leaderboardItem) {
        return this.getAmount() - leaderboardItem.getAmount();
    }
}
