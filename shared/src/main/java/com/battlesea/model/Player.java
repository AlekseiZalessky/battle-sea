package com.battlesea.model;

import java.util.UUID;

public class Player {
    private final UUID id;
    private String name;


    public Player(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
