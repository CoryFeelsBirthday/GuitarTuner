package nightwisher.guitartuner;

import android.content.Context;

import java.util.HashSet;

public class PreDefinedTunes {
    private static Tune[] tunes;
    private static int[][] tuneIndexes = new int[][]{{4,9,14,19,23,28},{2,9,14,19,23,28},{2,9,14,18,21,26}};
    private static String[] tuneNameResStrings = new String[]{"standard_tune","drop_d","open_d"};
    private static HashSet<String> tuneNames;
    public static Tune[] getTunes(Context context) {
        if (tunes == null) {
            tunes = new Tune[tuneNameResStrings.length];
            for(int i=0;i<tuneNameResStrings.length;i++){
                String packageName = context.getPackageName();
                tunes[i] = new Tune(
                        context.getString(
                                context.getResources().getIdentifier(tuneNameResStrings[i],
                                        "string",
                                        packageName)),
                        tuneIndexes[i]);
            }
        }
        return tunes;
    }
    public static HashSet<String> getTuneNames(Context context){
        if(tuneNames == null){
            tuneNames = new HashSet<>();
            for(String tuneName:tuneNameResStrings){
                String packageName = context.getPackageName();
                tuneNames.add(context.getString(context.getResources().getIdentifier(
                        tuneName,
                        "string",
                        packageName)));
            }
        }
        return tuneNames;
    }
}
