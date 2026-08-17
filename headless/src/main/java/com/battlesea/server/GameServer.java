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
    private static final Logger log = LoggerFactory.getLogger(GameServer.class);

    public GameServer(int port) {
        this.port = port;
        this.executor = Executors.newFixedThreadPool(5);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.debug("Сервер запущен на порту {}", port);

            while (true) {
                Socket socket = serverSocket.accept();
                executor.execute(() -> {
                    try {
                        new ClientHandler(socket, this);
                    } catch (Exception e) {
                        log.error("Error creating ClientHandler: {}", e.getMessage(), e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error in GameServer: {}", e.getMessage(), e);
        }
    }

    public void registerPlayer(Player player, ClientHandler clientHandler) {
        if (player == null) {
            log.error("Player is null");
            throw new IllegalArgumentException("Player is null");
        }
        if (clientHandler == null) {
            log.error("ClientHandler is null");
            throw new IllegalArgumentException("ClientHandler is null");
        }
        log.debug("Игрок {} зарегистрирован", player.getName());
        players.put(player, clientHandler);
    }

    public ClientHandler getClientHandler(Player player) {
        if (player == null) {
            log.error("Player is null");
            throw new IllegalArgumentException("Player is null");
        }
        return players.get(player);
    }

    public void removePlayer(Player player) {
        if (player == null) {
            log.error("Player is null");
            throw new IllegalArgumentException("Player is null");
        }
        log.debug("Игрок {} удален", player.getName());
        players.remove(player);
    }

    public GameSession createSession(Game game, ClientHandler clientHandler) {
        if (game == null) {
            log.error("Game is null");
            throw new IllegalArgumentException("Game is null");
        }
        if (clientHandler == null) {
            log.error("ClientHandler is null");
            throw new IllegalArgumentException("ClientHandler is null");
        }
        GameSession session = new GameSession(game, clientHandler);
        sessions.put(game.getId(), session);
        log.debug("Создана сессия для игры с id: {}", game.getId());
        return session;
    }

    public GameSession getSession(UUID gameId) {
        if (gameId == null) {
            log.error("Game id is null");
            throw new IllegalArgumentException("Game id is null");
        }
        return sessions.get(gameId);
    }

    public void removeGameSession(UUID gameId) {
        if (gameId == null) {
            log.error("Game id is null");
            throw new IllegalArgumentException("Game id is null");
        }
        log.debug("Удалена сессия с id: {}", gameId);
        sessions.remove(gameId);
    }
}
