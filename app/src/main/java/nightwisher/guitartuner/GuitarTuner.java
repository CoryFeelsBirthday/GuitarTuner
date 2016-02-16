package nightwisher.guitartuner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.Gson;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class GuitarTuner extends AppCompatActivity {


    final static String[] noteArray = {"C2","Db2","D2","Eb2","E2","F2","Gb2","G2",
            "Ab2","A2","Bb2","B2","C3","Db3","D3","Eb3","E3","F3","Gb3","G3",
            "Ab3","A3","Bb3","B3","C4","Db4","D4","Eb4","E4","F4","Gb4","G4",
            "Ab4","A4","Bb4","B4"};
    final static float[] freqArray = {65.406f,69.296f,73.416f,77.782f,82.407f,87.307f,92.499f,
            97.999f,103.826f,110f,116.541f,123.471f,130.813f,138.591f,146.832f,155.563f,164.814f,174.614f,
            184.997f,195.998f,207.652f,220f,233.082f,246.942f,261.626f,277.183f,293.665f,311.127f,329.628f,
            349.228f,369.994f,391.995f,415.305f,440f,466.164f,493.883f};

//    use 8000Hz on emulator and 44100Hz on device
    private final static int SAMPLE_RATE = "sdk".equals(Build.PRODUCT) ? 8000 : 44100;
    private final static int CHANNEL_FORMAT = AudioFormat.CHANNEL_IN_MONO;
    private final static int ENCODING_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private final static int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
            CHANNEL_FORMAT,
            ENCODING_FORMAT);

    final static int RECORD_AUDIO_PERMISSION_CODE = 1;
    final static String TUNE_PREF_STRING = "tunes_pref";
    final static String TUNE_NAME_STRING = "tune_name";
    final static String NOTE_INDEX_STRING = "note_index";
    final static String TUNE_POS_STRING = "tune_pos";
    final static String LAST_TUNE_STRING = "last_tune";

    final static int TUNE_SELECT_CODE = 1;


    private AudioRecord recorder;
    private Tune currentTune;
    private RadioGroup noteBtnGroup;
    private int selectedBtnIndex = 6;
    private ImageView guitarView;
    private YinPitchTracker pitchTracker = new YinPitchTracker(SAMPLE_RATE);

    public Tune getCurrentTune(){
        return currentTune;
    }


    public int getSelectedBtnIndex(){
        return selectedBtnIndex;
    }

    public YinPitchTracker getPitchTracker(){
        return pitchTracker;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guitar_tuner);

        guitarView = (ImageView) findViewById(R.id.guitar_image);

        // get microphone permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_CODE);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.guitartuner_toolbar);
        setSupportActionBar(toolbar);
        noteBtnGroup = (RadioGroup) findViewById(R.id.note_btn_group);

        MovingGridView movingGridView = (MovingGridView)findViewById(R.id.moving_grid_view);
        movingGridView.setGuitarTuner(this);

        Button addNewTuneBtn = (Button) findViewById(R.id.select_tune_btn);
        addNewTuneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GuitarTuner.this, TuneSelect.class);
                startActivityForResult(intent, TUNE_SELECT_CODE);
            }
        });

        // load last selected tune
        SharedPreferences lastTunePref = getSharedPreferences(LAST_TUNE_STRING, Context.MODE_PRIVATE);
        if (lastTunePref.contains(LAST_TUNE_STRING)) {
            Gson gson = new Gson();
            currentTune = gson.fromJson(lastTunePref.getString(LAST_TUNE_STRING, ""),Tune.class);
        }else{
            currentTune = PreDefinedTunes.getTunes(this)[0];
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNoteName();


        noteBtnGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton noteBtn = (RadioButton) findViewById(checkedId);
                selectedBtnIndex = group.indexOfChild(noteBtn);

                Bitmap guitarImage = BitmapFactory.decodeResource(GuitarTuner.this.getResources(),
                        R.drawable.guitar).copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(guitarImage);
                Paint paint = new Paint();
                Bitmap mask;
                switch (selectedBtnIndex) {
                    case 0:
                        mask = BitmapFactory.decodeResource(GuitarTuner.this.getResources(),
                                R.drawable.guitar_string6);
                        break;
                    case 1:
                        mask = BitmapFactory.decodeResource(GuitarTuner.this.getResources(),
                                R.drawable.guitar_string5);
                        break;
                    case 2:
                        mask = BitmapFactory.decodeResource(GuitarTuner.this.getResources(),
                                R.drawable.guitar_string4);
                        break;
                    case 3:
                        mask = BitmapFactory.decodeResource(GuitarTuner.this.getResources(),
                                R.drawable.guitar_string3);
                        break;
                    case 4:
                        mask = BitmapFactory.decodeResource(GuitarTuner.this.getResources(),
                                R.drawable.guitar_string2);
                        break;
                    case 5:
                        mask = BitmapFactory.decodeResource(GuitarTuner.this.getResources(),
                                R.drawable.guitar_string1);
                        break;
                    default:
                        mask = BitmapFactory.decodeResource(GuitarTuner.this.getResources(),
                                R.drawable.guitar_no_mask);
                }
                canvas.drawBitmap(mask, 0, 0, paint);
                guitarView.setImageBitmap(guitarImage);
            }
        });

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_FORMAT,
                ENCODING_FORMAT,
                minBufferSize);

        if(recorder.getState()==AudioRecord.STATE_INITIALIZED){
            recorder.startRecording();
            Thread recordThread=new Thread(new Runnable() {
                @Override
                public void run() {

                    short[] audioData = new short[minBufferSize];
                    pitchTracker = new YinPitchTracker(SAMPLE_RATE);
                    while(recorder!=null&&recorder.getRecordingState()==AudioRecord.RECORDSTATE_RECORDING) {
                        recorder.read(audioData, 0, audioData.length);
                        pitchTracker.detectPitch(audioData);
                    }
                }

            });
            recordThread.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(recorder!=null){
            if(recorder.getRecordingState()==AudioRecord.RECORDSTATE_RECORDING)
                recorder.stop();
            recorder.release();
            recorder = null;
        }
        SharedPreferences lastTunePref = getSharedPreferences(LAST_TUNE_STRING, Context.MODE_PRIVATE);
        SharedPreferences.Editor lastTunePrefEditor = lastTunePref.edit();
        Gson gson = new Gson();
        lastTunePrefEditor.putString(LAST_TUNE_STRING, gson.toJson(currentTune));
        lastTunePrefEditor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==TUNE_SELECT_CODE&&resultCode==TuneSelect.TUNE_SELECTED_CODE){
            currentTune.setNoteIndex(data.getIntArrayExtra(GuitarTuner.NOTE_INDEX_STRING));
            currentTune.setName(data.getStringExtra(GuitarTuner.TUNE_NAME_STRING));
            setNoteName();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_guitar_tuner, menu);
        return true;
    }


    private void setNoteName(){
        for(int i=0;i<6;i++){
            RadioButton noteBtn = (RadioButton) noteBtnGroup.getChildAt(i);
            noteBtn.setText(noteArray[currentTune.getNoteIndex()[i]]);
        }
    }
}

