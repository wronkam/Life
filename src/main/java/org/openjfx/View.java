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

import static java.lang.Thread.sleep;

public class View extends VBox {
    private Canvas canvas;
    private float dx;
    private float dy;
    public Board board;
    class Toolbar extends ToolBar{
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
        public Toolbar(){
            this.random=new Random();
            this.step=new Button("step");
            this.step.setOnAction(actionEvent -> {
                board.step();
                draw();
            });
            this.pause=new Button("pause");
            this.randomize=new Button("random");
            this.randomize.setOnAction(actionEvent -> {
                int rx=board.getX(),ry=board.getY();
                for(int i=0;i<rx;++i)
                    for (int j = 0; j < ry; ++j)
                        board.set(i, j, random.nextInt() % 2);
                draw();
            });
            this.save=new Button("save");
            this.load=new Button("load");
            this.clear=new Button("clear");
            this.clear.setOnAction(actionEvent -> {
                board.clear();
                draw();
            });
            this.reset=new Button("reset");
            this.resetOptions=new TextInputDialog();
            this.reset.setOnAction(actionEvent ->{
                resetOptions.showAndWait();
                int newCellSize=-1;
                try{
                    newCellSize=Integer.parseInt(resetOptions.getEditor().getText());
                }catch (Exception e){
                    System.out.println("Can't parse "+resetOptions.getEditor().getText());
                }finally {
                    resetOptions.getEditor().clear();
                }
                if(newCellSize>=5){
                    board=new Board(Constants.width/newCellSize,Constants.height/newCellSize);
                    dx=Constants.width/board.getX();
                    dy=Constants.height/board.getY();
                }
                draw();
            });
            resetOptions.setTitle("Reset");
            resetOptions.setHeaderText("New cell size (default="+Constants.cellSize+')');
            this.state=new Label("editing");
            this.getItems().addAll(this.step,this.pause,this.clear,this.randomize,this.reset,this.save,this.load,this.state);

        }
    }
    public View(){
        this.canvas=new Canvas(Constants.width,Constants.height);
        this.canvas.setOnMouseClicked(this::drawHandler);
        this.canvas.setOnMouseDragged(this::drawHandler);

        Toolbar toolbar=new Toolbar();
        this.board=new Board();
        this.dx=Constants.width/board.getX();
        this.dy=Constants.height/board.getY();

        this.setOnKeyPressed(this::simulate);

        this.getChildren().addAll(this.canvas,toolbar);
    }

    private void simulate(KeyEvent keyEvent) {
        if(keyEvent.getCode()== KeyCode.CONTROL){
            this.board.step();
            draw();
        }
    }

    private void drawHandler(MouseEvent mouseEvent) {
        int cellX= (int) (mouseEvent.getX()/dx);
        int cellY= (int) (mouseEvent.getY()/dy);
        System.out.println(cellX+" : "+cellY);
        int st=-1;
        if(mouseEvent.getButton()== MouseButton.PRIMARY){
            st=1;
        }else if(mouseEvent.getButton()== MouseButton.SECONDARY){
            st=0;
        }
        if(st>=0) {
            this.board.set(cellX, cellY, st);
            drawSingle(cellX, cellY, st);
        }
    }
    public void drawSingle(int cellX,int cellY,int st){
        GraphicsContext c=this.canvas.getGraphicsContext2D();
        if(st==0)
            c.setFill(Color.BLACK);
        else
            c.setFill(Color.DARKGRAY);
        c.fillRect(cellX*dx+8*c.getLineWidth(),cellY*dy+8*c.getLineWidth(),dx-16*c.getLineWidth(),dy-16*c.getLineWidth());
    }

    public void draw(){
        GraphicsContext c=this.canvas.getGraphicsContext2D();
        c.setFill(Color.BLACK);
        c.fillRect(0,0,Constants.width,Constants.height);

        c.setFill(Color.DARKGRAY);

        c.setLineWidth(0.2);
        for(int x=0;x<this.board.getX();++x){
            for(int y=0;y<this.board.getY();++y){
               if(this.board.getState(x,y)==1) {
                   c.fillRect(x*dx+8*c.getLineWidth(),y*dy+8*c.getLineWidth(),dx-16*c.getLineWidth(),dy-16*c.getLineWidth());
               }
            }
        }

        c.setStroke(Color.WHITE);
        for(int x=0;x<=this.board.getX();++x){
            c.strokeLine(x*dx,0,x*dx,Constants.height);
        }
        for(int y=0;y<=this.board.getY();++y){
            c.strokeLine(0,y*dy,Constants.width,y*dy);
        }
    }
}
