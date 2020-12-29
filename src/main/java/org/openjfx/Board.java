package org.openjfx;


import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Integer.min;

public class Board {

    private static class cell {
        short tmp=0;
        short state=0;
        void update(){
            this.state=this.tmp;
            this.tmp=0;
        }
    }

    private final int x;
    private final int y;
    int getX(){
        return x;
    }
    int getY(){
        return y;
    }
    cell[][] tab;
    ExecutorService executorService;
    Collection<Callable<Void>> prepareAll;
    Collection<Callable<Void>> updateAll;
    Board(int cols, int rows){
        this.x=cols;
        this.y=rows;
        tab=new cell[this.x][this.y];
        executorService= Executors.newFixedThreadPool(Constants.threads);
        prepareAll=new ArrayList<>();
        updateAll=new ArrayList<>();
        for (int i=0;i<this.x;++i)
            for (int j=0;j<this.y;++j)
                tab[i][j]= new cell();
        int assignment = this.x / Constants.threads + 1;
        for(int i=0;i<this.x;i+= assignment){
            int x1= i;
            int x2=min(i+ assignment,this.x);
            prepareAll.add(()->prepare(x1,x2,y));
            updateAll.add(()->update(x1,x2,y));
        }
    }
    Board(){
        this(Constants.cols, Constants.rows);
    }
    Void prepare(int x1,int x2,int yy){
        for(int i=x1;i<x2;++i)
            for(int j=0;j<yy;++j)
                check(i,j);
        return null;
    }
    Void update(int x1,int x2,int yy){
        for(int i=x1;i<x2;++i)
            for(int j=0;j<yy;++j)
                tab[i][j].update();
        return null;
    }
    void set(int x,int y,int st){
        if(0<=x && x<this.x && 0<=y && y<this.y) {
            tab[x][y].state = (short) st;
            tab[x][y].tmp=(short) 0;
        }
    }
    void preset(int x,int y){
        if(0<=x && x<this.x && 0<=y && y<this.y)
            tab[x][y].tmp=1;
    }
    short getState(int xx,int yy){
        if(0<=xx && xx<this.x && 0<=yy && yy<this.y)
            return tab[xx][yy].state;
        else
            return 0;
    }
    void prepare(){
        try {
            executorService.invokeAll(prepareAll);
        } catch (InterruptedException e) {
            e.printStackTrace();
            executorService.shutdownNow();
        }
    }
    void update(){
        try {
            executorService.invokeAll(updateAll);
        } catch (InterruptedException e) {
            e.printStackTrace();
            executorService.shutdownNow();
        }
    }
    void check(int x,int y){
        int count=0;
        count+=  getState(x-1,y-1)
                +getState(x,y-1)
                +getState(x+1,y-1)
                +getState(x-1,y)
                +getState(x+1,y)
                +getState(x-1,y+1)
                +getState(x,y+1)
                +getState(x+1,y+1);
        if(count==3 || (getState(x,y)==1 && count==2)){
            preset(x,y);
        }
    }
    void clear(){
        for(int i=0;i<this.x;++i)
            for(int j=0;j<this.y;++j)
                set(i,j,0);
    }
    void step(){
        this.prepare();
        this.update();
    }
}
