package com.battlesea.server;

import com.battlesea.enums.GameMode;
import com.battlesea.model.Game;
import com.battlesea.service.AIService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Setter
@Getter
public class GameSession {
    private final Game game;
    private final AIService aiService;
    private final ClientHandler creatorHandler;
    private ClientHandler opponentHandler;
    private static final Logger log = LoggerFactory.getLogger(GameSession.class);

    public GameSession(Game game, ClientHandler creatorHandler) {
        if (game == null) {
            log.error("Game is null");
            throw new IllegalArgumentException("Game is null");
        }
        if (creatorHandler == null) {
            log.error("CreatorHandler is null");
            throw new IllegalArgumentException("CreatorHandler is null");
        }
        this.game = game;
        this.aiService = game.getGameMode() == GameMode.PVE ? new AIService() : null;
        this.creatorHandler = creatorHandler;
        log.debug("created game session: {}", game);
    }


    @Override
    public String toString() {
        return "GameSession{" +
            "game=" + game +
            ", aiService=" + aiService +
            ", creatorHandler=" + creatorHandler +
            ", opponentHandler=" + opponentHandler +
            '}';
    }
}
