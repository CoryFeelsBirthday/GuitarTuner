package nightwisher.guitartuner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class EditTune extends AppCompatActivity{
    private ViewGroup btnGroup;
    private int[] indexArray = {-1, -1, -1, -1, -1, -1};

    class NoteBtnOnclickListener implements View.OnClickListener{
        private int intentCode;

        public NoteBtnOnclickListener(int intentCode){
            super();
            this.intentCode = intentCode;
        }

        @Override
        public void onClick(View v){
            SpannableString str = new SpannableString(getString(R.string.pick_a_note));
            str.setSpan(new ForegroundColorSpan(Color.BLACK), 0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            AlertDialog.Builder builder = new AlertDialog.Builder(EditTune.this);
            builder.setTitle(str)
                    .setItems(GuitarTuner.noteArray, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ((Button) btnGroup.getChildAt(intentCode)).
                                    setText(GuitarTuner.noteArray[which]);
                            indexArray[intentCode] = which;
                        }
                    });
            final AlertDialog dialog = builder.create();
            ListView listView = dialog.getListView();
            listView.setDivider(getResources().getDrawable(R.color.gray));
            listView.setDividerHeight(1);
            int padding = (int)getResources().getDimension(R.dimen.activity_vertical_margin);
            listView.setPadding(padding,0,padding,0);
            dialog.show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tune);
        Toolbar toolbar = (Toolbar) findViewById(R.id.guitartuner_toolbar);
        setSupportActionBar(toolbar);

        btnGroup = (ViewGroup) findViewById(R.id.editNoteBtnLayout);

        final Bundle extras = getIntent().getExtras();
        final EditText tuneNameInput = (EditText) findViewById(R.id.tuneNameInput);
        final int actionCode = extras.getInt(TuneSelect.ACTION_STRING);

        int childCount = btnGroup.getChildCount();
        if(actionCode == TuneSelect.EDIT_ACTION_CODE){
            indexArray = extras.getIntArray(GuitarTuner.NOTE_INDEX_STRING);
            for (int i = 0; i < childCount; i++) {
                tuneNameInput.setText(extras.getString(GuitarTuner.TUNE_NAME_STRING));
                Button btn = (Button) btnGroup.getChildAt(i);
                if(indexArray[i]!=-1)
                    btn.setText(GuitarTuner.noteArray[indexArray[i]]);
            }
        }

        for (int i = 0; i < childCount; i++) {
            Button btn = (Button) btnGroup.getChildAt(i);
            btn.setOnClickListener(new NoteBtnOnclickListener(i));
        }

        Button editTuneBtn = (Button) findViewById(R.id.editTune_okBtn);
        editTuneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tuneNameString = tuneNameInput.getText().toString();
                if (TextUtils.isEmpty(tuneNameString)) {
                    Toast.makeText(EditTune.this, R.string.tune_name_empty_toast, Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences pref = getSharedPreferences(GuitarTuner.TUNE_PREF_STRING,
                        Context.MODE_PRIVATE);

                if (actionCode == TuneSelect.ADD_ACTION_CODE &&
                        (pref.contains(tuneNameString) ||
                        PreDefinedTunes.getTuneNames(EditTune.this).contains(tuneNameString))) {
                    Toast.makeText(EditTune.this, R.string.tune_name_exist_toast, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(actionCode == TuneSelect.EDIT_ACTION_CODE&&
                        (!tuneNameString.equals(extras.getString(GuitarTuner.TUNE_NAME_STRING))&&
                                pref.contains(tuneNameString)||
                                PreDefinedTunes.getTuneNames(EditTune.this).contains(tuneNameString))){
                    Toast.makeText(EditTune.this, R.string.tune_name_exist_toast, Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int index : indexArray) {
                    if (index == -1) {
                        Toast.makeText(EditTune.this, R.string.note_select_toast, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                int tunePos = extras.getInt(GuitarTuner.TUNE_POS_STRING);
                Intent intent = new Intent();
                intent.putExtra(GuitarTuner.TUNE_NAME_STRING, tuneNameInput.getText().toString());
                intent.putExtra(GuitarTuner.NOTE_INDEX_STRING, indexArray);
                intent.putExtra(GuitarTuner.TUNE_POS_STRING, tunePos);
                setResult(TuneSelect.RESULT_CODE, intent);
                finish();
            }
        });

        Button cancelBtn = (Button) findViewById(R.id.editTune_cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(TuneSelect.CANCELED_CODE, intent);
                finish();
            }
        });


    }
}


