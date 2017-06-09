package com.compdigitec.libvlcandroidsample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.compdigitec.libvlcandroidsample.bean.Record;
import com.compdigitec.libvlcandroidsample.bean.Word;
//import com.xw.repo.BubbleSeekBar;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.AndroidUtil;
import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class VideoActivity extends Activity implements IVLCVout.Callback,SurfaceHolder.Callback{
    public final static String TAG = "VideoActivity";

    public final static String LOCATION = "com.compdigitec.libvlcandroidsample.VideoActivity.location";

    private String mFilePath;
    private String srtFilePath;

    // display surface
    private SurfaceView mSurface;
    private SubtitleView mSubtitleView;
    private SurfaceView subtitleView1;

    private SurfaceHolder holder;

    private SeekBar seek_bar;
    private int total_second = 0;
    // media player
    private LibVLC libvlc;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;
    private Handler mHandler = new Handler();

    private GestureDetectorCompat mDetector;

    private AlertDialog alertDialog;

    DBHelper helper = null;

    private boolean startAtFlag = false;
    private Long startAt = 0l;

    private String sub_show = Dao.KEY_SUB_SHOW_EN_ONLY;

    private Tts tts = null;
    public MediaPlayer getMediaPlayer()
    {
        return this.mMediaPlayer;
    }
    /*************
     * Activity
     *************/


    class DefaultGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
            final int FLING_MIN_DISTANCE=100;//X或者y轴上移动的距离(像素)
            final int FLING_MIN_VELOCITY=200;//x或者y轴上的移动速度(像素/秒)

            long time = mMediaPlayer.getTime();
            if((e1.getX()-e2.getX())>FLING_MIN_DISTANCE && Math.abs(velocityX)>FLING_MIN_VELOCITY)
            {
                long pre_time = mSubtitleView.getPre_time();
                if(pre_time < 0l)
                {
                    time = time - 5000;
                    Utils.displayShortToask(VideoActivity.this,"左:上一个5秒");
                }
                else
                {
                    time = pre_time;
                    Utils.displayShortToask(VideoActivity.this,"左滑:上一字幕");
                }

                mMediaPlayer.setTime(time);
            }
            else if((e2.getX()-e1.getX())>FLING_MIN_DISTANCE && Math.abs(velocityX)>FLING_MIN_VELOCITY)
            {

                Long next_time = mSubtitleView.getNext_time();
                if(next_time < 0l)
                {
                    time =time+5000;
                    Utils.displayShortToask(VideoActivity.this,"右滑:下一个5秒");
                }
                else
                {
                    time = next_time-10;
                    Utils.displayShortToask(VideoActivity.this,"右滑:下一字幕");
                }
                //time = time + 5000;
                mMediaPlayer.setTime(time);
            }

            mMediaPlayer.play();
            return false;

        }

        public boolean onSingleTapConfirmed(MotionEvent e) {

            if(alertDialog == null || !alertDialog.isShowing())
            {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mSubtitleView.setOnlyShowEn(false);
                    Utils.displayShortToask(getApplicationContext(),"paused");

                } else {
                    mMediaPlayer.play();
                    boolean onlyShowEn = sub_show.equals(Dao.KEY_SUB_SHOW_ALL) ? false : true;
                    mSubtitleView.setOnlyShowEn(onlyShowEn);
                    Utils.displayShortToask(getApplicationContext(),"playing");

                }
            }

            if(! seek_bar.isShown())
            {
                seek_bar.setVisibility(mSubtitleView.VISIBLE);
            }

            //Toast.makeText(VideoActivity.this,"onSingleTapConfirmed",Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    class AddWordListener implements View.OnClickListener {

        private Word word;
        private String mpath;
        SubtitleView.Line line;

        public AddWordListener(View view,Word word, String mpath, SubtitleView.Line line) {
            this.word = word;
            this.mpath = mpath;
            this.line = line;
        }

        @Override
        public void onClick(View view) {

            if(this.word.getWord() != null)
                tts.speak(this.word.getWord());

            File f = new File(this.mpath);
            String mname = f.getName();

            if(line == null) {
                return;
            }

            String text = line.getText();
            Dao dao = Dao.getInstance(VideoActivity.this.getApplicationContext());

            List<Record> rs = dao.findRecords(word.getWord(),mname,text);

            if(rs!=null && rs.size() > 0)
            {
                Utils.displayShortToask(getApplicationContext(),word.getWord()+"已经添加过了~");
                return;
            }

            Record r = new Record(-1, word.getId(), word.getWord(),  this.mpath, mname, new Date(), text, line.getFrom(), line.getTo(), 0);
            dao.saveRecord(r);

            Toast.makeText(getApplicationContext(),"添加"+word.getWord()+"到生词本",Toast.LENGTH_SHORT).show();
            view.setBackgroundColor(Color.GRAY);

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if(helper == null)
                helper = new DBHelper(this);
            helper.createDataBase();


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        setContentView(R.layout.sample);

        if(tts == null )
        {
            tts = new Tts(this);
        }

        // Receive path to play from intent
        Intent intent = getIntent();
        mFilePath = intent.getData().getPath();
        startAt = intent.getExtras().getLong("startAt");
        startAtFlag = false;
        if(startAt == null)
        {
            startAt = 0l;
        }
        else{
            startAtFlag = true;
        }

        //mFilePath = intent.getExtras().getString(LOCATION);
        //srtFilePath = mFilePath.replace(".mp4",".srt").replace(".avi",".srt");
        String ext_name = Utils.fileExt(mFilePath);
        srtFilePath = mFilePath.replace(ext_name,".srt");

        File srtFile = new File(srtFilePath);

        if(!srtFile.exists())
        {
            srtFilePath = "没有字幕文件";
        }

        //保存播放文件
        Dao.getInstance(getApplicationContext()).saveVideoPathes(mFilePath,srtFilePath);

        Log.d(TAG, "Playing back " + mFilePath);
        mSurface = (SurfaceView) findViewById(R.id.surface);
        mSubtitleView = (SubtitleView) findViewById(R.id.subtitle_view);
        mSubtitleView.setVisibility(View.VISIBLE);
        //subtitleView1 =  (SurfaceView) findViewById(R.id.subtitle_view1);

        mSubtitleView.setSubSource(srtFilePath , null);
        sub_show = Dao.getInstance(this.getApplicationContext()).getSubShow();
        boolean onlyShowEn = sub_show.equals(Dao.KEY_SUB_SHOW_ALL) ? false : true;
        mSubtitleView.setOnlyShowEn(onlyShowEn);

        seek_bar = (SeekBar)findViewById(R.id.seek_bar);

        holder = mSurface.getHolder();
        mDetector = new GestureDetectorCompat(this, new DefaultGestureDetector());

        mSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                mDetector.onTouchEvent(event);
                return true;
            }
        });
