package org.openjfx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.util.Duration;

public class Loop {
    private Timeline timeline;
    private View view;
    public Loop(View view){
        this.view=view;
        this.timeline=new Timeline(new KeyFrame(Duration.millis(Constants.initialDelay),this::doStep));
        this.timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void doStep(ActionEvent actionEvent) {
        this.view.board.step();
        this.view.draw();
    }
    public void start(){
        this.timeline.play();
    }
    public void stop(){
        this.timeline.stop();
    }
}
