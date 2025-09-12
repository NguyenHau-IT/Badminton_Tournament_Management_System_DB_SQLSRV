package com.example.badmintoneventtechnology.model.match;

public class Player {
    public final int nnr;
    public final String name;

    public Player(int nnr, String name) {
        this.nnr = nnr;
        this.name = (name == null || name.isBlank()) ? ("#" + nnr) : name;
    }

    public int getNnr() {
        return nnr;
    }

    public String getName() {
        return name;
    }
}