package com.example.robottime.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.robottime.R;
import com.example.robottime.StaticVariable;
import com.example.robottime.utils.CommonUtil;
import com.example.robottime.utils.FileUtil;
import com.example.robottime.voice.Oscilloscope;
import com.example.robottime.voice.RecognizerTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.example.robottime.voice.RecognitionListener;

import static android.speech.RecognizerIntent.EXTRA_RESULTS;

public class ActivityVoice extends AppCompatActivity implements View.OnTouchListener, RecognitionListener, View.OnClickListener {

    /*static library*/
    static {
        System.loadLibrary("pocketsphinx_jni");
    }

    private static final String TAG = "ActivityVoice";

    private SurfaceView svVoice;
    private ImageView ivVoice;
    private TextView tvVoiceState;
    private TextView tvResPath;

    private RadioGroup languageRg;
    private RadioButton enRbtn, zhRbtn;

    private Spinner modelSpinner;

    private Button recordBtn;

    //assets
    private static String hmmAssetsPath = "hmm.zip";
    private static String modelAssetsPath = "lm.zip";

    @SuppressLint("SdCardPath")
    private static final String sdHmmPath = "/sdcard/Android/data/com.robottime.hmm/";
    private static String sdHmmZhPath = sdHmmPath + "hmm/zh/tdt_sc_8k";
    private static String sdHmmEnPath = sdHmmPath + "hmm/en_US/hub4wsj_sc_8k";

    @SuppressLint("SdCardPath")
    private static final String sdLmPath = "/sdcard/Android/data/com.robottime.lm/";
    private static String sdModelZhPath = sdLmPath + "lm/zh/";
    private static String sdModelEnPath = sdLmPath + "lm/en_US/";

    //log path
    @SuppressLint("SdCardPath")
    private static String sphinxLogFile = "/sdcard/Android/data/com.robottime.log/pocketsphinx.log";

    //model name
    private List<String> lmList = new ArrayList<String>();
    private ArrayAdapter<String> lmAdapter;

    //osc
    private static final int frequency = 24000;//分辨率
    private static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private static final int xMax = 16;//X轴缩小比例最大值,X轴数据量巨大，容易产生刷新延时
    private static final int xMin = 8;//X轴缩小比例最小值
    private static final int yMax = 16;//Y轴缩小比例最大值
    private static final int yMin = 1;//Y轴缩小比例最小值
    private Oscilloscope oscilloscope = new Oscilloscope();
    private int recBufSize;
    private AudioRecord audioRecord;
    private Paint paint;

    //rec
    private Thread rec_thread;
    private RecognizerTask rec;
    private boolean listening;
    private Intent intent;

