package com.example.application.ApplicationServer.Controller;

import org.springframework.stereotype.Service;
import lombok.Data;

@Data
@Service
public class CreditManager {
    private int earnedUnits = 0; 
    private int expectedUnits = 25;
    private final int GRADUATION_UNITS = 25; 

    public void graduateUnits() {
        this.earnedUnits += this.expectedUnits;
    }

    public boolean isGraduated() {
        return this.earnedUnits >= GRADUATION_UNITS;
    }

    public void reset() {
        this.earnedUnits = 0;
        this.expectedUnits = 25;
    }
}
