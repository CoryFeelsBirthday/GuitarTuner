package nightwisher.guitartuner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class TuneSelect extends AppCompatActivity{
    public final static int STATUS_SELECT = 1;
    public final static int STATUS_EDIT = 2;
    public final static int STATUS_DELETE = 3;
    public final static int RESULT_CODE = 1;
    public final static int CANCELED_CODE = 2;
    public final static int ADD_ACTION_CODE = 1;
    public final static int EDIT_ACTION_CODE = 2;
    final static int ADD_NEW_TUNE_CODE = 1;
    final static int Edit_TUNE_CODE = 2;
    final static int TUNE_SELECTED_CODE = 3;
    final static String ACTION_STRING = "action";
    private List<Tune> tunes = new ArrayList<>();
    private TuneSelectAdapter adapter;
    private int tuneSelectStatus = STATUS_SELECT;
    private FloatingActionButton goBackBtn;

    public int getTuneSelectStatus(){
        return tuneSelectStatus;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tune_select);

        Toolbar toolbar = (Toolbar) findViewById(R.id.guitartuner_toolbar);
        setSupportActionBar(toolbar);

        goBackBtn = (FloatingActionButton) findViewById(R.id.go_back_btn);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tuneSelectStatus = STATUS_SELECT;
                goBackBtn.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }
        });

        tunes.addAll(Arrays.asList(PreDefinedTunes.getTunes(this)));
        SharedPreferences tunePreference = getSharedPreferences(GuitarTuner.TUNE_PREF_STRING,
                Context.MODE_PRIVATE);
        Map<String,?> prefMap = tunePreference.getAll();
        Gson gson = new Gson();
        for(Object tuneJson:prefMap.values()){
            tunes.add(gson.fromJson((String)tuneJson, Tune.class));
        }
        adapter = new TuneSelectAdapter(this,R.layout.tune_item,tunes);
        final ListView listView = (ListView) findViewById(R.id.tune_select_list);
        FloatingActionButton addNewTuneBtn = (FloatingActionButton) findViewById(R.id.add_new_tune);
        addNewTuneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TuneSelect.this, EditTune.class);
                intent.putExtra(ACTION_STRING, ADD_ACTION_CODE);
                startActivityForResult(intent, ADD_NEW_TUNE_CODE);
            }
        });
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            HashSet<String> predefinedTuneNames = PreDefinedTunes.getTuneNames(TuneSelect.this);
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Tune tune = adapter.getItem(position);
                if(tuneSelectStatus == STATUS_SELECT){
                    Intent intent = new Intent();
                    intent.putExtra(GuitarTuner.TUNE_NAME_STRING, tune.getName());
                    intent.putExtra(GuitarTuner.NOTE_INDEX_STRING, tune.getNoteIndex());
                    setResult(TuneSelect.TUNE_SELECTED_CODE, intent);
                    finish();
                }
                else if(tuneSelectStatus == STATUS_EDIT){

                    if(!predefinedTuneNames.contains(tune.getName())){
                        Intent intent = new Intent(TuneSelect.this,EditTune.class);
                        intent.putExtra(GuitarTuner.TUNE_NAME_STRING, tune.getName());
                        intent.putExtra(GuitarTuner.NOTE_INDEX_STRING, tune.getNoteIndex());
                        intent.putExtra(GuitarTuner.TUNE_POS_STRING, position);
                        intent.putExtra(ACTION_STRING, EDIT_ACTION_CODE);
                        startActivityForResult(intent, TuneSelect.Edit_TUNE_CODE);
                    }

                }
                else if(tuneSelectStatus == STATUS_DELETE){
                    if(!predefinedTuneNames.contains(tune.getName())){
                        AlertDialog.Builder builder = new AlertDialog.Builder(TuneSelect.this);
                        builder.setMessage(getString(R.string.delete_tune_confirm) +
                                tune.getName() + "?");
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Tune tune = adapter.getItem(position);
                                SharedPreferences tunePreference =
                                        getSharedPreferences(GuitarTuner.TUNE_PREF_STRING,
                                                Context.MODE_PRIVATE);
                                SharedPreferences.Editor tunePreferenceEditor = tunePreference.edit();
                                tunePreferenceEditor.remove(tune.getName());
                                tunePreferenceEditor.apply();
                                tunes.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        });
                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                        final AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Gson gson = new Gson();
        if(requestCode==ADD_NEW_TUNE_CODE&&resultCode==RESULT_CODE){
            Tune tune = new Tune(data.getStringExtra(GuitarTuner.TUNE_NAME_STRING),
                    data.getIntArrayExtra(GuitarTuner.NOTE_INDEX_STRING));
            tunes.add(tune);
            adapter.notifyDataSetChanged();

            SharedPreferences tunePreference = getSharedPreferences(GuitarTuner.TUNE_PREF_STRING, Context.MODE_PRIVATE);
            SharedPreferences.Editor tunePreferenceEditor = tunePreference.edit();

            tunePreferenceEditor.putString(tune.getName(), gson.toJson(tune));
            tunePreferenceEditor.apply();
        }
        else if(requestCode==Edit_TUNE_CODE&&resultCode==RESULT_CODE){
            String tuneName = data.getStringExtra(GuitarTuner.TUNE_NAME_STRING);
            int[] noteIndex = data.getIntArrayExtra(GuitarTuner.NOTE_INDEX_STRING);
            int tunePos = data.getIntExtra(GuitarTuner.TUNE_POS_STRING, -1);
            Tune tune = tunes.get(tunePos);

            SharedPreferences tunePreference = getSharedPreferences(GuitarTuner.TUNE_PREF_STRING, Context.MODE_PRIVATE);
            SharedPreferences.Editor tunePreferenceEditor = tunePreference.edit();
            tunePreferenceEditor.remove(tune.getName());

            tune.setName(tuneName);
            tune.setNoteIndex(noteIndex);

            tunePreferenceEditor.putString(tuneName, gson.toJson(tune));
            tunePreferenceEditor.apply();

            adapter.notifyDataSetChanged();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tune_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.tune_select_edit){
            tuneSelectStatus = STATUS_EDIT;
            goBackBtn.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
        if (id == R.id.tune_select_delete){
            tuneSelectStatus = STATUS_DELETE;
            goBackBtn.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }
}

