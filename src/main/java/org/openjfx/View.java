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

import java.io.*;
import java.util.Random;

import static java.lang.Long.max;
import static java.lang.Long.min;

public class View extends VBox {
    private final Canvas canvas;
    private float dx;
    private float dy;
    private boolean specialLabel;
    public Board board;
    private boolean simMode;
    private final Loop mainLoop;
    private final Toolbar toolbar;

    class Toolbar extends ToolBar {
        private final TextInputDialog inputDialog;
        private final Button pause;
        private final Label state;
        private final Random random;

        public Toolbar() {
            this.state = new Label("editing");
            this.random = new Random();
            Button step = new Button("step (ctrl)");
            step.setOnAction(actionEvent -> {
                setMode(false);
                board.step();
                draw();
            });
            this.pause = new Button("play (A)");
            this.pause.setOnAction(actionEvent -> setMode(!simMode));
            Button randomize = new Button("random");
            randomize.setOnAction(actionEvent -> {
                setMode(false);
                int rx = board.getX(), ry = board.getY();
                for (int i = 0; i < rx; ++i)
                    for (int j = 0; j < ry; ++j)
                        board.set(i, j, random.nextInt() % 2);
                draw();
            });
            this.inputDialog = new TextInputDialog();
            Button save = new Button("save");
            save.setOnAction(actionEvent -> {
                setMode(false);
                inputDialog.setTitle("Save");
                inputDialog.setHeaderText("File name:");
                inputDialog.showAndWait();
                String name=inputDialog.getEditor().getText();
                name=name.replaceAll("[^A-Za-z0-9]","");
                specialLabel=true;
                this.state.setText("Saving to: "+name);
                try {
                    FileOutputStream saveFile=new FileOutputStream(name);
                    System.out.println(board.getX()+" "+board.getY());
                    saveFile.write(board.getX()/256);
                    saveFile.write(board.getX());
                    saveFile.write(board.getY()/256);
                    saveFile.write(board.getY());
                    for(int i=0;i<board.getX();++i)
                        for(int j=0;j<board.getY();++j)
                            saveFile.write(board.getState(i,j));
                    saveFile.close();
                } catch (Exception e) {
                    state.setText("An error Occurred");
                    e.printStackTrace();
                }
                inputDialog.getEditor().clear();
            });
            Button load = new Button("load");
            load.setOnAction(actionEvent -> {
                setMode(false);
                inputDialog.setTitle("Load");
                inputDialog.setHeaderText("File name:");
                inputDialog.showAndWait();
                String name=inputDialog.getEditor().getText();
                name=name.replaceAll("[^A-Za-z0-9]","");
                specialLabel=true;
                this.state.setText("Reading from: "+name);
                inputDialog.getEditor().clear();
                try {
                    FileInputStream loadFile=new FileInputStream(name);
                    int tmpX=loadFile.read();
                    tmpX=tmpX*256+loadFile.read();
                    int tmpY=loadFile.read();
                    tmpY=tmpY*256+loadFile.read();
                    Board tmpB=new Board(tmpX,tmpY);
                    System.out.println(tmpX+" "+tmpY);
                    for(int i=0;i<tmpX;++i) {
                        for (int j = 0; j < tmpY; ++j) {
                            tmpB.set(i,j,loadFile.read());
                        }
                    }
                    loadFile.close();
                    board=tmpB;
                    dx = Constants.width /(float) board.getX();
                    dy = Constants.height /(float) board.getY();
                    draw();
                } catch (Exception e) {
                    state.setText("An error Occurred");
                    e.printStackTrace();
                }
            });
            Button clear = new Button("clear");
            clear.setOnAction(actionEvent -> {
                setMode(false);
                board.clear();
                draw();
            });
            Button reset = new Button("reset");
            reset.setOnAction(actionEvent -> {
                setMode(false);
                inputDialog.setTitle("Reset");
                inputDialog.setHeaderText("New cell size (default=" + Constants.cellSize + ')');
                inputDialog.showAndWait();
                long newCellSize = -1;
                try {
                    newCellSize = Long.parseLong(inputDialog.getEditor().getText());
                } catch (Exception e) {
                    System.out.println("Can't parse " + inputDialog.getEditor().getText());
                } finally {
                    inputDialog.getEditor().clear();
                }
                if (newCellSize >= 0) {
                    newCellSize=min(max(5,newCellSize),max(Constants.width,Constants.height));
                    board = new Board((int) max(Constants.width / newCellSize, 1), (int) max(Constants.height / newCellSize, 1));
                    dx = Constants.width /(float) board.getX();
                    dy = Constants.height /(float) board.getY();
                }
                draw();
            });
            this.getItems().addAll( this.pause, step, clear, randomize, reset, save, load, this.state);

        }
    }

    public View() {
        this.simMode = false;
        this.specialLabel=false;
        this.canvas = new Canvas(Constants.width, Constants.height);
        this.canvas.setOnMouseClicked(this::drawHandler);
        this.canvas.setOnMouseDragged(this::drawHandler);

        toolbar = new Toolbar();
        this.board = new Board();
        this.dx = Constants.width /(float) board.getX();
        this.dy = Constants.height /(float) board.getY();

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
                this.toolbar.pause.setText("pause (A)");
                this.mainLoop.start();
            } else {
                this.toolbar.state.setText("editing");
                this.toolbar.pause.setText("play (A)");
                this.mainLoop.stop();
            }
            specialLabel = false;
        }else if(specialLabel)
            if(simMode)
                this.toolbar.state.setText("simulating");
            else
                this.toolbar.state.setText("editing");
    }
}