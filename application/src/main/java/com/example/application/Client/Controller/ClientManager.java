package com.example.application.Client.Controller;

import org.glassfish.grizzly.http.util.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

@Controller
public class ClientManager {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.remote.base:http://192.168.10.113:8081}")
    private String remoteBase;

    @Value("${client.management.rest.base:http://localhost:8082/api}")
    private String managementRestBase;
    @Value("${client.management.ws.uri:ws://localhost:8080/app/matching}")
    private String managementWsUri;
    @Value("${app.server.ws.uri:ws://localhost:8081/game-server}")
    private String appServerWsUri;
    @Value("${app.server.rest.base:http://localhost:8081/api}")
    private String appServerRestBase;

    private String authApiUrl() {
        return managementRestBase + "/auth";
    }

    @GetMapping("/")
    public String home() {
        System.out.println("Accessed home page");
        return "home";
    }

    @GetMapping("/start")
    public String start() {
        System.out.println("Accessed start page");
        return "start";
    }

    @PostMapping("/login-process")
    public String processLogin(@RequestParam String username, @RequestParam String password,
            HttpSession session, Model model) {
        try {
            Map<String, String> request = Map.of("username", username, "password", password);
            Map response = restTemplate.postForObject(authApiUrl() + "/login", request, Map.class);
            if (response != null) {
                session.setAttribute("loginName", username);
                System.out.println("User " + username + " logged in successfully.");
                return "redirect:/start";
            }
        } catch (Exception e) {
            model.addAttribute("error", "ログイン失敗、または既にログイン中です。");
            System.out.println("Login failed for user " + username + ": " + e.getMessage());
        }
        return "home";
    }

    @PostMapping("/register-process")
    public String processSignup(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            restTemplate.postForObject(authApiUrl() + "/register",
                    Map.of("username", username, "password", password), String.class);
            System.out.println("User " + username + " registered successfully.");
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "登録に失敗しました。");
            System.out.println("Registration failed for user " + username + ": " + e.getMessage());
            return "home";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        String user = (String) session.getAttribute("loginName");
        if (user != null) {
            try {
                restTemplate.postForObject(authApiUrl() + "/logout", Map.of("username", user), String.class);
                System.out.println("User " + user + " logged out successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Logout failed for user " + user + ": " + e.getMessage());
            }
        }
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/matchingWait")
    public String matchingWait(Model model) {
        System.out.println("Accessed matchingWait page");
        model.addAttribute("mgmtWsUri", managementWsUri);
        model.addAttribute("mgmtRestBase", managementRestBase);
        return "matchingwait";
    }

    @GetMapping("/rule")
    public String rule() {
        System.out.println("Accessed rule page");
        return "rule";
    }

    @GetMapping("/game")
    public String index(@RequestParam(required = false) String roomId,
            @RequestParam(required = false) String playerId,
            Model model, HttpSession session) {
        model.addAttribute("earnedUnits", 0);
        model.addAttribute("expectedUnits", 25);
        model.addAttribute("result", "ダイスを振ってください");
        model.addAttribute("mgmtRestBase", managementRestBase);
        model.addAttribute("appWsUri", appServerWsUri);
        System.out.println("Accessed game page. roomId=" + roomId + ", playerId=" + playerId);
        return "game";
    }

    @GetMapping("/score")
    public String showScorePage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("loginName");
        if (username == null) return "redirect:/";

        try {
            String url = authApiUrl() + "/score?username=" + username;
            Map<String, Object> record = restTemplate.getForObject(url, Map.class);

            model.addAttribute("username", username);
            model.addAttribute("record", record); // Thymeleafへ渡す
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "戦績の取得に失敗しました。");
        }
        return "score"; // score.html を表示
    }

    @GetMapping("/proxy/result")
    public ResponseEntity<String> proxyResult(@RequestParam String roomId,
            @RequestParam(required = false) String playerId) {
        String url = remoteBase + "/result?roomId=" + roomId;
        if (playerId != null && !playerId.isBlank())
            url += "&playerId=" + playerId;

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);

        ResponseEntity<String> res = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        HttpHeaders out = new HttpHeaders();
        out.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity<>(res.getBody(), out, res.getStatusCode());
    }
}