package com.battlesea;

import com.battlesea.enums.GameMode;
import com.battlesea.model.Board;
import com.battlesea.model.Player;
import com.battlesea.service.GameService;
import com.battlesea.service.ShipPlacementService;

public class Start {
    public static void main(String[] args) {
        ShipPlacementService service = new ShipPlacementService();
        Player player = new Player("qweqwe");
        Board board = service.generateRandomShips(player);
        GameService gameService = new GameService();
        gameService.startGame(player, board, GameMode.PVE);
        board.printCells();

    }
}
