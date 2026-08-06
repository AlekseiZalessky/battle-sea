package com.battlesea.server;

import com.battlesea.model.Game;
import com.battlesea.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private final int port;
    private final ExecutorService executor;
    private final Map<Player, ClientHandler> players = new ConcurrentHashMap<>();
    private final Map<UUID, GameSession> sessions = new ConcurrentHashMap<>();
    private ClientHandler clientHandler;
    private static final Logger log = LoggerFactory.getLogger(GameServer.class);

    public GameServer(int port) {
        this.port = port;
        this.executor = Executors.newFixedThreadPool(5);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.debug("Сервер запущен на порту " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                executor.execute(() ->{
                    try {
                        clientHandler = new ClientHandler(socket, this);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error in GameServer: {}", e.getMessage(), e);
        }
    }

    public void registerPlayer(Player player, ClientHandler clientHandler) {
        players.put(player, clientHandler);
    }

    public ClientHandler getClientHandler(Player player) {
        return players.get(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public GameSession createSession(Game game) {
        GameSession session = new GameSession(game, clientHandler);
        sessions.put(game.getId(), session);
        return session;
    }

    public GameSession getSession(UUID gameId) {
        return sessions.get(gameId);
    }

    public void removeGameSession(UUID gameId) {
        sessions.remove(gameId);
    }
}
