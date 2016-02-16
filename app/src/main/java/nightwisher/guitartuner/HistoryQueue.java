package nightwisher.guitartuner;

import java.util.ArrayList;
import java.util.List;

public class HistoryQueue<T> {
    private List<T> list;
    private int maxSize;
    public HistoryQueue(int maxSize){
        this.maxSize = maxSize;
        list = new ArrayList<>();
    }
    public void push(T item){
        if(list.size()==maxSize){
            list.remove(0);
        }
        list.add(item);
    }
    public T getItem(int index){
        return list.get(index);
    }

    public T pop(){
        if(list.size()==0){
            return null;
        }else{
            return list.remove(0);
        }
    }

    public int size(){
        return list.size();
    }
}