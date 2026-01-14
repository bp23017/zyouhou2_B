package com.example.application.ApplicationServer.Controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.example.application.WebSocketConfig;

/**
 * アプリケーションサーバ専用の起動エントリ。
 */
@SpringBootApplication(scanBasePackages = "com.example.application.ApplicationServer")
@Import(WebSocketConfig.class)
public class AppServerLauncher {

    public static void main(String[] args) {
        // 環境変数 / JVM 引数で指定がなければデフォルトプロファイルを付与
        if (System.getenv("SPRING_PROFILES_ACTIVE") == null
                && System.getProperty("spring.profiles.active") == null) {
            System.setProperty("spring.profiles.active", "app-server");
        }

        SpringApplication.run(AppServerLauncher.class, args);
    }
}
