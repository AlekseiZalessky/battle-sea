package com.battlesea.service;

import com.battlesea.enums.GameMode;
import com.battlesea.enums.GameStatus;
import com.battlesea.model.Board;
import com.battlesea.model.Game;
import com.battlesea.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameService {
    private static final Logger log = LoggerFactory.getLogger(GameService.class);
    private ShipPlacementService shipPlacementService;
    private static final List<Game> createdGames = new ArrayList<>();

    public GameService() {

    }

    public Game startGame(Player player, Board board, GameMode gameMode) {
        if (player == null) {
            log.error("Player is null");
            throw new IllegalArgumentException("Player is null");
        }
        if (board == null) {
            log.error("Board is null");
            throw new IllegalArgumentException("Board is null");
        }
        if (gameMode == null) {
            log.error("GameMode is null");
            throw new IllegalArgumentException("GameMode is null");
        }
        Game game = null;
        Player opponent = null;
        Board opponentPlayerBoard = null;
        if (gameMode == com.battlesea.enums.GameMode.PVE) {
            shipPlacementService = new ShipPlacementService();
            opponent = new Player("Computer");
            game = new Game(player, board, opponent, gameMode);
            opponentPlayerBoard = shipPlacementService.generateRandomShips(opponent, new Board());
            game.setBoardOpponent(opponentPlayerBoard);
            randomTurnPlayer(game);
            game.setGameStatus(GameStatus.STARTED);
        }

        if (gameMode == GameMode.PVP_ONLINE) {
            if (createdGames.isEmpty()) {
                log.debug("Creating new game for player: {}", player);
                game = new Game(player, board, gameMode);
                createdGames.add(game);
                game.setGameStatus(GameStatus.CREATED);
                game.setBoardCreator(board);
                log.debug("Created game: {}", game);

            } else {
                game = createdGames.getFirst();
                System.out.println();
                createdGames.remove(game);
                game.setOpponent(player);
                game.setBoardOpponent(board);
                randomTurnPlayer(game);
                game.setGameStatus(GameStatus.STARTED);
                log.debug("Player: {} connected to game: {}", player, game);
            }
        }

        if (game == null) {
            return null;
        }

        game.setGameMode(gameMode);
        game.setStartTime(LocalDateTime.now());
        return game;
    }

    public void deleteGameFromCreatedGames(Game game) {
        if (game == null) {
            log.error("Game is null");
            throw new IllegalArgumentException("Game is null");
        }
        createdGames.remove(game);
    }

    private void randomTurnPlayer(Game game) {
        Random random = new Random();
        boolean turnCreator = random.nextBoolean();
        if (turnCreator) {
            game.setTurnPlayer(game.getCreator());
        } else {
            game.setTurnPlayer(game.getOpponent());
        }
        log.debug("randomTurnPlayer: {}", game.getTurnPlayer());
    }
}
