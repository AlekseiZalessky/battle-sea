package com.battlesea.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.battlesea.client.Client;
import com.battlesea.Main;
import com.battlesea.button.ButtonFactory;
import com.battlesea.constants.Commands;
import com.battlesea.enums.Cell;
import com.battlesea.enums.GameMode;
import com.battlesea.enums.TypeShip;
import com.battlesea.model.Board;
import com.battlesea.model.Coordinate;
import com.battlesea.model.Ship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PlacementScreen implements Screen {
    private final Main game;
    private Stage stage;
    private GameMode gameMode;
    private BitmapFont font;
    private Client client;
    private SpriteBatch batch;
    private Board boardPlayer1;
    private Texture cellEmpty;
    private Texture cellShip;
    private Texture cellHalo;
    private TextButton startGame;
    private Map<Ship, List<Integer>> shipCoordinateMap = new HashMap<>();
    private Map<Ship, List<Integer>> backup = new HashMap<>();
    private Ship copyShip;
    private int sizeDeck = 32;
    private final Vector2 position;
    private static final Logger log = LoggerFactory.getLogger(PlacementScreen.class);

    private Ship draggingShip;           // корабль, который сейчас перетаскиваем
    private boolean isDragging;          // флаг, что перетаскивание активно
    private float dragOffsetX;           // смещение по X от центра корабля до курсора
    private float dragOffsetY;           // смещение по Y
    private boolean isClick;              // флаг, что это был клик (а не перетаскивание)
    private float startMouseX, startMouseY; // начальные координаты клика

    {
        int startX = 500;
        int startY = 340;

        Ship four = new Ship(startX, startY, true, TypeShip.FourDeckShip);
        shipCoordinateMap.put(four, List.of(startX, startX + sizeDeck * 4, startY, startY + sizeDeck));

        Ship threeN1 = new Ship(startX, startY - sizeDeck * 2, true, TypeShip.ThreeDeckShip);
        shipCoordinateMap.put(threeN1, List.of(startX, startX + sizeDeck * 3, startY - sizeDeck * 2, startY - sizeDeck));

        Ship threeN2 = new Ship(startX + sizeDeck * 4, startY - sizeDeck * 2, true, TypeShip.ThreeDeckShip);
        shipCoordinateMap.put(threeN2, List.of(startX + sizeDeck * 4, startX + sizeDeck * 7, startY - sizeDeck * 2, startY - sizeDeck));

        Ship twoN1 = new Ship(startX, startY - sizeDeck * 4, true, TypeShip.TwoDeckShip);
        shipCoordinateMap.put(twoN1, List.of(startX, startX + sizeDeck * 2, startY - sizeDeck * 4, startY - sizeDeck * 3));

        Ship twoN2 = new Ship(startX + sizeDeck * 3, startY - sizeDeck * 4, true, TypeShip.TwoDeckShip);
        shipCoordinateMap.put(twoN2, List.of(startX + sizeDeck * 3, startX + sizeDeck * 5, startY - sizeDeck * 4, startY - sizeDeck * 3));

        Ship twoN3 = new Ship(startX + sizeDeck * 6, startY - sizeDeck * 4, true, TypeShip.TwoDeckShip);
        shipCoordinateMap.put(twoN3, List.of(startX + sizeDeck * 6, startX + sizeDeck * 8, startY - sizeDeck * 4, startY - sizeDeck * 3));

        Ship oneN1 = new Ship(startX, startY - sizeDeck * 6, true, TypeShip.OneDeckShip);
        shipCoordinateMap.put(oneN1, List.of(startX, startX + sizeDeck, startY - sizeDeck * 6, startY - sizeDeck * 5));

        Ship oneN2 = new Ship(startX + sizeDeck * 2, startY - sizeDeck * 6, true, TypeShip.OneDeckShip);
        shipCoordinateMap.put(oneN2, List.of(startX + sizeDeck * 2, startX + sizeDeck * 3, startY - sizeDeck * 6, startY - sizeDeck * 5));

        Ship oneN3 = new Ship(startX + sizeDeck * 4, startY - sizeDeck * 6, true, TypeShip.OneDeckShip);
        shipCoordinateMap.put(oneN3, List.of(startX + sizeDeck * 4, startX + sizeDeck * 5, startY - sizeDeck * 6, startY - sizeDeck * 5));

        Ship oneN4 = new Ship(startX + sizeDeck * 6, startY - sizeDeck * 6, true, TypeShip.OneDeckShip);
        shipCoordinateMap.put(oneN4, List.of(startX + sizeDeck * 6, startX + sizeDeck * 7, startY - sizeDeck * 6, startY - sizeDeck * 5));


        for (Map.Entry<Ship, List<Integer>> entry : shipCoordinateMap.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    public PlacementScreen(Client client, Main game, GameMode gameMode) {
        this.game = game;
        this.gameMode = gameMode;
        this.client = client;
        this.boardPlayer1 = new Board();
        this.position = new Vector2(50, 50);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        cellEmpty = new Texture("cellEmpty.png");
        cellShip = new Texture("cellShip.png");
        cellHalo = new Texture("cellHalo.png");

        font = new BitmapFont();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        TextButton autoButton = ButtonFactory.createAutoButton(client, font);
        stage.addActor(autoButton);

        startGame = ButtonFactory.createStartButton(font);
        startGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!startGame.isDisabled()) {
                    System.out.println("click start");
                    if (gameMode == GameMode.PVE) {
                        client.sendMessage(Commands.START_GAME_PVE);
                        timeout(2000);
                        game.setScreen(new BattleScreen(client, game, gameMode));
                    } else {
                        client.sendMessage(Commands.START_GAME_PVP_ONLINE);
                        timeout(1000);
                        game.setScreen(new WaitingStartBattleScreen(client, game, gameMode));
                    }
                }
            }
        });
        stage.addActor(startGame);

        // Кнопка возврата в меню
        TextButton menuButton = ButtonFactory.createMenuButton(font);
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("click menu");
                client.setGameOver(true);
                client.updateOnStartGame();
                client.sendMessage(Commands.UPDATE_FIELDS);
                game.setScreen(new MenuScreen(client, game));
            }
        });
        stage.addActor(menuButton);
    }

    private static void timeout(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (client != null) {
            Board newBoard = client.getBoardCreator();
            if (newBoard != null) {
                boardPlayer1 = newBoard;
                if (!boardPlayer1.getShips().isEmpty() && client.isAllShipPlaced()) {
                    startGame.setDisabled(false);
                }
            }
        }

        handleDragAndDrop();

        batch.begin();

        if (boardPlayer1 != null) {
            drawMyBoard(batch, boardPlayer1);
        } else {
            drawEmptyBoard(batch);
        }

        stage.act(delta);
        stage.draw();

        drawShips();
        batch.end();
    }

    private void drawShips() {
        for (Map.Entry<Ship, List<Integer>> entry : shipCoordinateMap.entrySet()) {
            Ship ship = entry.getKey();
            int x = ship.getX();
            int y = ship.getY();
            int size = ship.getType().getSize();

            if (ship.isHorizontal()) {
                // Рисуем по горизонтали
                for (int i = 0; i < size; i++) {
                    batch.draw(cellShip, x + i * sizeDeck, y);
                }
            } else {
                // Рисуем по вертикали
                for (int i = 0; i < size; i++) {
                    batch.draw(cellShip, x, y + i * sizeDeck);
                }
            }
        }
    }

    private void drawEmptyBoard(SpriteBatch batch) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                batch.draw(cellEmpty, sizeDeck * i + 50, sizeDeck * j + 50);
            }
        }
    }

    private void drawMyBoard(SpriteBatch batch, Board board) {
        if (board.getShips().isEmpty()) {
            drawEmptyBoard(batch);
            return;
        }

        Cell[][] cells = board.getCells();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (cells[i][j] == Cell.EMPTY) {
                    batch.draw(cellEmpty, sizeDeck * i + 50, sizeDeck * j + 50);
                }
                if (cells[i][j] == Cell.SHIP) {
                    batch.draw(cellShip, sizeDeck * i + 50, sizeDeck * j + 50);
                }
                if (cells[i][j] == Cell.HALO) {
                    batch.draw(cellHalo, sizeDeck * i + 50, sizeDeck * j + 50);
                }
            }
        }
    }

    private Ship getShipAt(int mouseX, int mouseY) {
        for (Map.Entry<Ship, List<Integer>> entry : shipCoordinateMap.entrySet()) {
            Ship ship = entry.getKey();
            List<Integer> coords = entry.getValue();
            int x1 = coords.get(0);
            int x2 = coords.get(1);
            int y1 = coords.get(2);
            int y2 = coords.get(3);

            if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
                return ship;
            }
        }
        return null;
    }

    private void handleDragAndDrop() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        if (Gdx.input.justTouched()) {
            startMouseX = mouseX;
            startMouseY = mouseY;
            isClick = true;
            log.debug("mouseX: {}, mouseY: {}", mouseX, mouseY);
            Ship clickedShip = getShipAt(mouseX, mouseY);
            if (clickedShip != null) {
                draggingShip = clickedShip;
                isDragging = true;
                List<Integer> coords = shipCoordinateMap.get(clickedShip);
                copyShip = new Ship(clickedShip.getX(), clickedShip.getY(), clickedShip.isHorizontal(), clickedShip.getType());
                backup.put(copyShip, new ArrayList<>(coords));
                log.debug("coords {}", coords);
                dragOffsetX = mouseX - coords.get(0);
                dragOffsetY = mouseY - coords.get(2);
                log.debug("Начали перетаскивание: {}", clickedShip);
            }
        }

        if (isDragging && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (Math.abs(mouseX - startMouseX) > 15 || Math.abs(mouseY - startMouseY) > 15) {
                isClick = false;
            }

            float newX = mouseX - dragOffsetX;
            float newY = mouseY - dragOffsetY;

            draggingShip.setX((int) newX);
            draggingShip.setY((int) newY);
            updateShipCoordinates(draggingShip);
        }

        if (isDragging && !Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (isClick) {
                if (mouseX > position.x && mouseX < position.x + 10 * 32
                    && mouseY > position.y && mouseY < position.y + 10 * 32) {

                    // ИСПОЛЬЗУЕМ СОХРАНЕННЫЕ КООРДИНАТЫ ИЗ BACKUP
                    List<Integer> backupCoords = backup.get(copyShip);
                    if (backupCoords != null && !backupCoords.isEmpty()) {
                        int backupX = backupCoords.get(0);
                        int backupY = backupCoords.get(2);

                        int cellX = (int) ((backupX - position.x) / 32);
                        int cellY = (int) ((backupY - position.y) / 32);

                        log.debug("coords from backup: {}, {}", cellX, cellY);
                        client.sendChangeOrientation(new Coordinate(cellX, cellY));
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        log.debug("ship111: {}", draggingShip);
                        if (client.isChangeOrientationSuccess()) {
                            draggingShip.setHorizontal(!draggingShip.isHorizontal());

                            // ВАЖНО: обновляем x и y у объекта Ship
                            draggingShip.setX(backupX);
                            draggingShip.setY(backupY);

                            // Обновляем координаты в карте на основе backup координат
                            List<Integer> newCoords = new ArrayList<>();
                            int size = draggingShip.getType().getSize();

                            if (draggingShip.isHorizontal()) {
                                newCoords.add(backupX);
                                newCoords.add(backupX + size * sizeDeck);
                                newCoords.add(backupY);
                                newCoords.add(backupY + sizeDeck);
                            } else {
                                newCoords.add(backupX);
                                newCoords.add(backupX + sizeDeck);
                                newCoords.add(backupY);
                                newCoords.add(backupY + size * sizeDeck);
                            }

                            shipCoordinateMap.put(draggingShip, newCoords);

                            // Также обновляем backup, чтобы при следующем клике использовались правильные координаты
                            backup.put(copyShip, new ArrayList<>(newCoords));

                            Board newBoard = client.getBoardCreator();
                            if (newBoard != null) {
                                boardPlayer1 = newBoard;
                            }
                        } else {
                            // ЕСЛИ ОРИЕНТАЦИЯ НЕ ИЗМЕНИЛАСЬ - ВОССТАНАВЛИВАЕМ ИСХОДНОЕ СОСТОЯНИЕ
                            log.debug("CHANGE_ORIENTATION_FAIL - восстанавливаем исходное состояние");

                            // Восстанавливаем координаты из backup (оригинальные, до попытки смены ориентации)
                            List<Integer> originalCoords = backup.get(copyShip);
                            if (originalCoords != null && !originalCoords.isEmpty()) {
                                int origX = originalCoords.get(0);
                                int origY = originalCoords.get(2);

                                // Восстанавливаем позицию корабля
                                draggingShip.setX(origX);
                                draggingShip.setY(origY);
                                // Ориентацию НЕ МЕНЯЕМ - оставляем как была

                                // Восстанавливаем карту координат
                                shipCoordinateMap.put(draggingShip, new ArrayList<>(originalCoords));
                            }

                            // Получаем обновленную доску от сервера (она не изменилась, но обновим для синхронизации)
                            Board newBoard = client.getBoardCreator();
                            if (newBoard != null) {
                                boardPlayer1 = newBoard;
                            }
                        }
                        log.debug("ship111: {}", draggingShip);
                        log.debug("shipCoordinateMap: {}", shipCoordinateMap);
                    }
                }
            } else {
                int firstX = draggingShip.getX();
                int firstY = draggingShip.getY();
                log.debug("firstX: " + firstX + " firstY: " + firstY);

                int cellX = (int) ((firstX - position.x) / 32);
                int cellY = (int) ((firstY - position.y) / 32);
                log.debug("cellX: {} cellY: {}", cellX, cellY);

                List<Integer> list1 = backup.get(draggingShip);
                List<Integer> list2 = shipCoordinateMap.get(draggingShip);

                log.debug("list1: {}", list1);
                log.debug("list2: {}", list2);

                Coordinate oldCoordinate = null;
                if (list1 != null && !list1.isEmpty()) {
                    int oldX = list1.get(0);
                    int oldY = list1.get(2);

                    int cellOldX = (int) ((oldX - position.x) / 32);
                    int cellOldY = (int) ((oldY - position.y) / 32);
                    oldCoordinate = new Coordinate(cellOldX, cellOldY);
                }

                client.sendPlaceShip(new Coordinate(cellX, cellY), oldCoordinate, draggingShip.getType(), draggingShip.isHorizontal());

                isDragging = false;

                for (Map.Entry<Ship, List<Integer>> entry : shipCoordinateMap.entrySet()) {
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (!client.isPlaceShipSuccess()) {
                    List<Integer> copyListCoords = backup.get(copyShip);
                    draggingShip.setX(copyListCoords.get(0));
                    draggingShip.setY(copyListCoords.get(2));
                    shipCoordinateMap.put(draggingShip, backup.get(copyShip));
                } else {
                    updateActualCoordinates(draggingShip);
                    backup.remove(copyShip);
                    backup.put(draggingShip, shipCoordinateMap.get(draggingShip));
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                for (Map.Entry<Ship, List<Integer>> entry : shipCoordinateMap.entrySet()) {
                    log.debug("{} : {}", entry.getKey(), entry.getValue());
                }
                log.debug("ship111: {}", shipCoordinateMap.get(draggingShip));
            }
            isDragging = false;
            isClick = false;
        }
    }


    private void updateActualCoordinates(Ship ship) {
        log.debug("начало метода updateActualCoordinates");
        List<Integer> coords = shipCoordinateMap.get(ship);
        shipCoordinateMap.remove(draggingShip);
        List<Integer> newCoords = new ArrayList<>();
        log.debug("координаты до обновления {}", coords);

        int startX = coords.get(0);
        int endX;
        int startY = coords.get(2);
        int endY;

        if (startX <= 50) {
            startX = 50;
        } else {
            startX = ((startX - 50) / 32) * 32 + 50;
        }

        if (startY <= 50) {
            startY = 50;
        } else {
            startY = ((startY - 50) / 32) * 32 + 50;
        }

        int sizeShip = ship.getType().getSize();

        if (ship.isHorizontal()) {
            endX = sizeShip * sizeDeck + startX;
            endY = sizeDeck + startY;
        } else {
            endY = sizeShip * sizeDeck + startY;
            endX = sizeDeck + startX;
        }

        ship.setX(startX);
        ship.setY(startY);

        newCoords.add(startX);
        newCoords.add(endX);
        newCoords.add(startY);
        newCoords.add(endY);
        log.debug("Координаты после ообновления: {}", newCoords);
        shipCoordinateMap.put(ship, newCoords);

    }

    private void updateShipCoordinates(Ship ship) {
        List<Integer> coords = new ArrayList<>();
        coords.add(ship.getX());
        coords.add(ship.getX() + ship.getType().getSize() * sizeDeck);
        coords.add(ship.getY());
        coords.add(ship.getY() + sizeDeck);
        shipCoordinateMap.put(ship, coords);
    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (font != null) {
            font.dispose();
            font = null;
        }
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        if (cellEmpty != null) {
            cellEmpty.dispose();
            cellEmpty = null;
        }
        if (cellShip != null) {
            cellShip.dispose();
            cellShip = null;
        }
        if (cellHalo != null) {
            cellHalo.dispose();
            cellHalo = null;
        }
    }
}
