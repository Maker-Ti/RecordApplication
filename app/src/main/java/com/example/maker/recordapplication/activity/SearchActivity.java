package com.example.maker.recordapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.maker.recordapplication.R;
import com.example.maker.recordapplication.tool.UserAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchActivity extends Activity {
    private TextView tv_search;
    private EditText ed_search;
    private ImageView img_load;
    private RelativeLayout re_load;
    private ListView lv_record;
    private List<Map<String,String>> rearchedList = new ArrayList<>();
    @Override
    protected void onResume() {
        super.onResume();
        re_load.setVisibility(View.GONE);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sarch);
        initView();
    }

    private void initView() {
        img_load = findViewById(R.id.img);
        re_load = findViewById(R.id.re_load);
        re_load.setVisibility(View.GONE);
        lv_record = findViewById(R.id.lv_record);
        tv_search = findViewById(R.id.tv_search);
        ed_search = findViewById(R.id.ed_search);
        tv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String index = ed_search.getText().toString();
                rearchedList.clear();
                for(int i=0;i< UserAction.recordListIntent.size();i++){
                    String nameKey = UserAction.recordListIntent.get(i).get("name");
                    String dateKey = UserAction.recordListIntent.get(i).get("date");
                    String textKey = UserAction.recordListIntent.get(i).get("text");
                    if (nameKey.contains(index)||dateKey.contains(index)||textKey.contains(index)){
                        rearchedList.add( UserAction.recordListIntent.get(i));
                    }
                }
                initData();
                Toast.makeText(SearchActivity.this, "查询结果"+rearchedList.size()+"条", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void initData() {
        String[] agrs = {"name","date","text"};
        int[] ids = {R.id.tv_name,R.id.tv_date,R.id.tv_info};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,rearchedList,R.layout.layout_record_list_item,agrs,ids);
        lv_record.setAdapter(simpleAdapter);
        lv_record.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                re_load.setVisibility(View.VISIBLE);
                Drawable drawable = img_load.getDrawable();
                ((Animatable)drawable).start();
                UserAction.selectedRecord = rearchedList.get(i);
                startActivity(new Intent(SearchActivity.this,RecordDetailActivity.class));
            }
        });
    }
}
