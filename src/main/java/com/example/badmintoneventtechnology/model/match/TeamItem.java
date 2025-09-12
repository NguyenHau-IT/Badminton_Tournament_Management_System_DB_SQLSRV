package com.example.badmintoneventtechnology.model.match;

public class TeamItem {
    public final int teamId;
    public final String label;

    public TeamItem(int teamId, String label) {
        this.teamId = teamId;
        this.label = (label == null || label.isBlank()) ? ("#" + teamId) : label;
    }

    @Override
    public String toString() {
        return label;
    }
}