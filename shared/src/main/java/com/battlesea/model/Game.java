package com.battlesea.model;

import com.battlesea.enums.Cell;
import com.battlesea.enums.GameMode;
import com.battlesea.enums.GameStatus;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class Game {
    private static final Logger log = LoggerFactory.getLogger(Game.class);
    private final UUID id;
    private final Player creator;
    private Player opponent;
    private final LocalDateTime creationTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private GameMode gameMode;
    private GameStatus gameStatus;
    private Board boardCreator;
    private Board boardOpponent;
    private Player turnPlayer;
    private Player winner;
    public static final int TURN_TIME = 10;
    private boolean gameOver;

    public Game(Player creator, Board boardCreator, Player opponent, GameMode gameMode) {
        this.id = UUID.randomUUID();
        this.creator = creator;
        this.boardCreator = boardCreator;
        this.opponent = opponent;
        this.creationTime = LocalDateTime.now();
        this.gameMode = gameMode;
    }

    public Game(Player creator, Board board, GameMode gameMode) {
        this(creator, board, null, gameMode);
    }

    public Cell shoot(Coordinate coordinate) {
        int x = coordinate.x();
        int y = coordinate.y();

        Board targetBoard = turnPlayer == creator ? boardOpponent : boardCreator;

        Cell[][] cells = targetBoard.getCells();

        switch (cells[x][y]) {
            case HALO:
            case HIT:
            case MISS:
                log.debug(cells[x][y].toString());
                return null;
            case EMPTY:
                cells[x][y] = Cell.MISS;
                log.debug(cells[x][y].toString());
                switchTurnPlayer();
                return Cell.MISS;
            case SHIP:
                cells[x][y] = Cell.HIT;
                log.debug(cells[x][y].toString());
                Ship ship = targetBoard.getShip(coordinate);
                if (ship.checkIsSunk(cells)) {
                    if (targetBoard.allShipsIsSunk()) {
                        endGame(GameStatus.ENDED);
                    }
                }
                targetBoard.addHalo(coordinate);
                return Cell.HIT;
        }
        return null;
    }

    public void endGame(GameStatus gameStatus) {
        this.winner = turnPlayer;
        this.gameOver = true;
        this.gameStatus = gameStatus;
        this.endTime = LocalDateTime.now();
    }

    public void switchTurnPlayer() {
        if (turnPlayer == creator) {
            turnPlayer = opponent;
        } else {
            turnPlayer = creator;
        }
        log.info("Turn switched to: {}", turnPlayer.getName());
    }

}
