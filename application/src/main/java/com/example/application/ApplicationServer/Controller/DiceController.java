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
            // ジャストダイス：指定された値をそのまま返す
            return targetValue;
        }

        if ("DOUBLE".equals(itemType)) {
            // ダブルダイス：2回振って合計を返す
            return roll() + roll();
        }

        // 通常：1回振る
        return roll();
    }

    // 純粋に1回だけ振る内部メソッド
    public int roll() {
        return random.nextInt(6) + 1;
    }
}