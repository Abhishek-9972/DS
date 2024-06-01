package DS.LinkedList.a17DesignCircularDeque;

import java.util.ArrayList;
import java.util.List;

class MyCircularDeque {

    List<Integer> list;
    int max;

    public MyCircularDeque(int k) {
        list = new ArrayList<>();
        max =k;
    }
    
    public boolean insertFront(int value) {
        if(isFull()){return false;}
        else{list.add(0,value); return true;}
    }
    
    public boolean insertLast(int value) {
        if(isFull()){return false;}
        else{list.add(value);return true; }
    }
    
    public boolean deleteFront() {
        if(isEmpty()){return false;}
        else{list.remove(0);return true;}
    }
    
    public boolean deleteLast() {
        if(isEmpty()){return false;}
        else{list.remove(list.size()-1);return true;}
    }
    
    public int getFront() {
        if(isEmpty()){return -1;}
        else{return list.get(0);}
    }
    
    public int getRear() {
        if(isEmpty()){return -1;}
        else{return list.get(list.size()-1);}
    }
    
    public boolean isEmpty() {
        if(list.size()==0){
            return true;
        }else{
            return false;
        }
    }
    
    public boolean isFull() {
        if(list.size()==max){
            return true;
        }else{
            return false;
        }
    }
}

/**
 * Your MyCircularDeque object will be instantiated and called as such:
 * MyCircularDeque obj = new MyCircularDeque(k);
 * boolean param_1 = obj.insertFront(value);
 * boolean param_2 = obj.insertLast(value);
 * boolean param_3 = obj.deleteFront();
 * boolean param_4 = obj.deleteLast();
 * int param_5 = obj.getFront();
 * int param_6 = obj.getRear();
 * boolean param_7 = obj.isEmpty();
 * boolean param_8 = obj.isFull();
 */