//        holder.addCallback(this);

        createPlayer(mFilePath,0l);


        mSubtitleView.setPlayer(mMediaPlayer);
        mSubtitleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                String text = ((TextView)view).getText().toString();
                //Toast.makeText(VideoActivity.this, text, Toast.LENGTH_SHORT).show();
                dialog(text);
                mMediaPlayer.pause();
                return false;
            }
        });

        Utils.statistics(getApplicationContext(),"video");

    }

    private  List<Word> queryDB(List<String> words)
    {
        List<Word> ws = new ArrayList<Word>();
        if(words.size() == 0)
            return ws;
        String [] w = new String[words.size()];
        ws=Dao.getInstance(this.getApplicationContext()).findWordsLikely(words.toArray(w));
        return ws;
    }

    protected void dialog(String text) {

        String[] words={};
        if(text!=null)
        {
            words = text.split(" ");
        }

        Pattern p = Pattern.compile("[a-zA-Z|-|']+");
        List<String> words_ = new ArrayList<String>();
        for (int i = 0; i < words.length; i++)
        {
            String name = words[i];
            name = name.replaceAll("['|'|，|,|\\.|\"|！|!|:|;|。|\\(|\\)]", "").trim().toLowerCase();
            if (name.length() <= 2 )
                continue;

            name = name.toLowerCase();
            words_.add(name);
        }

        if(words_.size() == 0)
            return;

        List<Word> wms = queryDB(words_);

        LayoutInflater mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.alert_dialog, null);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.id_recordlayout);
        layout.setPadding(2, 2, 2, 2);

        SubtitleView.Line line = mSubtitleView.getTimedText();

        LayoutInflater li = LayoutInflater.from(this.getApplicationContext());
        View tip = li.inflate(R.layout.dialog_words_head_row,null);

        tip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(alertDialog!=null)
                {
                    alertDialog.dismiss();;
                }
            }
        });

        layout.addView(tip);

        for(Word w: wms)
        {
            LinearLayout ll = new LinearLayout(this);
            ll.setLayoutParams(new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
            ));

            TextView tv = new TextView(this);

            tv.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
            );
            tv.setLayoutParams(param);

            Button btn = new Button(this);

            param = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT

            );


            btn.setText("+");
            btn.setLayoutParams(param);

            btn.setPadding(1,1,1,1);
            ll.addView(tv);

            //ll.addView(btn);

            String mean = w.getMean_cn();
            if(mean != null)
                mean = mean.replaceAll("\r\n"," ");
            String word_mean = "<font color='#2196F3'>"+w.getWord()+"</font>     "
                            +"<font>"+w.getAccent()+"</font> <br>"
                            + mean;
            Spanned sp =  Html.fromHtml(word_mean);

            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
            tv.setText(sp);
            tv.setPadding(1,1,1,4);
            layout.addView(ll);

            tv.setOnClickListener(new AddWordListener(tv,w,mFilePath,line));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
        //builder.setTitle("选择单词查询单词");
        builder.setView(view);

