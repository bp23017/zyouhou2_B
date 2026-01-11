package com.example.application.ClientManagementServer.Controller;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.tyrus.server.Server;

import com.example.application.ClientManagementServer.Controller.AccountManagement.MatchingRest;

import java.net.URI;

// クライアント管理サーバーの起動クラス
public class ManagementServerLauncher {
    static String contextRoot = "/app";
    static String protocol = "ws";
    static int port = 8080;
    public static final String restUri = "http://0.0.0.0:8082/api"; 

    public static void main(String[] args) throws Exception {

        DatabaseAccess dbAccess = new DatabaseAccess();
        dbAccess.resetAllLoginStatuses();

        // WebSocketサーバの起動
        Server wsServer = new Server(protocol, port, contextRoot, null, CommunicationController.class);
        wsServer.start();

        final ResourceConfig rc = new ResourceConfig(AccountManagement.class);
        rc.register(AccountManagement.class);
        rc.register(MatchingRest.class); 
        rc.register(CorsFilter.class);   
        
        final HttpServer restServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(restUri), rc);

        try {
            wsServer.start();
            System.out.println("[Management Server] Started. REST: 8082 / WS: 8080");
            System.in.read(); 
        } finally {
            wsServer.stop();
            restServer.shutdownNow();
        }
    }
}