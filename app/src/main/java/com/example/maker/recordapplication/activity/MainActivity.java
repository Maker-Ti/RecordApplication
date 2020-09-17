package com.example.maker.recordapplication.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.example.maker.recordapplication.R;
import com.example.maker.recordapplication.adapter.tableRecordDataBaseAdapterOpenHelper;
import com.example.maker.recordapplication.tool.UserAction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    private ListView lv_record;
    private LocationClient mLocationClient;
    private RelativeLayout re_add;
    private RelativeLayout re_find;
    private List<Map<String,String>> recordList ;
    private tableRecordDataBaseAdapterOpenHelper openHelper;
    private SQLiteDatabase sqLiteDatabase;
    private TextView tv_norecord;
    private double gpsx ;
    private double gpsy ;
    private boolean locatedFlag = false;
    private ImageView img_load;
    private RelativeLayout re_load;

    @Override
    protected void onResume() {
        super.onResume();
        re_load.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        initView();

        loadLocation();
        initDataBase();
        init();
        initData();
    }
    private void loadLocation() {
        mLocationClient = new LocationClient(this);

//注册LocationListener监听器
        MainActivity.MyLocationListener myLocationListener = new MainActivity.MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
//通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(3000);

//设置locationClientOption
        mLocationClient.setLocOption(option);

//开启地图定位图层
        mLocationClient.start();
    }
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {

            gpsx = location.getLatitude();
            gpsy = location.getLongitude();
            locatedFlag = true;
            Log.e("MainAct","getPo:"+location.getLatitude()+"getLo"+location.getLongitude());

        }
    }
    private void initDataBase() {
        openHelper = new tableRecordDataBaseAdapterOpenHelper(MainActivity.this,"wordDB",null,1);
        sqLiteDatabase = openHelper.getReadableDatabase();

    }
    private void init() {
        recordList = openHelper.getRecordList(sqLiteDatabase);
        Log.e("txhList",""+recordList);
        if(recordList.size()>0){
            tv_norecord.setText("当前"+recordList.size()+"条记录");
            initData();
        }else {
            tv_norecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for(int i=0;i<10;i++){
                        openHelper.insertRecord("name"+i,"1","1","201911241400","档案"+i,sqLiteDatabase);
                    }
                    init();
                }
            });
        }
    }


    private void appendAddDialog(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View view = LayoutInflater.from(this).inflate(R.layout.add_record_dialog,null);
        alertDialog.setView(view);
        alertDialog.show();
        final EditText ed_name = view.findViewById(R.id.ed_name);
        ed_name.setText(getSystemTime(true));
        final EditText ed_info = view.findViewById(R.id.ed_info);
        Button addrecord = view.findViewById(R.id.btn_sure);
        addrecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locatedFlag == true){
                    boolean insert = openHelper.insertRecord(ed_name.getText().toString(),""+gpsx,""+gpsy,getSystemTime(false),ed_info.getText().toString(),sqLiteDatabase);
                    if(insert){
                        Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                        init();
                        alertDialog.dismiss();
                        UserAction.selectedRecord = recordList.get(0);
                        startActivity(new Intent(MainActivity.this,RecordDetailActivity.class));
                    }else {
                        Toast.makeText(MainActivity.this, "添加出错", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "定位不成功，不可添加", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }
    private String getSystemTime(boolean flag){
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd-HH-mm");// HH:mm:ss
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
//获取当前时间
        Date date = new Date(System.currentTimeMillis());

        if(flag){
            return simpleDateFormat1.format(date);
        }else {
            return simpleDateFormat2.format(date);
        }
    }
    private void initData() {
        String[] agrs = {"name","date","text"};
        int[] ids = {R.id.tv_name,R.id.tv_date,R.id.tv_info};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,recordList,R.layout.layout_record_list_item,agrs,ids);
        lv_record.setAdapter(simpleAdapter);
        lv_record.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                re_load.setVisibility(View.VISIBLE);
                Drawable drawable = img_load.getDrawable();
                ((Animatable)drawable).start();
                UserAction.selectedRecord = recordList.get(i);
                startActivity(new Intent(MainActivity.this,RecordDetailActivity.class));
            }
        });
    }

    private void initView() {
        lv_record = findViewById(R.id.lv_record);
        tv_norecord = findViewById(R.id.tv_norecord);
        re_add = findViewById(R.id.re_add);
        re_find = findViewById(R.id.re_find);
        re_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserAction.recordListIntent = recordList;
                startActivity(new Intent(MainActivity.this,SearchActivity.class));
            }
        });
        re_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendAddDialog();
            }
        });
        img_load = findViewById(R.id.img);
        re_load = findViewById(R.id.re_load);
    }

}
