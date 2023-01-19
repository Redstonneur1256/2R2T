package fr.redstonneur1256.omega.config;

public class RemoteServerInfo {

    public String key;
    public String uri;

    public static RemoteServerInfo createDefault() {
        var server = new RemoteServerInfo();

        server.key = "some shared key with the bot";
        server.uri = "ws://example.com/websocket";

        return server;
    }

}
