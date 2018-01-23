package bella.caffedemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
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
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import bella.caffedemo.caffelibs.CaffeMobile;

public class MainActivity extends AppCompatActivity {
    private final String LOG_TAG="huangyaling";
    // Example of a call to a native method
    private TextView tv;

    private ImageView classification_img;
    private Button classification_btn;
    private TextView load_caffe;

    private CaffeMobile mCaffeMobile;

    File sdCard= Environment.getExternalStorageDirectory();
    final File imageFile=new File(sdCard,"test_image.jpg");
    final File modelFile = new File(Environment.getExternalStorageDirectory(), "net.protobin");
    final File weightFile = new File(Environment.getExternalStorageDirectory(), "weight.caffemodel");
    final int REQUST_CODE=001;


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("caffe-jni");
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
    }

    //init activity view
    private void initView(){
        // Example of a call to a native method
        tv = (TextView) findViewById(R.id.sample_text);
        classification_img=findViewById(R.id.classification_img);
        classification_btn=findViewById(R.id.classification_btn);
        load_caffe=findViewById(R.id.load_caffe);
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
        //classification_img.setImageResource(R.drawable.cat);
        Bitmap bmp = BitmapFactory.decodeFile(imageFile.getPath());
        classification_img.setImageBitmap(bmp);
    }

    //set button click listener
    private void setListener(){
        classification_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG,"button click");
                final TextView tv = (TextView) findViewById(R.id.Console);
                tv.append("\nCaffe inferring...\n");
                final Handler myHandler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        load_caffe.append((String)msg.obj);
                    }
                };
                (new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = myHandler.obtainMessage();
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
                        myHandler.sendMessage(msg);
                    }
                })).start();
            }
        });
    }



    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
