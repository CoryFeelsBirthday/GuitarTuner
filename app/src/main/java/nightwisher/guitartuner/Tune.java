package nightwisher.guitartuner;

import android.support.annotation.NonNull;

public class Tune implements Comparable<Tune>{

    private String name;
    private int[] noteIndex;

    public Tune(String name, int[] noteIndex){
        this.name = name;
        this.noteIndex = noteIndex;
    }

    @Override
    public int compareTo(@NonNull Tune tune){
        return this.name.compareTo(tune.name);
    }

    public void setName(String name){
        this.name = name;
    }

    public void setNoteIndex(int[] noteIndex){
        this.noteIndex = noteIndex;
    }

    public String getName(){
        return name;
    }

    public int[] getNoteIndex(){
        return noteIndex;
    }
}