//         builder.setNegativeButton("继续", new DialogInterface.OnClickListener() {
//          @Override
//          public void onClick(DialogInterface dialog, int which) {
//           dialog.dismiss();
//          }
//         });
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    protected void onResume() {

        /**
         * 设置为横屏
         */
        if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        if(mSubtitleView!= null) {
            sub_show = Dao.getInstance(this.getApplicationContext()).getSubShow();
            boolean onlyShowEn = sub_show.equals(Dao.KEY_SUB_SHOW_ALL) ? false : true;
            mSubtitleView.setOnlyShowEn(onlyShowEn);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        tts.destroy();
        tts = null;
    }

    /*************
     * Surface
     *************/
    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        if(holder == null || mSurface == null)
            return;

        // get screen size
        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        // force surface buffer size
        holder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        LayoutParams lp = mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
    }

    /*************
     * Player
     *************/

    private void createPlayer(String media,Long startAt) {
        releasePlayer();
        try {
            if (media.length() > 0) {
//                Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
//                        0);
//                toast.show();
            }

            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            libvlc = new LibVLC(getApplicationContext(),options);

            holder.setKeepScreenOn(true);

            // Create media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);


            // Set up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();

            vout.setVideoView(mSurface);
            //vout.setSubtitlesView(subtitleView1);

            vout.addCallback(this);
            vout.attachViews();

            Media m = new Media(libvlc, media);
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();

        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    // TODO: handle this cleaner
    private void releasePlayer() {
        if (libvlc == null)
            return;
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        holder = null;
        libvlc.release();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    /*************
     * Events
     *************/

    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    @Override
    public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0)
            return;

        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        setSize(mVideoWidth, mVideoHeight);
    }

    public void surfaceCreated(SurfaceHolder var1){};

    public void surfaceChanged(SurfaceHolder var1, int var2, int var3, int var4){};

    public void surfaceDestroyed(SurfaceHolder var1){};

    @Override
    public void onSurfacesCreated(IVLCVout vout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout)
    {

    }
    private  class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<VideoActivity> mOwner;
        private int progress_show_count = 0;
        int SeekBarShowTime = 5;

        public MyPlayerListener(VideoActivity owner) {
            mOwner = new WeakReference<VideoActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            final VideoActivity player = mOwner.get();

            switch(event.type) {
                case MediaPlayer.Event.EndReached:
                    Log.d(TAG, "MediaPlayerEndReached");
                    player.releasePlayer();
                    break;
                case MediaPlayer.Event.Playing:


                    player.mMediaPlayer.setSpuTrack(-1);
                    MediaPlayer.TrackDescription[] tds = player.mMediaPlayer.getSpuTracks();

//                    Media.Slave slave = new Media.Slave(Media.Slave.Type.Subtitle,4,player.srtFilePath+".jpg");
//                    player.mMediaPlayer.getMedia().addSlave(slave);
//
//
//                    if(tds!= null && tds.length > 0)
//                    {
//                        for (int i = tds.length - 1; i >= 0; i--) {
//                            MediaPlayer.TrackDescription td = tds[i];
//                            if (td.id > 0) {
//                                player.mMediaPlayer.setSpuTrack(td.id);
//                                break;
//                            }
//                        }
//
//                        String td_ = "";
//                        for (int i = tds.length - 1; i >= 0; i--) {
//                            MediaPlayer.TrackDescription td = tds[i];
//                            td_+=td.id +";"+td.name+". ";
//                        }
//
//                        Toast.makeText(VideoActivity.this,td_,Toast.LENGTH_LONG).show();
//                    }

                    int total_second = (int)player.mMediaPlayer.getMedia().getDuration()/1000;
                    player.seek_bar.setMax(total_second);
                    player.seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            if(b == true)
                            {
                                player.mMediaPlayer.setTime(i*1000);
                                progress_show_count = 0;
                            }
                            else
                            {
                                progress_show_count += 1;
                            }

                            if(progress_show_count > SeekBarShowTime)
                            {
                                seek_bar.setVisibility(View.INVISIBLE);
                                progress_show_count = 0;
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            progress_show_count = 0;
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            progress_show_count = 0;
                        }
                    });


                    Log.d(TAG,"");
                    break;

                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                case MediaPlayer.Event.TimeChanged:

                    if(startAtFlag == true)
                    {
                        player.mMediaPlayer.setTime(startAt);
                        startAtFlag=false;
                    }

                    Log.d(TAG, "+++time---"+player.mMediaPlayer.getTime());
                    long time  = player.mMediaPlayer.getTime();
                    player.seek_bar.setProgress((int)(time/1000));

                    MainActivity.pre_stop_time = time;



                    MediaPlayer.TrackDescription[] tracks = player.mMediaPlayer.getSpuTracks();
                    Log.d(TAG,tracks+"");
                    break;
                default:
                    break;
            }
        }
    }

}