class MovingGridView extends SurfaceView implements SurfaceHolder.Callback{
    private final static int MIN_NOTE_INDICATOR_SIZE = 100;
    private final static int FRESH_RATE = 60;
    private final static int MOVING_SPEED = 5;
    private final static int GRID_SIZE = 50;
    private final static int BORDER_WIDTH = 4;
    private ScheduledExecutorService gridViewExecutor;
    private int width;
    private int height;
    private GuitarTuner guitarTuner;
    private HistoryQueue<Float> posHistory;
    private int gridPos;
    private int noteIndex = -1;
    private int noteIndicatorSize;
    private Paint framePaint = new Paint();
    private Paint lowCharPaint = new Paint();
    private Paint highCharPaint = new Paint();
    private Paint gridPainter = new Paint();
    private Paint recordPainter = new Paint();
    private Paint midLinePainter = new Paint();
    private Paint notePaint = new Paint();
    private Paint cursorPaint = new Paint();

    private void drawBackground(Canvas canvas){
        canvas.drawColor(Color.WHITE);
        canvas.drawRect(BORDER_WIDTH / 2, BORDER_WIDTH / 2,
                width - BORDER_WIDTH / 2, height - BORDER_WIDTH / 2, framePaint);
        canvas.drawLine(0, noteIndicatorSize, width, noteIndicatorSize, framePaint);
    }

    private void drawNoteIndicator(Canvas canvas, int noteIndex, float currentPos){
        lowCharPaint.setTextSize((float) 0.6 * noteIndicatorSize);
        highCharPaint.setTextSize((float) 0.6 * noteIndicatorSize);
        notePaint.setTextSize((float) 0.6 * noteIndicatorSize);
        canvas.drawText("b", noteIndicatorSize, (noteIndicatorSize - lowCharPaint.ascent()) / 2,
                lowCharPaint);
        canvas.drawText("#", width - noteIndicatorSize, (noteIndicatorSize - highCharPaint.ascent()) / 2,
                highCharPaint);
        canvas.drawText(GuitarTuner.noteArray[noteIndex],width/2,
                (noteIndicatorSize - notePaint.ascent()) / 2,notePaint);
        canvas.drawLine(currentPos, 0, currentPos, noteIndicatorSize, cursorPaint);
    }

