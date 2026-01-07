package com.example.application.ApplicationServer.Entity;

import java.util.UUID;
import lombok.Data;

@Data

public class Player {
    private String id;
    private String name;
    private String color;
    private int currentPosition;
    private int earnedUnits;
    private int expectedUnits;

    public Player(String name, String color) {
        this.id = UUID.randomUUID().toString(); 
        this.name = name;
        this.color = color;
        this.currentPosition = 0;
        this.earnedUnits = 0;
        this.expectedUnits = 25;
    }
}

   