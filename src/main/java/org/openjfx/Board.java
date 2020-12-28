package org.openjfx;

public class Board {
    private class cell{
        short tmp=0;
        short state=0;
        void update(){
            this.state=this.tmp;
            this.tmp=0;
        }
    }
    private int x;
    private int y;
    cell tab[][];
    Board(){
        this.x=Constants.cols;
        this.y=Constants.rows;
        tab=new cell[this.x][this.y];
        for (int i=0;i<this.x;++i)
            for (int j=0;j<this.y;++j)
                tab[i][j]=new cell();
    }
    void set(int x,int y,int st){
        if(0<=x && x<this.x && 0<=y && y<this.y)
            tab[x][y].tmp=(short) st;
    }
    short getState(int xx,int yy){
        if(0<=xx && xx<this.x && 0<=yy && yy<this.y)
            return tab[xx][yy].state;
        else
            return 0;
    }
    void prepare(){
        for(int i=0;i<this.x;++i)
            for(int j=0;j<this.y;++j)
                check(i,j);
    }
    void update(){
        for(int i=0;i<this.x;++i)
            for(int j=0;j<this.y;++j)
                tab[i][j].update();
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
            set(x,y,1);
        }
    }
    Board step(){
        this.prepare();
        this.update();
        return this;
    }
}
