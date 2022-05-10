package org.openjfx;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
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
    Collection<Callable<Void>> prepare2All;
    Collection<Callable<Void>> updateAll;
    public void stop(){
        executorService.shutdownNow();
    }
    Board(int cols, int rows){
        this.x=cols;
        this.y=rows;
        tab=new cell[this.x][this.y];
        executorService= Executors.newFixedThreadPool(Constants.threads);
        prepareAll=new ArrayList<>();
        prepare2All=new ArrayList<>();
        updateAll=new ArrayList<>();
        for (int i=0;i<this.x;++i)
            for (int j=0;j<this.y;++j)
                tab[i][j]= new cell();
        int assignment = this.x / Constants.threads + 1;
        for(int i=0;i<this.x;i+= assignment){
            int x1= i;
            int x2=min(i+ assignment,this.x);
            prepareAll.add(()->prepare(x1,x2,y));
            prepare2All.add(()->prepare2(x1,x2,y));
            updateAll.add(()->update(x1,x2,y));
        }
    }


    Board(){
        this(Constants.width/Constants.cellSize, Constants.height/Constants.cellSize);
    }
    Void prepare(int x1,int x2,int yy){
        for(int i=x1;i<x2;++i)
            for(int j=0;j<yy;++j)
                check(i,j);
        return null;
    }
    Void prepare2(int x1,int x2,int yy){
        for(int i=x1;i<x2;++i)
            for(int j=0;j<yy;++j)
                check2(i,j);
        return null;
    }
    Void update(int x1,int x2,int yy){
        for(int i=x1;i<x2;++i)
            for(int j=0;j<yy;++j)
                update(i,j);
        return null;
    }
    void set(int x,int y,int st){
        if(0<=x && x<this.x && 0<=y && y<this.y) {
            tab[x][y].state = (short) st;
            tab[x][y].tmp=(short) 0;
        }
    }
    void preset(int x,int y,short val){
        if(0<=x && x<this.x && 0<=y && y<this.y)
            tab[x][y].state=val;
    }
    boolean isCell(int x,int y){
        return (0<=x && x<this.x && 0<=y && y<this.y);
    }
    void presetTMP(int x,int y,short val){
        if(0<=x && x<this.x && 0<=y && y<this.y)
            tab[x][y].tmp=val;
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
    void prepare2(){
        try {
            executorService.invokeAll(prepare2All);
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
        /*states
        0 - inactive
        1 - active
        2 - fading
        3 - emerging
         */
        int s[]={-1,1};
        int count=0;
        for(int k:s){
            if(getState(x+k,y)==1)
                count+=1;
            if(getState(x,y+k)==1)
                count+=1;
        }
        if(getState(x,y)==1){
            if(count<2){
                preset(x,y, (short) 2);
            }
        }else{
            if(count>=2){
                preset(x,y, (short) 3);
            }
        }
    }
    void check2(int x,int y){
        /*states
        0 - inactive
        1 - active
        2 - fading
        3 - emerging
         */
        int s[]={-1,1};
        int count=0;
        for(int k:s){
            if(getState(x+k,y)==1 || getState(x+k,y)==3 )
                count+=1;
            if(getState(x,y+k)==1 || getState(x+k,y+k)==3 )
                count+=1;
        }
        if(getState(x,y)==2){
            if(count<2){
                preset(x,y, (short) 0);
            }else {
                preset(x,y, (short) 1);
            }
        }
    }
    void update(int x,int y){
        int s[]={-1,1};
        int count=0;
        for(int k:s){
            if(getState(x+k,y)==1)
                count+=1;
            if(getState(x,y+k)==1)
                count+=1;
        }
        if(getState(x,y)==3){
            if(count>2){
                set(x,y, (short) 1);
            }else {
                set(x,y, (short) 0);
            }
        }else if(getState(x,y)==1){
            set(x,y,1);
        }else {
            set(x,y,0);
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


    void random(double r){
        Random random=new Random();
        for (int i = 1; i < x-1; ++i) {
            for (int j = 1; j < y-1; ++j) {
                if(random.nextDouble()<=r) {
                    set(i, j, 1);
                }else {
                    set(i, j, 0);
                }
            }
        }
    }

    int getFill(){
        int count=0;
        for (int i =0; i < x; ++i) {
            for (int j = 0; j < y; ++j) {
                if(getState(i,j)!=0)
                    count+=1;
            }
        }
        return (int) Math.round((double) count/(double)(x*y)*100);
    }
    void heuristic(){
        for (int i = 0; i < x; ++i) {
            for (int j = 0; j < y; ++j) {
                heuristic(i,j);
            }
        }
        for (int i = 0; i < x; ++i) {
            for (int j = 0; j < y; ++j) {
                tab[i][j].update();
            }
        }
    }

    private void heuristic(int x, int y) {
        int count=
                getState(x,y-1)*getState(x-1,y)
                +getState(x,y-1)*getState(x+1,y)
                +getState(x,y+1)*getState(x-1,y)
                +getState(x,y+1)*getState(x+1,y)
                +getState(x+1,y)*getState(x-1,y)
                +getState(x,y-1)*getState(x,y+1);
        if (count<2) {
            presetTMP(x, y, (short) 0);
        }else if(getState(x,y)==1){
            presetTMP(x, y, (short) 1);
        }
    }
    protected void dfs(){
        Boolean visited[][]= new Boolean[x][y];
        for (int i = 0; i < x; ++i) {
            for (int j = 0; j < y; ++j) {
                visited[i][j]=false;
            }
        }
        dfs(0,0,visited); //margin around board is always free
        for (int i = 0; i < x; ++i) {
            for (int j = 0; j < y; ++j) {
                if(!visited[i][j] && getState(i,j)==0)
                    set(i,j,1);
            }
        }
    }

    private void dfs(int x, int y,Boolean[][] vis) {
        int s[]={-1,1};
        if(isCell(x,y)) {
            vis[x][y]=true;
            for(int k:s){
                if(isCell(x+k,y) && getState(x+k,y)==0){
                    if(!vis[x+k][y])
                        dfs(x+k,y,vis);
                }
                if(isCell(x,y+k) && getState(x,y+k)==0){
                    if(!vis[x][y+k])
                        dfs(x,y+k,vis);
                }
            }
        }
    }

}
