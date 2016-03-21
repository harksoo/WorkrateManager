package com.sovate.workratemanager.common;

import java.util.ArrayList;

/**
 * Created by harksoo on 2016-03-21.
 */
public class BatchWorkList extends ArrayList<String> {

    int postion = -1;
    boolean complete = false;

    public int getPostion() {
        return postion;
    }

    public void setPostion(int postion) {
        this.postion = postion;
    }

    public void init(){
        if(this.size() > 0){
            postion = 0;
        } else {
            postion = -1;
        }
    }

    public String GetNextItem(){
        if(this.size() > 0 && postion >= 0){
            return this.get(postion++);
        }

        // 초기화 : 순환 구조
        init();

        return null;
    }

    public void Clear(){
        this.clear();
        postion = -1;
    }
}