class TuneSelectAdapter extends ArrayAdapter<Tune>{

    private TuneSelect tuneSelectActivity;

    private String[] intArrayToNoteArray(int[] input){
        String[] output = new String[input.length];
        for(int i=0;i<input.length;i++){
            output[i] = GuitarTuner.noteArray[input[i]];
        }
        return output;
    }

    public TuneSelectAdapter(Context context, int resource, List<Tune> items) {
        super(context, resource, items);
        tuneSelectActivity = (TuneSelect) context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View tuneItemView = convertView;

        if (tuneItemView == null) {
            LayoutInflater inflater;
            inflater = LayoutInflater.from(getContext());
            tuneItemView = inflater.inflate(R.layout.tune_item, null);
        }

        final Tune tune = getItem(position);

        HashSet<String> predefinedTuneNames = PreDefinedTunes.getTuneNames(tuneSelectActivity);


        ImageView tuneIcon = (ImageView) tuneItemView.findViewById(R.id.tune_icon);
        if(tuneSelectActivity.getTuneSelectStatus() == TuneSelect.STATUS_SELECT){
            tuneIcon.setVisibility(View.GONE);
        }
        else if(tuneSelectActivity.getTuneSelectStatus() == TuneSelect.STATUS_EDIT){
            if(!predefinedTuneNames.contains(tune.getName())){
                tuneIcon.setImageResource(R.drawable.edit_icon);
                tuneIcon.setVisibility(View.VISIBLE);
            }

        }
        else if(tuneSelectActivity.getTuneSelectStatus() == TuneSelect.STATUS_DELETE){
            if(!predefinedTuneNames.contains(tune.getName())){
                tuneIcon.setImageResource(R.drawable.delete_icon);
                tuneIcon.setVisibility(View.VISIBLE);
            }
        }
        TextView tuneName = (TextView) tuneItemView.findViewById(R.id.tuneItem_tuneName);
        tuneName.setText(tune.getName());
        TextView tuneContent = (TextView) tuneItemView.findViewById(R.id.tuneItem_tuneContent);
        tuneContent.setText(TextUtils.join(", ", intArrayToNoteArray(tune.getNoteIndex())));

        return tuneItemView;
    }

}