    //handler
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what){
                case StaticVariable.MODELRES_UZIP_START:
                    tvResPath.setText("资源文件解压中，请稍后...");
                    break;
                case StaticVariable.MODELRES_UZIP_END:
                    CommonUtil.toastTextShow(ActivityVoice.this, "资源文件解压完成");
                    tvResPath.setText("");
                    //设置模型列表
                    getLocalModels();
                    break;
                default:
                    break;
            }

            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_voice);
        intent = this.getIntent();

        initLayout();
        //initAudioRecord();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        searchLoadModel();
    }

    //初始化控件
    @SuppressLint("ClickableViewAccessibility")
    private void initLayout()
    {
        //录音波形显示
        svVoice = (SurfaceView)findViewById(R.id.sv_voice);
        //默认图片显示
        ivVoice = (ImageView)findViewById(R.id.iv_voice);
        //识别状态
        tvVoiceState = (TextView)findViewById(R.id.tv_voice_state);
        //资源路径
        tvResPath = (TextView)findViewById(R.id.tv_res);

        modelSpinner = (Spinner)findViewById(R.id.model_spinner);

        languageRg = (RadioGroup)findViewById(R.id.language_rg);
        enRbtn = (RadioButton)findViewById(R.id.en_rbtn);
        zhRbtn = (RadioButton)findViewById(R.id.zh_rbtn);

        recordBtn = (Button)findViewById(R.id.record_btn);

        enRbtn.setOnClickListener(this);
        zhRbtn.setOnClickListener(this);
        recordBtn.setOnLongClickListener(new keyBtnLongClickListener(recordBtn));
        recordBtn.setOnTouchListener(this);

    }

    private void initAudioRecord()
    {
        recBufSize = AudioRecord.getMinBufferSize(frequency,
                channelConfiguration, audioEncoding);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
                channelConfiguration, audioEncoding, recBufSize);

        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.colorPrimary));
        paint.setStrokeWidth(2);

        oscilloscope.initOscilloscope(xMax/2, yMax/2, svVoice.getHeight()/2);
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View v, MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                if(v.getId() == R.id.record_btn)
                {
                    //oscilloscope.Stop();

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    recordBtn.setText("长按识别");
                    tvVoiceState.setText("已停止");
                    svVoice.setVisibility(View.GONE);
                    ivVoice.setVisibility(View.VISIBLE);

                    intent.putExtra(EXTRA_RESULTS, this.tvResPath.getText().toString());
                    setResult(RESULT_OK, intent);
                }
                break;
        }
        return false;
    }

    //按键长按事件
    private class keyBtnLongClickListener implements View.OnLongClickListener{
        private Button btn;

        private keyBtnLongClickListener(Button btn){
            this.btn = btn;
        }

        @Override
        public boolean onLongClick(View v){

            switch (btn.getId())
            {
                case R.id.record_btn:
                    //振动一次
                    final Vibrator vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
                    assert vibrator != null;
                    vibrator.vibrate(30);
                    recordBtn.setText("识别中");
                    tvVoiceState.setText("正在接收语音，释放按键停止");
                    ivVoice.setVisibility(View.GONE);
                    svVoice.setVisibility(View.VISIBLE);

                    //oscilloscope.baseLine = svVoice.getHeight() / 2;
                    //oscilloscope.Start(audioRecord,recBufSize,svVoice,paint);

                    rec.start();
                    break;
            }

            return true;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.en_rbtn:
                enRbtn.setChecked(true);
                searchLoadModel();
                break;
            case R.id.zh_rbtn:
                zhRbtn.setChecked(true);
                searchLoadModel();
                break;
        }

    }

    //搜索并加载声音模型文件
    private void searchLoadModel()
    {
        if(FileUtil.isFolderExists(sdHmmPath, false) &&
                FileUtil.isFolderExists(sdLmPath, false))
        {
            getLocalModels();
            return;
        }

        new Thread(){
            public void run() {
                //在新线程中以同名覆盖方式解压
                try {
                    Message msg = Message.obtain();
                    msg.what = StaticVariable.MODELRES_UZIP_START;
                    handler.sendMessage(msg);

                    //检测日志文件
                    FileUtil.createFile(sphinxLogFile);

                    if(!FileUtil.isFolderExists(sdHmmPath, false))
                    {
                        //SD卡hmm文件资源解压
                        FileUtil.unZip(ActivityVoice.this,
                                hmmAssetsPath, sdHmmPath, true);
                    }

                    if(!FileUtil.isFolderExists(sdLmPath, false))
                    {
                        //SD卡lm默认模型文件资源解压
                        FileUtil.unZip(ActivityVoice.this,
                                modelAssetsPath, sdLmPath, true);
                    }

                    Thread.sleep(1000);

                    Message msg1 = Message.obtain();
                    msg1.what = StaticVariable.MODELRES_UZIP_END;
                    handler.sendMessage(msg1);

                } catch (IOException | InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void getLocalModels()
    {
        if(checkResFolder())
        {
            lmList.clear();

            //搜索...
            if(zhRbtn.isChecked())
            {
                lmList.addAll(checkModelFiles(sdModelZhPath));
            }
            else if(enRbtn.isChecked())
            {
                lmList.addAll(checkModelFiles(sdModelEnPath));
            }
        }

        if(lmList.size() == 0)
        {
            CommonUtil.toastTextShow(ActivityVoice.this, "未找到资源文件");
        }

        setLmSpinner();
    }

    private void setLmSpinner()
    {
        lmAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lmList);
        lmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(lmAdapter);

        //加载所需文件
        loadLmFile();
    }

    //检查模型文件是否存在
    private List<String> checkModelFiles(String path)
    {
        final String lmSuffix = ".lm";
        final String dicSuffix = ".dic";
        List<String> lmFiles = new ArrayList<String>();
        List<Integer> index = new ArrayList<Integer>();

        //搜索模型文件
        lmFiles = FileUtil.getFiles(path, lmSuffix, true, false);

        //检测字典文件
        for(String lm : lmFiles)
        {
            if(!FileUtil.isFileExists(path + lm + dicSuffix))
            {
                index.add(lmFiles.indexOf(lm));
            }
        }

        if(index.size() > 0)
        {
            for(int j : index)
            {
                lmFiles.remove(j);
            }
        }

        return lmFiles;
    }

    //加载模型文件
    private void loadLmFile()
    {
        try
        {
            final int id = modelSpinner.getSelectedItemPosition();
            final String name = modelSpinner.getSelectedItem().toString();
            String hmm = "", lm = "", log = sphinxLogFile;

            if(zhRbtn.isChecked())
            {
                hmm = sdHmmZhPath;
                lm = sdModelZhPath + name;
            }
            else if(enRbtn.isChecked())
            {
                hmm = sdHmmEnPath;
                lm = sdModelEnPath + name;
            }

            //加载
            this.rec = new RecognizerTask(hmm, lm, log);
            this.rec_thread = new Thread(this.rec);
            this.listening = false;
            this.rec.setRecognitionListener(this);
            this.rec_thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean checkResFolder()
    {
        //检测资源目录是否存在
        //tvResPath.setText(sdModelZhPath);

        if(FileUtil.isFolderExists(sdModelZhPath, false) &&
                FileUtil.isFolderExists(sdModelEnPath, false))
        {
            return true;
        }
        else
        {
            CommonUtil.toastTextShow(ActivityVoice.this,
                    "未找到资源文件，将使用默认模型");
        }

        return false;
    }


    /** Called when partial results are generated. */
    public void onPartialResults(Bundle b)
    {
        final ActivityVoice that = this;
        final String hyp = b.getString("hyp");

        this.tvResPath.post(new Runnable() {
            @Override
            public void run() {

                that.tvResPath.setText(hyp);
            }
        });
    }

    /** Called with full results are generated. */
    public void onResults(Bundle b)
    {
        final ActivityVoice that = this;
        final String hyp = b.getString("hyp");

        this.tvResPath.post(new Runnable() {
            @Override
            public void run() {

                that.tvResPath.setText(hyp);
            }
        });
    }

    @Override
    public void onError(int err)
    {
        final ActivityVoice that = this;

        this.tvResPath.post(new Runnable() {
            @Override
            public void run() {

                that.tvResPath.setText("error");
            }
        });

        setResult(RESULT_CANCELED, intent);
    }


}
