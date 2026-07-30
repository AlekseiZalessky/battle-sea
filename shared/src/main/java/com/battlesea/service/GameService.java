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

    public void startGame(Player currentPlayer, Board board, GameMode gameMode) {
        Game game = null;
        Player opponent = null;
        Board currentPlayerBoard = board;
        Board opponentPlayerBoard = null;
        if (gameMode == com.battlesea.enums.GameMode.PVE) {
            opponent = new Player("Computer");
            game = new Game(currentPlayer, opponent, gameMode);
            currentPlayerBoard = shipPlacementService.generateRandomShips(currentPlayer);
            opponentPlayerBoard = shipPlacementService.generateRandomShips(opponent);

        }

//        if (gameMode == GameMode.PVP_ONLINE) {
//            currentPlayerBoard = shipPlacementService.generateRandomShips(currentPlayer);
//            game = createdGames.getFirst();
//            if (game == null) {
//                game = new Game(currentPlayer, gameMode);
//                createdGames.add(game);
//                game.setGameStatus(GameStatus.CREATED);
//                opponent = findOpponent();
//                if (opponent == null) {
//                    return;
//                }
//                game.setPlayer2(opponent);
//            } else {
//                createdGames.remove(game);
//                game.setPlayer2(currentPlayer);
//            }
//        }

        if(game == null){
            return;
        }

        game.setGameMode(gameMode);
        game.setGameStatus(GameStatus.STARTED);
        game.setStartTime(LocalDateTime.now());
        battleService.startGame(game, currentPlayerBoard, opponentPlayerBoard);
    }

//    private Player findOpponent() {
//
//    }
}
