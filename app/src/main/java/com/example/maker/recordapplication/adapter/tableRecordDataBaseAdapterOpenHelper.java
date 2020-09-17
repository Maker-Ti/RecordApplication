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

public class tableRecordDataBaseAdapterOpenHelper extends SQLiteOpenHelper {
    private String TABLE_NAME = "record";
    //id,name,gpsx,gpsy,date,text
    public tableRecordDataBaseAdapterOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //word 单词，wordMean 单词意思，times 出现的频率
        String sql="create table record (id integer primary key autoincrement,name varchar(20),gpsx varchar(20),gpsy varchar(20),date varchar(100),text varchar(200))";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    //获取所有记录列表
    //id,name,gpsx,gpsy,date,text
    public List<Map<String,String>> getRecordList( SQLiteDatabase database){
        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor = database.query(TABLE_NAME,null,null,null,null,null,null);
        while(cursor.moveToNext()){
            Map<String,String> map = new HashMap<>();
            map.put("name",cursor.getString(cursor.getColumnIndex("name")));
            map.put("gpsx",cursor.getString(cursor.getColumnIndex("gpsx")));
            map.put("gpsy",cursor.getString(cursor.getColumnIndex("gpsy")));
            map.put("date",cursor.getString(cursor.getColumnIndex("date")));
            map.put("text",cursor.getString(cursor.getColumnIndex("text")));
            list.add(map);
        }
        List<Map<String,String>> opsiteList = new ArrayList<>();
        for(int i=(list.size()-1);i>-1;i--){
            Map<String,String> map = list.get(i);
            opsiteList.add(map);
        }
        return opsiteList;
    }

    //删除
    public boolean deleteWord(String word,SQLiteDatabase db){
        boolean flag = false;
        int num;
        num = (int) db.delete(TABLE_NAME,"name=?",new String[]{word});
        if(num>0){
            flag = true;
        }
        return flag;

    }
    //插入一个记录
    //id,name,gpsx,gpsy,date,text
    public boolean insertRecord(String name,String gpsx,String gpsy,String date,String text,SQLiteDatabase db){
        boolean flag = false;
        int num;
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("gpsx", gpsx);
        values.put("gpsy", gpsy);
        values.put("date", date);
        values.put("text", text);
        num = (int) db.insert(TABLE_NAME,null,values);
        if(num>0){
            flag = true;
        }
        return flag;

    }
    //查询记录是否存在
    public boolean getRecordExist(String name,SQLiteDatabase db){
        boolean flag = false;
        Cursor cursor = db.query("words",null,"word=?",new String[]{name},null,null,null);
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
