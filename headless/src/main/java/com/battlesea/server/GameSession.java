package com.battlesea.server;

import com.battlesea.enums.GameMode;
import com.battlesea.model.Game;
import com.battlesea.service.AIService;
import com.battlesea.service.BattleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameSession {
    private final Game game;
    private final AIService aiService;
    private final BattleService battleService;
    private final ClientHandler creatorHandler;
    private ClientHandler opponentHandler;
    private static final Logger log = LoggerFactory.getLogger(GameSession.class);

    public GameSession(Game game, ClientHandler creatorHandler) {
        this.game = game;
        this.battleService = new BattleService();
        this.aiService = game.getGameMode() == GameMode.PVE ? new AIService(game) : null;
        this.creatorHandler = creatorHandler;
        log.debug("""
            \ngame: {}
            aiService: {}
            battleService: {}
            creatorHandler: {}
            opponentHandler: {}""",
            game, aiService, battleService, creatorHandler, opponentHandler);
    }

    public Game getGame() {
        return game;
    }

    public AIService getAiService() {
        return aiService;
    }

    public BattleService getBattleService() {
        return battleService;
    }

    public ClientHandler getCreatorHandler() {
        return creatorHandler;
    }

    public ClientHandler getOpponentHandler() {
        return opponentHandler;
    }

    public void setOpponentHandler(ClientHandler opponentHandler) {
        this.opponentHandler = opponentHandler;
    }

    @Override
    public String toString() {
        return "GameSession{" +
            "game=" + game +
            ", aiService=" + aiService +
            ", battleService=" + battleService +
            ", creatorHandler=" + creatorHandler +
            ", opponentHandler=" + opponentHandler +
            '}';
    }
}
