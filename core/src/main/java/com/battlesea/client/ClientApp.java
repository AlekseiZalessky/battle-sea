package com.battlesea.client;

public class ClientApp {
    private static String host = "localhost";
    private static int port = 8189;

    public static void main(String[] args) {
        try {
            new Client(host, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
