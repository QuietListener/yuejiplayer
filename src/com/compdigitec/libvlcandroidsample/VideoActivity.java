package com.compdigitec.libvlcandroidsample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.AndroidUtil;
import java.io.File;
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

            if((e1.getX()-e2.getX())>FLING_MIN_DISTANCE && Math.abs(velocityX)>FLING_MIN_VELOCITY)
            {
                Toast.makeText(VideoActivity.this, "向左滑动", Toast.LENGTH_SHORT).show();
                long time = mMediaPlayer.getTime();
                time = time - 5000;
                mMediaPlayer.setTime(time);
            }
            else if((e2.getX()-e1.getX())>FLING_MIN_DISTANCE && Math.abs(velocityX)>FLING_MIN_VELOCITY)
            {
                Toast.makeText(VideoActivity.this, "向右滑动", Toast.LENGTH_SHORT).show();
                long time = mMediaPlayer.getTime();
                time = time + 5000;
                mMediaPlayer.setTime(time);
            }
            return false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample);

        // Receive path to play from intent
        Intent intent = getIntent();
        mFilePath = intent.getExtras().getString(LOCATION);
        //srtFilePath = mFilePath.replace(".mp4",".srt").replace(".avi",".srt");

        srtFilePath = mFilePath.replace(".mp4",".srt").replace(".avi",".srt");

        Log.d(TAG, "Playing back " + mFilePath);

        mSurface = (SurfaceView) findViewById(R.id.surface);
        mSubtitleView = (SubtitleView) findViewById(R.id.subtitle_view);
        mSubtitleView.setVisibility(View.VISIBLE);
        //subtitleView1 =  (SurfaceView) findViewById(R.id.subtitle_view1);

        mSubtitleView.setSubSource(srtFilePath , null);

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

        createPlayer(mFilePath);


        mSubtitleView.setPlayer(mMediaPlayer);
        mSubtitleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                String text = ((TextView)view).getText().toString();
                Toast.makeText(VideoActivity.this, text, Toast.LENGTH_SHORT).show();
                dialog(text);
                return false;
            }
        });


    }

    protected void dialog(String text) {

        String[] words={};
        if(text!=null)
        {
            words = text.split(" ");
        }
        LayoutInflater mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.alert_dialog, null);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.id_recordlayout);
        layout.setPadding(2, 2, 2, 2);
        Pattern p = Pattern.compile("[a-zA-Z|-]+-*[a-zA-Z|-]+");
        for (int i = 0; i < words.length; i++) {
            String name = words[i];
            name = name.replaceAll("[\'|\\.|\"]","").trim();
            if (name.length() <= 2 || !p.matcher(name).matches())
                continue;
            TextView tv = new TextView(this);
            tv.setTextColor(Color.WHITE);
            tv.setText(words[i]);
            layout.addView(tv);
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
        builder.create().show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    protected void onResume() {
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

    private void createPlayer(String media) {
        releasePlayer();
        try {
            if (media.length() > 0) {
                Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();
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
//                    MediaPlayer.TrackDescription[] tds = player.mMediaPlayer.getSpuTracks();
//
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
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                    Log.d(TAG,"");
                    break;

                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                case MediaPlayer.Event.TimeChanged:
                    Log.d(TAG, "+++time---"+player.mMediaPlayer.getTime());
                    long time  = player.mMediaPlayer.getTime();
                    int total_time = player.seek_bar.getMax();
                    int progress = (int)(time/1000);
                    player.seek_bar.setProgress(progress);


                    MediaPlayer.TrackDescription[] tracks = player.mMediaPlayer.getSpuTracks();
                    Log.d(TAG,tracks+"");
                    break;
                default:
                    break;
            }
        }
    }

}