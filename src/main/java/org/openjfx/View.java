package org.openjfx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Random;

import static java.lang.Long.max;
import static java.lang.Long.min;

public class View extends VBox {
    private Canvas canvas;
    private float dx;
    private float dy;
    public Board board;
    private boolean simMode;
    private Loop mainLoop;
    private Toolbar toolbar;

    class Toolbar extends ToolBar {
        private TextInputDialog resetOptions;
        private Button step;
        private Button pause;
        private Button randomize;
        private Button save;
        private Button load;
        private Button reset;
        private Button clear;
        private Label state;
        private Random random;

        public Toolbar() {
            this.random = new Random();
            this.step = new Button("step");
            this.step.setOnAction(actionEvent -> {
                setMode(false);
                board.step();
                draw();
            });
            this.pause = new Button("play");
            this.pause.setOnAction(actionEvent -> setMode(!simMode));
            this.randomize = new Button("random");
            this.randomize.setOnAction(actionEvent -> {
                setMode(false);
                int rx = board.getX(), ry = board.getY();
                for (int i = 0; i < rx; ++i)
                    for (int j = 0; j < ry; ++j)
                        board.set(i, j, random.nextInt() % 2);
                draw();
            });
            this.save = new Button("save");
            this.load = new Button("load");
            this.clear = new Button("clear");
            this.clear.setOnAction(actionEvent -> {
                setMode(false);
                board.clear();
                draw();
            });
            this.reset = new Button("reset");
            this.resetOptions = new TextInputDialog();
            this.reset.setOnAction(actionEvent -> {
                setMode(false);
                resetOptions.showAndWait();
                long newCellSize = -1;
                try {
                    newCellSize = Long.parseLong(resetOptions.getEditor().getText());
                } catch (Exception e) {
                    System.out.println("Can't parse " + resetOptions.getEditor().getText());
                } finally {
                    resetOptions.getEditor().clear();
                }
                if (newCellSize >= 0) {
                    newCellSize=min(max(5,newCellSize),max(Constants.width,Constants.height));
                    board = new Board((int) max(Constants.width / newCellSize, 1), (int) max(Constants.height / newCellSize, 1));
                    dx = Constants.width / board.getX();
                    dy = Constants.height / board.getY();
                }
                draw();
            });
            resetOptions.setTitle("Reset");
            resetOptions.setHeaderText("New cell size (default=" + Constants.cellSize + ')');
            this.state = new Label("editing");
            this.getItems().addAll( this.pause,this.step, this.clear, this.randomize, this.reset, this.save, this.load, this.state);

        }
    }

    public View() {
        this.simMode = false;
        this.canvas = new Canvas(Constants.width, Constants.height);
        this.canvas.setOnMouseClicked(this::drawHandler);
        this.canvas.setOnMouseDragged(this::drawHandler);

        toolbar = new Toolbar();
        this.board = new Board();
        this.dx = Constants.width / board.getX();
        this.dy = Constants.height / board.getY();

        this.setOnKeyPressed(this::keyHandler);

        this.getChildren().addAll(this.canvas, toolbar);

        this.mainLoop = new Loop(this);
    }

    private void keyHandler(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.CONTROL) {
            this.setMode(false);
            this.board.step();
            draw();
        }else if(keyEvent.getCode() == KeyCode.A){
            this.setMode(!simMode);
        }
    }

    private void drawHandler(MouseEvent mouseEvent) {
        this.setMode(false);
        int cellX = (int) (mouseEvent.getX() / dx);
        int cellY = (int) (mouseEvent.getY() / dy);
        int st = -1;
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            st = 1;
        } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            st = 0;
        }
        if (st >= 0) {
            this.board.set(cellX, cellY, st);
            drawSingle(cellX, cellY, st);
        }
    }

    public void drawSingle(int cellX, int cellY, int st) {
        GraphicsContext c = this.canvas.getGraphicsContext2D();
        if (st == 0)
            c.setFill(Color.BLACK);
        else
            c.setFill(Color.DARKGRAY);
        c.fillRect(cellX * dx + 8 * c.getLineWidth(), cellY * dy + 8 * c.getLineWidth(), dx - 16 * c.getLineWidth(), dy - 16 * c.getLineWidth());
    }

    public void draw() {
        GraphicsContext c = this.canvas.getGraphicsContext2D();
        c.setFill(Color.BLACK);
        c.fillRect(0, 0, Constants.width, Constants.height);

        c.setFill(Color.DARKGRAY);

        c.setLineWidth(0.2);
        for (int x = 0; x < this.board.getX(); ++x) {
            for (int y = 0; y < this.board.getY(); ++y) {
                if (this.board.getState(x, y) == 1) {
                    c.fillRect(x * dx + 8 * c.getLineWidth(), y * dy + 8 * c.getLineWidth(), dx - 16 * c.getLineWidth(), dy - 16 * c.getLineWidth());
                }
            }
        }

        c.setStroke(Color.WHITE);
        for (int x = 0; x <= this.board.getX(); ++x) {
            c.strokeLine(x * dx, 0, x * dx, Constants.height);
        }
        for (int y = 0; y <= this.board.getY(); ++y) {
            c.strokeLine(0, y * dy, Constants.width, y * dy);
        }
    }

    private void setMode(boolean st) {
        if (st != simMode) {
            simMode = st;
            if (simMode) {
                this.toolbar.state.setText("simulating");
                this.toolbar.pause.setText("pause");
                this.mainLoop.start();
            } else {
                this.toolbar.state.setText("editing");
                this.toolbar.pause.setText("play");
                this.mainLoop.stop();
            }
        }
    }
}