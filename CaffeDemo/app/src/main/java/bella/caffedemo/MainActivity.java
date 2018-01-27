package bella.caffedemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;

import java.io.File;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import bella.caffedemo.caffelibs.CaffeMobile;
import bella.caffedemo.view.ChartService;
import bella.caffedemo.view.LineChartView;

public class MainActivity extends Activity {
    private final String LOG_TAG="huangyaling";
    private final int CLASSIFICATION_NUM=4;
    private Boolean isTest=true;
    private TextView tv;

    private ImageView classification_img;
    private Button classification_btn;
    private Button classification_end;
    private TextView load_caffe;
    //折线图
    private LinearLayout chartView;
    private GraphicalView graphicalView;
    private ChartService chartService;

    private CaffeMobile mCaffeMobile;

    File sdCard= Environment.getExternalStorageDirectory();
    //加载一组图片
    String[] imageName=new String[]{"test_image.jpg","test_image2.jpg","test_image3.jpg","test_image4.jpg"};
    File imageFile=new File(sdCard,imageName[0]);
    final File modelFile = new File(Environment.getExternalStorageDirectory(), "net.protobin");
    final File weightFile = new File(Environment.getExternalStorageDirectory(), "weight.caffemodel");
    final int REQUST_CODE=001;

    private Handler myHandler;
    private Bitmap bmp;


    private int temp=0;
    private Timer timer;



    // Used to load the 'native-lib' library on application startup.
    static {
        //System.loadLibrary("caffe-jni");
        System.loadLibrary("native-lib");
    }

    protected class Cate implements Comparable<Cate> {
        public final int    idx;
        public final float  prob;

        public Cate(int idx, float prob) {
            this.idx = idx;
            this.prob = prob;
        }

