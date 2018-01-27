package bella.caffedemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dell on 18-1-22.
 */

public class HomeActivity extends AppCompatActivity {
    final int REQUST_CODE=001;
    final int CLASSIFICATION=0;
    final int CHARTTEST=1;
    private GridView mGridView;
    List<Map<String, Object>> listItem = new ArrayList<Map<String, Object>>();
    private int[] gv_image;
    private String[] gv_text;
    private GridViewAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setContentView(R.layout.home_activity);
        gv_image=new int[]{R.drawable.function,R.drawable.function,R.drawable.function,R.drawable.function};
        gv_text=getResources().getStringArray(R.array.grid_view_text);
        for(int i=0;i<gv_text.length;i++){
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("image",gv_image[i]);
            map.put("title",gv_text[i]);
            listItem.add(map);
        }
        mGridView=findViewById(R.id.functions_gv);
        adapter=new GridViewAdapter(this);
        mGridView.setAdapter(adapter);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUST_CODE);
        }else{
            Toast.makeText(this,"yyyy",Toast.LENGTH_SHORT).show();
        }
        clickEvent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUST_CODE){
            Toast.makeText(this,"hhhh",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this,"tttt",Toast.LENGTH_SHORT).show();
        }
    }

    //gridview click event
    public void clickEvent(){
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position){
                    case CLASSIFICATION:
                        startActivity(new Intent(HomeActivity.this,MainActivity.class));
                        break;
                    case CHARTTEST:
                        //startActivity(new Intent(HomeActivity.this,LineChartActivity.class));
                        break;
                }
            }
        });
    }

    public class GridViewAdapter extends BaseAdapter{
        private Context mContext;

        public GridViewAdapter(Context context){
            this.mContext=context;
        }

        @Override
        public int getCount() {
            return listItem.size();
        }

        @Override
        public Object getItem(int i) {
            return listItem.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view==null){
                view= LayoutInflater.from(mContext).inflate(R.layout.gridview_item,null);
            }
            ImageView item_image=view.findViewById(R.id.itemImage);
            TextView item_text=view.findViewById(R.id.itemText);
            Map<String,Object> map=listItem.get(i);
            item_image.setImageResource((Integer) map.get("image"));
            item_text.setText(map.get("title")+"");
            return view;
        }
    }

}
