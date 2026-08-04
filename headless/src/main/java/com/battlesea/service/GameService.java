package com.battlesea.service;

import com.battlesea.enums.GameMode;
import com.battlesea.enums.GameStatus;
import com.battlesea.model.Board;
import com.battlesea.model.Game;
import com.battlesea.model.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameService {
    private final ShipPlacementService shipPlacementService;
    private final BattleService battleService;
    private static List<Game> createdGames = new ArrayList<>();

    public GameService() {
        shipPlacementService = new ShipPlacementService();
        battleService = new BattleService();
    }

    public Game startGame(Player player, Board board, GameMode gameMode) {
        Game game = null;
        Player opponent = null;
        Board opponentPlayerBoard = null;
        if (gameMode == com.battlesea.enums.GameMode.PVE) {
            opponent = new Player("Computer");
            game = new Game(player, board, opponent, gameMode);
            opponentPlayerBoard = shipPlacementService.generateRandomShips(opponent);
            game.setBoardOpponent(opponentPlayerBoard);
        }

        if (gameMode == GameMode.PVP_ONLINE) {
            if (createdGames.isEmpty()) {
                System.out.println("sozdanie novoi igri");
                game = new Game(player, board, gameMode);
                createdGames.add(game);
                game.setGameStatus(GameStatus.CREATED);
                game.setBoardCreator(board);
                System.out.println("sozdana igra: " + game);

            } else {
                game = createdGames.getFirst();
                System.out.println();
                createdGames.remove(game);
                game.setOpponent(player);
                game.setBoardOpponent(board);
                System.out.println("igrok: " + player + " podkluchilsya k igre: " + game);
            }
        }

        if (game == null) {
            return null;
        }

        game.setGameMode(gameMode);
        game.setGameStatus(GameStatus.STARTED);
        game.setStartTime(LocalDateTime.now());
        randomTurnPlayer(game);
        battleService.startGame(game);
        return game;
    }

    public void deleteGameFromCreatedGames(Game game) {
        System.out.println(createdGames);
        createdGames.remove(game);
        System.out.println(createdGames);
    }

    private void randomTurnPlayer(Game game) {
        Random random = new Random();
        boolean turnCreator = random.nextBoolean();
        if (turnCreator) {
            game.setTurnPlayer(game.getCreator());
        } else {
            game.setTurnPlayer(game.getOpponent());
        }
    }
}