        @Override
        public int compareTo(Cate other) {
            // need descending sort order
            return Float.compare(other.prob, this.prob);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUST_CODE){
            initView();
            initData();
            setListener();
        }else{
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG,"sdCard:"+sdCard);
        Log.d(LOG_TAG, "onCreate: modelFile:" + modelFile.getPath());
        Log.d(LOG_TAG, "onCreate: weightFIle:" + weightFile.getPath());
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUST_CODE);
        }else{
            initView();
            initData();
            setListener();
        }

        myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                //load_caffe.append((String)msg.obj);
                classification_img.setImageBitmap(bmp);
                chartService.updateChart(temp,Math.random() * 10);
                temp++;

            }
        };
    }

    //init activity view
    private void initView(){
        // Example of a call to a native method
        tv = (TextView) findViewById(R.id.sample_text);
        classification_img=findViewById(R.id.classification_img);
        classification_btn=findViewById(R.id.classification_btn);
        classification_end=findViewById(R.id.classification_end);
        load_caffe=findViewById(R.id.load_caffe);
        chartView=findViewById(R.id.line_chart_view);
    }

    //init data
    private void initData(){
        long start_time = System.nanoTime();
        mCaffeMobile=new CaffeMobile();
        tv.setText(stringFromJNI());
        load_caffe.append("Loading caffe model...");
        load_caffe.setMovementMethod(new ScrollingMovementMethod());
        boolean res = mCaffeMobile.loadModel(modelFile.getPath(), weightFile.getPath());
        long end_time = System.nanoTime();
        double difference = (end_time - start_time)/1e6;
        Log.d(LOG_TAG, "onCreate: loadmodel:" + res);
        load_caffe.append(String.valueOf(difference) + "ms\n");
        bmp = BitmapFactory.decodeFile(imageFile.getPath());
        classification_img.setImageBitmap(bmp);

        //chart
        chartService=new ChartService(this);
        chartService.setXYMultipleSeriesDataset("测试");
        chartService.setXYMultipleSeriesRenderer(60, 10, "测试", "时间:s", "fps:千张/秒",
                Color.WHITE, Color.WHITE, Color.BLUE, Color.WHITE);
        graphicalView=chartService.getGraphicalView();
        chartView.addView(graphicalView);
    }

    //set button click listener
    private void setListener(){
        classification_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG,"button click");
                final TextView tv = (TextView) findViewById(R.id.Console);
                //load_caffe.append("\nCaffe inferring...\n");
                isTest=true;
                classification_btn.setVisibility(View.GONE);
                classification_end.setVisibility(View.VISIBLE);
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(LOG_TAG,"isTest="+isTest);
                        while(isTest){
                            if(CLASSIFICATION_NUM!=imageName.length){
                                return;
                            }
                            for(int j=0;j<CLASSIFICATION_NUM;j++){
                                if(!isTest){
                                    return;
                                }
                                Message msg = myHandler.obtainMessage();

                                imageFile=new File(sdCard,imageName[j]);
                                bmp = BitmapFactory.decodeFile(imageFile.getPath());

                                long start_time = System.nanoTime();
                                float mean[] = {81.3f, 107.3f, 105.3f};

                                float[] result = mCaffeMobile.predictImage(imageFile.getPath(), mean);
                                long end_time = System.nanoTime();
                                if (null != result) {
                                    double difference = (end_time - start_time) / 1e6;
                                    // Top 10
                                    int topN = 10;
                                    Cate[] cates = new Cate[result.length];
                                    for (int i = 0; i < result.length; i++) {
                                        cates[i] = new Cate(i, result[i]);
                                    }
                                    Arrays.sort(cates);
                                    msg.obj = "Top" + topN + " Results (" + String.valueOf(difference) + "ms):\n";
                                    for (int i = 0; i < topN; i++) {
                                        msg.obj += "output[" + cates[i].idx + "]\t=" + cates[i].prob + "\n";
                                    }
                                } else {
                                    msg.obj = "output=null (some error happens)";
                                }

                                //chart
                                //lineChartView.setyTemp(3);
                                //lineChartView.refreshChart();
                                //graphicalView.postInvalidate();

                                myHandler.sendMessage(msg);

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).start();*/
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.d(LOG_TAG,"isTest="+isTest);
                        while(isTest){
                            if(CLASSIFICATION_NUM!=imageName.length){
                                return;
                            }
                            for(int j=0;j<CLASSIFICATION_NUM;j++){
                                if(!isTest){
                                    return;
                                }
                                Message msg = myHandler.obtainMessage();

                                imageFile=new File(sdCard,imageName[j]);
                                bmp = BitmapFactory.decodeFile(imageFile.getPath());

                                long start_time = System.nanoTime();
                                float mean[] = {81.3f, 107.3f, 105.3f};

                                float[] result = mCaffeMobile.predictImage(imageFile.getPath(), mean);
                                long end_time = System.nanoTime();
                                if (null != result) {
                                    double difference = (end_time - start_time) / 1e6;
                                    // Top 10
                                    int topN = 10;
                                    Cate[] cates = new Cate[result.length];
                                    for (int i = 0; i < result.length; i++) {
                                        cates[i] = new Cate(i, result[i]);
                                    }
                                    //msg.arg1=(int) 1000/difference;
                                    Arrays.sort(cates);
                                    //msg.arg1=StringToInt((1000/difference)+"");
                                    Log.i(LOG_TAG,"msg.arg1");
                                    msg.obj = "Top" + topN + " Results (" + String.valueOf(difference) + "ms):\n";
                                    for (int i = 0; i < topN; i++) {
                                        msg.obj += "output[" + cates[i].idx + "]\t=" + cates[i].prob + "\n";
                                    }
                                } else {
                                    msg.obj = "output=null (some error happens)";
                                }
                                myHandler.sendMessage(msg);
                            }
                        }
                    }
                },10,1000);
            }
        });

        classification_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classification_btn.setVisibility(View.VISIBLE);
                classification_end.setVisibility(View.GONE);
                isTest=false;
                if(timer!=null){
                    timer.cancel();
                }
            }
        });
    }

    public int StringToInt(String string){
        int j=0;
        String str=string.substring(0,string.indexOf("."))+
                string.substring(string.indexOf(".")+1);
        int getInteger=Integer.parseInt(str);
        return getInteger;

    }



    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