    private int calculateNoteIndex(float freq, Tune currentTune, int selectedBtnIndex){
        if(freq<0){
            if(noteIndex==-1)
                return currentTune.getNoteIndex()[0];
            else {
                if(selectedBtnIndex==6)
                    return noteIndex;
                else
                    return currentTune.getNoteIndex()[selectedBtnIndex];
            }
        }else{
            if(selectedBtnIndex==6){

                float minFreqDif = Float.MAX_VALUE;
                int index = -1;
                float freqDif;
                for(int i=0;i<6;i++){
                    freqDif = Math.abs(GuitarTuner.freqArray[currentTune.getNoteIndex()[i]]-freq);
                    if(freqDif<minFreqDif) {
                        minFreqDif = freqDif;
                        index = currentTune.getNoteIndex()[i];
                    }
                }
                return index;
            }
            else{
                return currentTune.getNoteIndex()[selectedBtnIndex];
            }
        }
    }

    private float calculatePos(float freq){
        if(freq<0){
            return -1;
        }else{
            float targetFreq = GuitarTuner.freqArray[noteIndex];
            if(freq<0.9 * targetFreq)
                freq = 0.9f * targetFreq;
            else if(freq>1.1 * targetFreq)
                freq = 1.1f * targetFreq;
            return (float)((freq - targetFreq) / (0.1 * targetFreq) * (float)(width-10)/2 + (float)(width)/2);
        }
    }

    private void drawGrid(Canvas canvas){
        for(int x=width/2;x>=0;x-=GRID_SIZE){
            canvas.drawLine(x,noteIndicatorSize,x,height,gridPainter);
        }
        for(int x=width/2;x<width;x+=GRID_SIZE){
            canvas.drawLine(x,noteIndicatorSize,x,height,gridPainter);
        }
        for(int y=gridPos+noteIndicatorSize;y<height;y+=GRID_SIZE){
            canvas.drawLine(0,y,width,y,gridPainter);
        }
        gridPos+=MOVING_SPEED;
        if(gridPos>=GRID_SIZE){
            gridPos=0;
        }
        canvas.drawLine(width / 2, noteIndicatorSize, width / 2, height, midLinePainter);
    }

    private void drawRecord(Canvas canvas, float currentPos) {
        for(int i=0;i<MOVING_SPEED;i++){
            posHistory.push(currentPos);
        }

        for(int i=0;i<posHistory.size();i++){
            canvas.drawPoint(posHistory.getItem(i), noteIndicatorSize + height - i, recordPainter);
        }
    }

    class MovingGridThread implements Runnable {
        SurfaceHolder surfaceHolder = MovingGridView.this.getHolder();
        boolean canvasLocked = false;

        @Override
        public synchronized void run() {
            Canvas canvas = null;
            try{

                canvas = surfaceHolder.lockCanvas(null);
                canvasLocked = true;
                drawBackground(canvas);

                int selectedBtnIndex = guitarTuner.getSelectedBtnIndex();
                Tune currentTune = guitarTuner.getCurrentTune();
                float freq = guitarTuner.getPitchTracker().getFreq();

                noteIndex = calculateNoteIndex(freq, currentTune, selectedBtnIndex);

                float currentPos = calculatePos(freq);

                drawNoteIndicator(canvas, noteIndex, currentPos);

                drawGrid(canvas);

                drawRecord(canvas, currentPos);

            }finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public MovingGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        framePaint.setColor(ContextCompat.getColor(context,R.color.blue));
        framePaint.setStrokeWidth(BORDER_WIDTH);
        framePaint.setStyle(Paint.Style.STROKE);
        lowCharPaint.setColor(ContextCompat.getColor(context, R.color.blue));
        highCharPaint.setColor(ContextCompat.getColor(context, R.color.blue));
        lowCharPaint.setTextAlign(Paint.Align.CENTER);
        highCharPaint.setTextAlign(Paint.Align.CENTER);
        gridPainter.setColor(Color.GRAY);
        gridPainter.setStrokeWidth(1);
        recordPainter.setColor(Color.GRAY);
        recordPainter.setStrokeWidth(4);
        midLinePainter.setColor(Color.RED);
        midLinePainter.setStrokeWidth(2);
        notePaint.setColor(ContextCompat.getColor(context, R.color.blue));
        cursorPaint.setColor(Color.RED);
        cursorPaint.setStrokeWidth(4);
        notePaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
        this.width = width;
        this.height = height;
        noteIndicatorSize = Math.min(height/5, MIN_NOTE_INDICATOR_SIZE);
        if(posHistory==null) {
            posHistory = new HistoryQueue<>(height);
            for(int i=0;i<height;i++){
                posHistory.push(-1f);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        gridViewExecutor = Executors.newScheduledThreadPool(1);
        gridViewExecutor.scheduleWithFixedDelay(new MovingGridThread(), 0, FRESH_RATE,
                TimeUnit.MILLISECONDS);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        gridViewExecutor.shutdown();
    }

    public void setGuitarTuner(GuitarTuner guitarTuner){
        this.guitarTuner = guitarTuner;
    }
}
