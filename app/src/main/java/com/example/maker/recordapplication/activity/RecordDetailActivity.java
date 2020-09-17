package com.example.maker.recordapplication.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.maker.recordapplication.R;
import com.example.maker.recordapplication.adapter.DetailImgGriddapter;
import com.example.maker.recordapplication.adapter.tableDetailDataBaseAdapterOpenHelper;
import com.example.maker.recordapplication.adapter.tableRecordDataBaseAdapterOpenHelper;
import com.example.maker.recordapplication.tool.UserAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RecordDetailActivity extends Activity {
    private TextView tv_change_record;
    private LinearLayout lin_txt;
    private TextView tv_name;
    private TextView tv_info;
    private TextView tv_date;
    private TextView tv_locate;
    private RelativeLayout re_addimg;
    private RelativeLayout re_addtxt;
    private String selectedRecordName;
    private tableDetailDataBaseAdapterOpenHelper openHelper;
    private SQLiteDatabase sqLiteDatabase;
    private List<Map<String,String>> allData;
    private GridView imgGrid;
    private List<Map<String ,Object>> newImgData = new ArrayList<>();
    private List<Map<String,Object>> newVedioData = new ArrayList<>();
    private List<Map<String,Object>> newVoiceData = new ArrayList<>();
    private List<Map<String,Object>> newTxtData = new ArrayList<>();

    private RelativeLayout re_addVideo;
    private ListView lv_vedio,lv_voice,lv_txt;
    private Uri imageUri;
    private File outputImage;
    private File tempFile;
    private RelativeLayout re_addVoice;
    private TextView tv_img_num,tv_video_num,tv_voice_num,tv_txt_num;
    private boolean isStart = false;
    private MediaRecorder mr = null;
    private File dir;
    private File soundFile;
    private int voice_time = 0;
    private Timer timer;
    private TimerTask timerTask;
    private Button btn_control;
    private TextView tv_time;
    private boolean voicePlayingFlag = false;
    private MediaPlayer voicePlayer;
    private String playingVoiceTimes;
    private int playingVoiceIndex = 0;
    private String voicePlayerPath = "";
    private Timer voicePlayTimer;
    private TimerTask voicePlayTimerTask;
    private TextView tv_playingVoice;

    Handler handleVoicePlaying = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(voicePlayingFlag){
                playingVoiceIndex++;
            }
            int hour = playingVoiceIndex/60;
            int second = playingVoiceIndex%60;
            tv_playingVoice.setText(""+hour+":"+second+"/"+playingVoiceTimes);
        }
    };
    Handler handlerCodling = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            voice_time += 1;
            int hour = voice_time/60;
            int second = voice_time%60;
            tv_time.setText(""+hour+":"+second);
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);
        selectedRecordName = UserAction.selectedRecord.get("name");
        initDataBase();
        initView();
        initData();
        initDetailView();
    }
    //开始录制
    private void startRecord(){
        if(mr == null){
            dir = new File(getExternalFilesDir(null),"sound");
            if(!dir.exists()){
                dir.mkdirs();
            }
            soundFile = new File(dir,System.currentTimeMillis()+".amr");
            if(!soundFile.exists()){
                try {
                    soundFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            mr = new MediaRecorder();
            mr.setAudioSource(MediaRecorder.AudioSource.MIC);  //音频输入源
            mr.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);   //设置输出格式
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);   //设置编码格式
            mr.setOutputFile(soundFile.getAbsolutePath());
            try {
                mr.prepare();
                mr.start();  //开始录制
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    //停止录制，资源释放
    private void stopRecord(){
        if(mr != null){
            mr.stop();
            mr.release();
            mr = null;
        }
    }
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 18){
            Toast.makeText(this, "voice", Toast.LENGTH_SHORT).show();
        }
        if(requestCode == 22){
            showVideoDialog();
        }
        if(requestCode == 5){
            String imagePath = null;
            Uri uri = imageUri;
            if(DocumentsContract.isDocumentUri(RecordDetailActivity.this,uri)){
                //如果是document类型的Uri，则通过document id 处理
                String docId = DocumentsContract.getDocumentId(uri);
                if("com.android.providers.media.documents".equals(uri.getAuthority())){
                    String id = docId.split(":")[1];
                    String selection  = MediaStore.Images.Media._ID + "=" + id;
                    imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                    imagePath = getImagePath(contentUri, null);
                }
            }else if("content".equalsIgnoreCase(uri.getScheme())){
                //如果是content类型的Uri，则使用普通的方式处理
                imagePath = getImagePath(uri, null);
            }else if("file".equalsIgnoreCase(uri.getScheme())){
                //如果是file类型的Uri，直接获取图片路径即可
                imagePath = uri.getPath();
            }
            displayImage(imagePath);
        }
    }
    private void refershDataNum(){
        tv_video_num.setText(""+newVedioData.size());
        tv_img_num.setText(""+newImgData.size());
        tv_voice_num.setText(""+newVoiceData.size());
        tv_txt_num.setText(""+newTxtData.size());
    }
    private void displayImage(String imagePath){
        if(imagePath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            showBitMapDialog(bitmap);
        }else {
            Toast.makeText(RecordDetailActivity.this,"获取图片失败",Toast.LENGTH_SHORT).show();
        }
    }
    private String getImagePath(Uri uri, String selection){
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                path = outputImage.getPath();
            }
            cursor.close();
        }
        return path;
    }

    private void initDetailView() {
        newVedioData.clear();
         newImgData.clear();
         newVoiceData.clear();
         newTxtData.clear();
        for(Map<String,String> map:allData){
          /*  map.put("content",cursor.getString(cursor.getColumnIndex("content")));
            map.put("title",cursor.getString(cursor.getColumnIndex("title")));
            map.put("type",cursor.getString(cursor.getColumnIndex("type")));*/
          if(map.get("type").equals("img")){
              Map<String,Object> mapData = new HashMap<>();
              mapData.put("image",map.get("content"));
              mapData.put("title",map.get("title"));
              newImgData.add(mapData);
          }
          if(map.get("type").equals("video")){
              Map<String,Object> mapData = new HashMap<>();
              mapData.put("video",map.get("content"));
              mapData.put("title",map.get("title"));
              newVedioData.add(mapData);
          }
            if(map.get("type").equals("voice")){
                Map<String,Object> mapData = new HashMap<>();
                mapData.put("voice",map.get("content"));
                mapData.put("title",map.get("title"));
                newVoiceData.add(mapData);
            }
            if(map.get("type").equals("txt")){
                Map<String,Object> mapData = new HashMap<>();
                mapData.put("txt",map.get("content"));
                mapData.put("title",map.get("title"));
                newTxtData.add(mapData);
            }
        }
        imgGrid.setAdapter(new DetailImgGriddapter(this,newImgData));
        imgGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bitmap bitmap = BitmapFactory.decodeFile((String) newImgData.get(i).get("image"));
                showImageDetail((String)newImgData.get(i).get("title"),bitmap);
            }
        });
        SimpleAdapter simpleAdapter =
                new SimpleAdapter(this,newVedioData,R.layout.layout_list_video,new String[]{"title"},new int[]{R.id.title});
        lv_vedio.setAdapter(simpleAdapter);
        lv_vedio.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                UserAction.selectedVideoPath = (String) newVedioData.get(i).get("video");
                startActivity(new Intent(RecordDetailActivity.this,VideoPlayActivity.class));
            }
        });

        SimpleAdapter simpleAdapter1 =
                new SimpleAdapter(this,newVoiceData,R.layout.layout_list_voice,new String[]{"title"},new int[]{R.id.title});
        lv_voice.setAdapter(simpleAdapter1);
        lv_voice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                voicePlayerPath = (String) newVoiceData.get(i).get("voice");
                        playingVoiceDialohg();
            }
        });
        SimpleAdapter simpleAdapter2 =
                new SimpleAdapter(this,newTxtData,R.layout.layout_list_txt,new String[]{"title"},new int[]{R.id.title});
        lv_txt.setAdapter(simpleAdapter2);
        lv_txt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               showAddTxtViewDialog((String) newTxtData.get(i).get("title"),(String) newTxtData.get(i).get("txt"));
            }
        });
        refershDataNum();
    }

    private void playingVoiceDialohg() {
        final AlertDialog alertDialog = new AlertDialog.Builder(RecordDetailActivity.this).create();
        View dialog = LayoutInflater.from(RecordDetailActivity.this).inflate(R.layout.dialog_voice_playing,null);
        alertDialog.setView(dialog);
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
        Button close = dialog.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(voicePlayer!=null){
                    voicePlayer.pause();
                }

                voicePlayer = null;
                voicePlayTimer.cancel();
                voicePlayTimerTask = null;
                voicePlayTimer = null;
                voicePlayingFlag = false;
                playingVoiceTimes = "";
                playingVoiceIndex = 0;
                alertDialog.dismiss();
            }
        });
        tv_playingVoice = dialog.findViewById(R.id.time);
        final Button start = dialog.findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(voicePlayingFlag == false){

                   if(voicePlayer == null){
                       if(voicePlayTimer == null){
                           voicePlayTimerTask = new TimerTask() {
                               @Override
                               public void run() {
                                   handleVoicePlaying.sendEmptyMessage(0);

                               }
                           };
                           voicePlayTimer = new Timer();
                           voicePlayTimer.schedule(voicePlayTimerTask,0,1000);
                       }
                       voicePlayer = new MediaPlayer();
                       try {
                           voicePlayer.setDataSource(voicePlayerPath);
                           voicePlayer.prepare();
                           int time = voicePlayer.getDuration()/1000+1;
                           int hour = time/60;
                           int scend = time%60;
                           playingVoiceTimes = ""+hour+":"+scend;
                           voicePlayer.start();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                       voicePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                           @Override
                           public void onCompletion(MediaPlayer mediaPlayer) {
                               voicePlayer = null;
                               playingVoiceIndex = 0;
                               voicePlayingFlag = false;
                               start.setText("开始播放");
                               Toast.makeText(RecordDetailActivity.this, "播放完成", Toast.LENGTH_SHORT).show();
                           }
                       });

                   }else {
                       voicePlayer.start();
                   }
                    voicePlayingFlag = true;
                    start.setText("暂停播放");
                }else{
                    voicePlayer.pause();
                    voicePlayingFlag = false;
                    start.setText("开始播放");
                }

            }
        });
    }

    private void showImageDetail(String title, final Bitmap bitmap){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View dialog = LayoutInflater.from(this).inflate(R.layout.img_info_dialog,null);
        alertDialog.setView(dialog);
        alertDialog.show();
        ImageView img = dialog.findViewById(R.id.img);
        TextView tv_title = dialog.findViewById(R.id.title);
        tv_title.setText(title);
        img.setImageBitmap(bitmap);
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                bitmap.recycle();
                System.gc();
            }
        });
    }

    private void initDataBase() {
        openHelper = new tableDetailDataBaseAdapterOpenHelper(RecordDetailActivity.this,"detailDB",null,1);
        sqLiteDatabase = openHelper.getReadableDatabase();
        allData = openHelper.getDetialListByRecordName(sqLiteDatabase,selectedRecordName);
        Log.e("makerLog","num:"+allData.size());

    }
    private void showVideoDialog(){
        final String videoPath = tempFile.getAbsolutePath();
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View dialog = LayoutInflater.from(this).inflate(R.layout.video_dialog,null);
        alertDialog.setView(dialog);
        alertDialog.show();
        TextView dl_cancle = dialog.findViewById(R.id.tv_cancle);
        TextView dl_sure = dialog.findViewById(R.id.tv_sure);
        final EditText dl_title = dialog.findViewById(R.id.tv_title);
        TextView dl_video = dialog.findViewById(R.id.tv_video);
        dl_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserAction.selectedVideoPath = tempFile.getAbsolutePath();
                startActivity(new Intent(RecordDetailActivity.this,VideoPlayActivity.class));
            }
        });
