package com.example.maker.recordapplication.tool;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserAction {
    public static Map<String,String> selectedRecord = null;
    public static String selectedVideoPath = "";
    public static List<Map<String,String>> recordListIntent ;
    public static String getSystemTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");// HH:mm:ss
//获取当前时间
        Date date = new Date(System.currentTimeMillis());


            return simpleDateFormat.format(date);
    }

}
