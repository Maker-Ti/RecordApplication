package com.example.maker.recordapplication.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class tableDetailDataBaseAdapterOpenHelper extends SQLiteOpenHelper {
    private String TABLE_NAME = "detail";
    //id,name,gpsx,gpsy,date,text
    public tableDetailDataBaseAdapterOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //type:img text vedio voice

        String sql="create table detail (id integer primary key autoincrement,title varchar(20),type varchar(20),content varchar(200),record varchar(20))";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public List<Map<String,String>> getDetialListByRecordName( SQLiteDatabase database,String recordName){
        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor = database.query(TABLE_NAME,null,"record=?",new String[]{recordName},null,null,null);
        Log.e("makerLog","c"+cursor.getColumnCount());
        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                //id integer primary key autoincrement,
                // title varchar(20),
                // type varchar(20),
                // content varchar(20000),
                // record varchar(20)

                Map<String,String> map = new HashMap<>();
                map.put("content",cursor.getString(cursor.getColumnIndex("content")));
                map.put("type",cursor.getString(cursor.getColumnIndex("type")));
                map.put("title",cursor.getString(cursor.getColumnIndex("title")));


                list.add(map);
            }
        }
        List<Map<String,String>> opsiteList = new ArrayList<>();
        for(int i=(list.size()-1);i>-1;i--){
            Map<String,String> map = list.get(i);
            opsiteList.add(map);
        }
        return opsiteList;
    }
    //插入
    public boolean insertDetail(String title,String content,String type,String record,SQLiteDatabase db){
        boolean flag = false;
        if(getDetailExist(title,db) == false){
            int num;

            ContentValues values = new ContentValues();
            //id integer primary key autoincrement,
            // title varchar(20),
            // type varchar(20),
            // content varchar(20000),
            // record varchar(20)
            values.put("title", title);
            values.put("type", type);
            values.put("content", content);
            values.put("record", record);
            num = (int) db.insert(TABLE_NAME,null,values);
            if(num>0){
                flag = true;
            }
        }else {
            flag = false;
        }

        return flag;

    }
    //查询记录是否存在
    public boolean getDetailExist(String title,SQLiteDatabase db){
        boolean flag = false;
        Cursor cursor = db.query(TABLE_NAME,null,"title=?",new String[]{title},null,null,null);
        if(cursor.getCount()!=0){
            flag = true;
        }
        cursor.moveToNext();

        Log.e("txhLog","times="+cursor.getCount());
        return flag;
    }

    //更新记录，暂不用
    public boolean updateWordTimes(String name,SQLiteDatabase db){

            boolean flag = false;
            int times = 0;
            String wordMean="";
        Cursor cursor = db.query("words",null,"word=?",new String[]{name},null,null,null);
       while (cursor.moveToNext()){
           times = cursor.getInt(cursor.getColumnIndex("times"));
           wordMean = cursor.getString(cursor.getColumnIndex("wordMean"));
        }

       times = times+1;
        Log.e("txhvalue","times"+times+"wordmean"+wordMean);
        ContentValues values = new ContentValues();
        values.put("word",name);
        values.put("wordMean",wordMean);
        values.put("times",times);
        int num=db.update("words",values,"word=?",new String[]{name});
        Log.e("txhLog","update"+num);
        if(num>0){
            flag = true;
        }
            return flag;
    }


}
