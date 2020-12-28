package org.openjfx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class View extends VBox {
    private Canvas canvas;
    private float dx;
    private float dy;
    class Toolbar extends ToolBar{
        private Button step;
        private Button pause;
        private Button save;
        private Button load;
        public Toolbar(){
            this.step=new Button("step");
            this.pause=new Button("pause");
            this.save=new Button("save");
            this.load=new Button("load");
            this.getItems().addAll(this.step,this.pause,this.save,this.load);

        }
    }
    public View(){
        this.canvas=new Canvas(Constants.width,Constants.height);
        Toolbar toolbar=new Toolbar();


        this.dx=Constants.width/Constants.cols;
        this.dy=Constants.height/Constants.rows;
        this.getChildren().addAll(this.canvas,toolbar);
    }
    public void draw(){
        GraphicsContext c=this.canvas.getGraphicsContext2D();
        c.setFill(Color.BLACK);
        c.fillRect(0,0,Constants.width,Constants.height);

        c.setFill(Color.DARKGRAY);

        for(int x=0;x<Constants.cols;++x){
            for(int y=0;y<Constants.rows;++y){
               if((x+y)%2==0 && x%3==0 && y%5==0) {
                   System.out.println(x);
                   System.out.println(y);
                   System.out.println("--------------");
                   c.fillRect(x*dx,y*dy,dx,dy);
               }
            }
        }

        c.setStroke(Color.WHITE);
        c.setLineWidth(0.2);
        for(int x=0;x<=Constants.cols;++x){
            c.strokeLine(x*dx,0,x*dx,Constants.height);
        }
        for(int y=0;y<=Constants.rows;++y){
            c.strokeLine(0,y*dy,Constants.width,y*dy);
        }

    }
}
