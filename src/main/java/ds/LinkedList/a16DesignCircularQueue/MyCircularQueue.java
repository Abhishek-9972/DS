package ds.LinkedList.a16DesignCircularQueue;

import java.util.ArrayList;

class MyCircularQueue {

    ArrayList<Integer> list;
    int max;
    public MyCircularQueue(int k) {
        list=new ArrayList<>();
        max=k;
    }
    
    public boolean enQueue(int value) {
        if(list.size()==max) return false;
        list.add(value);
        return true;
    }
    
    public boolean deQueue() {
        if(list.size()==0) return false;
        list.remove(0);
        return true;
    }
    
    public int Front() {
        if(list.size()==0) return -1;
        return list.get(0);
    }
    
    public int Rear() {
        if(list.size()==0) return -1;
        return list.get(list.size()-1);
    }
    
    public boolean isEmpty() {
        return list.size()==0;
    }
    
    public boolean isFull() {
        return list.size()==max;
    }
}

/**
 * Your MyCircularQueue object will be instantiated and called as such:
 * MyCircularQueue obj = new MyCircularQueue(k);
 * boolean param_1 = obj.enQueue(value);
 * boolean param_2 = obj.deQueue();
 * int param_3 = obj.Front();
 * int param_4 = obj.Rear();
 * boolean param_5 = obj.isEmpty();
 * boolean param_6 = obj.isFull();
 */