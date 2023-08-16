package com.yshs.hytcheckrecord.records;

public class BedWarsRecord {
    private int mvpNum;
    private int playNum;
    private double winRate; //小数
    private int beddesNum;
    private double killDead; //小数

    private String playerName;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getMvpNum() {
        return mvpNum;
    }

    public void setMvpNum(int mvpNum) {
        this.mvpNum = mvpNum;
    }

    public int getPlayNum() {
        return playNum;
    }

    public void setPlayNum(int playNum) {
        this.playNum = playNum;
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public int getBeddesNum() {
        return beddesNum;
    }

    public void setBeddesNum(int beddesNum) {
        this.beddesNum = beddesNum;
    }

    public double getKillDead() {
        return killDead;
    }

    public void setKillDead(double killDead) {
        this.killDead = killDead;
    }

    public double getWinRatePercent() {
        return winRate * 100;
    }

    public double getMvpRatePercent() {
        return (double) mvpNum / this.getWinNum() * 100;
    }

    public int getWinNum() {
        return (int) (winRate * playNum);
    }

}
