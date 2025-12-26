package com.example.application.ApplicationServer.Controller;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class DiceController {

    private final Random random = new Random();

    /**
     * ダイスを振るメインロジック
     * @param itemType アイテムの種類 (DOUBLE, JUST, null)
     * @param targetValue ジャストダイスの場合の狙う数字
     * @return 最終的なダイスの合計値
     */
    public int executeRoll(String itemType, Integer targetValue) {
        if ("JUST".equals(itemType) && targetValue != null) {
            return targetValue;
        }

        if ("DOUBLE".equals(itemType)) {
            return roll() + roll();
        }

        return roll();
    }
    public int roll() {
        return random.nextInt(6) + 1;
    }
}