//        Log.e("makerLog","size"+bitmapTurnString(bitmap).length());
        dl_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        dl_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean flag =  openHelper.insertDetail(dl_title.getText().toString(),videoPath,"video",selectedRecordName,sqLiteDatabase);
                if(flag){
                    alertDialog.dismiss();
                    initDataBase();
                    initDetailView();
                    Toast.makeText(RecordDetailActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(RecordDetailActivity.this, "添加失败，请检查是否重名", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dl_title.setText(UserAction.getSystemTime());

    }
    private void showBitMapDialog(final Bitmap bitmap){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View dialog = LayoutInflater.from(this).inflate(R.layout.dialog_bitmap_alert,null);
        alertDialog.setView(dialog);
        alertDialog.show();
        TextView dl_cancle = dialog.findViewById(R.id.tv_cancle);
        TextView dl_sure = dialog.findViewById(R.id.tv_sure);
        final EditText dl_title = dialog.findViewById(R.id.tv_title);
        ImageView dl_img = dialog.findViewById(R.id.img);
        dl_img.setImageBitmap(bitmap);
//        Log.e("makerLog","size"+bitmapTurnString(bitmap).length());
        dl_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        dl_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               boolean flag =  openHelper.insertDetail(dl_title.getText().toString(),outputImage.getPath(),"img",selectedRecordName,sqLiteDatabase);
               if(flag){
                   alertDialog.dismiss();
                   initDataBase();
                   initDetailView();
                   Toast.makeText(RecordDetailActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
               }else {
                   Toast.makeText(RecordDetailActivity.this, "添加失败，请检查是否重名", Toast.LENGTH_SHORT).show();
               }
            }
        });
        dl_title.setText(UserAction.getSystemTime());

    }
    private String bitmapTurnString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return Base64.encodeToString(appicon, Base64.DEFAULT);
    }
    private Bitmap stringTurnBitmap(String str){
        Bitmap bitmap = null;
        try{
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 1;
            byte[] bitmapArray;
            bitmapArray = Base64.decode(str, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
                    bitmapArray.length,opts);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;

    }
    private void initData() {
        tv_name.setText(UserAction.selectedRecord.get("name"));
        tv_info.setText(UserAction.selectedRecord.get("text"));
        tv_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTextDialog(UserAction.selectedRecord.get("text"));
            }
        });

        tv_date.setText(UserAction.selectedRecord.get("date"));
        re_addVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                openVideoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
                tempFile = new File(getExternalFilesDir(null),UserAction.getSystemTime()+"output_image.mp4");
                Uri fileVUri;
                if(Build.VERSION.SDK_INT >= 24){
                    //如果是android7.0以上需要使用FileProvider.getUriForFile()这个方法
                    fileVUri = FileProvider.getUriForFile(RecordDetailActivity.this,"maker.fileProvider",tempFile);
                }else {
                    //如果不是android7.0以上就直接调用Uri.fromFile(）方法
                    fileVUri = Uri.fromFile(tempFile);
                }
                openVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileVUri);
                startActivityForResult(openVideoIntent,22); // 参数常量为自定义的request code, 在取返回结果时有用

            }
        });
        tv_locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RecordDetailActivity.this,Home_Activity.class));
                Toast.makeText(RecordDetailActivity.this, "查看创建位置", Toast.LENGTH_SHORT).show();
            }
        });
        re_addimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                outputImage = new File(getExternalFilesDir(null),UserAction.getSystemTime()+"output_image.jpg");

                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }

                //将outputImage的路径由File对象转换成Uri对象
                if(Build.VERSION.SDK_INT >= 24){
                    //如果是android7.0以上需要使用FileProvider.getUriForFile()这个方法
                    imageUri = FileProvider.getUriForFile(RecordDetailActivity.this,"maker.fileProvider",outputImage);
                }else {
                    //如果不是android7.0以上就直接调用Uri.fromFile(）方法
                    imageUri = Uri.fromFile(outputImage);
                }
                Log.e("maker",""+imageUri);
                //启动相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent, 5);
            }
        });
        re_addVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               final AlertDialog alertDialog = new AlertDialog.Builder(RecordDetailActivity.this).create();
               View dialog = LayoutInflater.from(RecordDetailActivity.this).inflate(R.layout.dialog_voice,null);
               alertDialog.setView(dialog);
               alertDialog.setCanceledOnTouchOutside(false);
               alertDialog.show();
                tv_time = dialog.findViewById(R.id.time);
                final EditText ed_name = dialog.findViewById(R.id.name);
                ed_name.setText(UserAction.getSystemTime());
                btn_control = dialog.findViewById(R.id.start);
                btn_control.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!isStart){
                            startRecord();
                            btn_control.setText("停止录制");
                            isStart = true;
                            timer  = null;
                            timerTask = null;
                            voice_time = 0;
                            timerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    handlerCodling.sendEmptyMessage(0);

                                }
                            };

                            timer = new Timer();
                            timer.schedule(timerTask,0,1000);
                        }else {
                            stopRecord();
                            btn_control.setText("录制完成(点击添加，长按关闭)");
                            isStart = false;
                            timer.cancel();
                            btn_control.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View view) {
                                    alertDialog.dismiss();
                                    Toast.makeText(RecordDetailActivity.this, "手动取消", Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                            });
                            btn_control.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    boolean flag =  openHelper.insertDetail(ed_name.getText().toString(),soundFile.getAbsolutePath(),"voice",selectedRecordName,sqLiteDatabase);
                                    if(flag){
                                        alertDialog.dismiss();
                                        initDataBase();
                                        initDetailView();
                                        Toast.makeText(RecordDetailActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(RecordDetailActivity.this, "添加失败，请检查是否重名", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });


                        }

                    }
                });
            }
        });
        re_addtxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTxtDialog();
            }
        });
    }

    private void showAddTxtDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View dialog = LayoutInflater.from(this).inflate(R.layout.txt_dialog,null);
        alertDialog.setView(dialog);
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
        final EditText title = dialog.findViewById(R.id.tv_title);
        final EditText content = dialog.findViewById(R.id.tv_content);
        TextView cancle = dialog.findViewById(R.id.tv_cancle);
        TextView sure = dialog.findViewById(R.id.tv_sure);
        title.setText(UserAction.getSystemTime());
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();

            }
        });
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = content.getText().toString();
                boolean flag =  openHelper.insertDetail(title.getText().toString(),str,"txt",selectedRecordName,sqLiteDatabase);
                if(flag){
                    alertDialog.dismiss();
                    initDataBase();
                    initDetailView();
                    Toast.makeText(RecordDetailActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(RecordDetailActivity.this, "添加失败，请检查是否重名", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void showAddTxtViewDialog(String title,String content) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View dialog = LayoutInflater.from(this).inflate(R.layout.txt_dialog_view,null);
        alertDialog.setView(dialog);
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
         TextView tv_title = dialog.findViewById(R.id.tv_title);
         TextView tv_content = dialog.findViewById(R.id.tv_content);

        TextView sure = dialog.findViewById(R.id.close);
        tv_title.setText(title);
        tv_content.setText(content);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              alertDialog.dismiss();
            }
        });
    }

    private void initView() {
        lin_txt = findViewById(R.id.lin_txt);
        tv_date = findViewById(R.id.tv_date);
        tv_info = findViewById(R.id.tv_info);
        tv_name = findViewById(R.id.tv_name);
        tv_locate = findViewById(R.id.tv_location);
        re_addimg = findViewById(R.id.re_addimg);
        imgGrid = findViewById(R.id.img_grid);
        re_addVideo = findViewById(R.id.addVideo);
        lv_vedio = findViewById(R.id.lv_video);
        tv_img_num = findViewById(R.id.tv_img_num);
        tv_txt_num = findViewById(R.id.tv_txt_num);
        tv_video_num = findViewById(R.id.tv_video_num);
        tv_voice_num = findViewById(R.id.tv_voice_num);
        re_addVoice = findViewById(R.id.addVoice);
        lv_voice = findViewById(R.id.lv_voice);
        re_addtxt = findViewById(R.id.re_addtxt);
        lv_txt = findViewById(R.id.lv_txt);
    }

    private void showTextDialog(String arg) {
         AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View view = LayoutInflater.from(this).inflate(R.layout.record_text_dialog,null);
        alertDialog.setView(view);
        alertDialog.show();
        TextView content = view.findViewById(R.id.content);
        content.setText(arg);
    }

    private void showChangeDialog(){
         AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View view = LayoutInflater.from(this).inflate(R.layout.add_record_dialog,null);
        alertDialog.setView(view);
        alertDialog.show();
    }
}
