package com.battlesea.service;


import com.battlesea.enums.GameMode;
import com.battlesea.enums.GameStatus;
import com.battlesea.model.Board;
import com.battlesea.model.Game;
import com.battlesea.model.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GameService {
    private final ShipPlacementService shipPlacementService;
    private final BattleService battleService;
    private static List<Game> createdGames = new ArrayList<>();

    public GameService() {
        shipPlacementService = new ShipPlacementService();
        battleService = new BattleService();
    }

//    public void selectGameMode(Player player) {
//
//    }

    public Game startGame(Player player, Board board, GameMode gameMode) {
        Game game = null;
        Player opponent = null;
        Board opponentPlayerBoard = null;
        if (gameMode == com.battlesea.enums.GameMode.PVE) {
            opponent = new Player("Computer");
            game = new Game(player, opponent, gameMode);
            opponentPlayerBoard = shipPlacementService.generateRandomShips(opponent);
            game.setBoardPlayer2(opponentPlayerBoard);
        }

        if (gameMode == GameMode.PVP_ONLINE) {
            if (createdGames.isEmpty()) {
                System.out.println("sozdanie novoi igri");
                game = new Game(player, gameMode);
                createdGames.add(game);
                game.setGameStatus(GameStatus.CREATED);
                game.setBoardPlayer1(board);
//                waitOpponent(game);
//                System.out.println("opponent connected");
//                game.setPlayer2(opponent);

            } else {
                game = createdGames.getFirst();
                System.out.println("podkluchenie k igre: " + game);
                createdGames.remove(game);
                game.setPlayer2(player);
                game.setBoardPlayer2(board);
            }
        }

        if(game == null){
            return null;
        }

        game.setGameMode(gameMode);
        game.setGameStatus(GameStatus.STARTED);
        game.setStartTime(LocalDateTime.now());

        battleService.startGame(game);
        return game;
    }

    private void waitOpponent(Game game) {
        while (game.getBoardPlayer2() == null) {
            try {
                System.out.println("waiting for opponent to start");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

//    private Player findOpponent() {
//
//    }
}
