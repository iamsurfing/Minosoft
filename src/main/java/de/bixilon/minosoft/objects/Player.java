package de.bixilon.minosoft.objects;

import java.util.UUID;

public class Player {
    Account acc;
    float health;
    short food;
    float saturation;

    public Player(Account acc) {
        this.acc = acc;
        acc.login();
    }

    public String getPlayerName() {
        return acc.getPlayerName();
    }

    public UUID getPlayerUUID() {
        return acc.getUUID();
    }

    public Account getAccount() {
        return this.acc;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public short getFood() {
        return food;
    }

    public void setFood(short food) {
        this.food = food;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }
}
