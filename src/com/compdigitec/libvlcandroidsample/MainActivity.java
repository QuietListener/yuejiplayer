package com.compdigitec.libvlcandroidsample;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.File;
import java.util.Date;

public class MainActivity extends Activity {
    public final static String TAG = "MainActivity";

    DirectoryAdapter mAdapter;
    LibVLC mLibVLC = null;
    MediaPlayer mMediaPlayer = null;
    DBHelper helper=null;

    boolean mPlayingVideo = false; // Don't destroy libVLC if the video activity is playing.

    View.OnClickListener mSimpleListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // Build the path to the media file
            String amp3 = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/a.mp4";
            String srt = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/a.srt";

            // Play the path. See the method for details.
            playMediaAtPath(amp3);
        }
    };

    /**
     * Demonstrates how to play a certain media at a given path.
     * TODO: demonstrate other LibVLC features like media lists, etc.
     */
    private void playMediaAtPath(String path) {
        // To play with LibVLC, we need a media player object.
        // Let's get one, if needed.
        if(mMediaPlayer == null)
            mMediaPlayer = new MediaPlayer(mLibVLC);

        // Sanity check - make sure that the file exists.
        if(!new File(path).exists()) {
            Toast.makeText(
                    MainActivity.this,
                    path + " does not exist!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Create a new Media object for the file.
        // Each media - a song, video, or stream is represented by a Media object for LibVLC.
        Media m = new Media(mLibVLC, path);

        // Tell the media player to play the new Media.
        mMediaPlayer.setMedia(m);

        // Finally, play it!
        mMediaPlayer.play();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the LibVLC multimedia framework.
        // This is required before doing anything with LibVLC.

        try {
                if(helper == null)
                     helper = new DBHelper(this);
                helper.createDataBase();


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            mLibVLC = new LibVLC(this);
        } catch(IllegalStateException e) {
            Toast.makeText(MainActivity.this,
                    "Error initializing the libVLC multimedia framework!",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the UI elements.
        mAdapter = new DirectoryAdapter();
        Button btn_test = (Button)findViewById(R.id.main_btn);
        btn_test.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {


                SQLiteDatabase db = MainActivity.this.helper.getWritableDatabase();


                ContentValues cv = new ContentValues();
                cv.put("word",new Date().getTime()+"");
                cv.put("mean_cn", new Date().getTime()+"");
                long ret = db.insert("word",null,cv);

                db.close();
                db = null;

                db = MainActivity.this.helper.getReadableDatabase();
                Cursor cursor = db.query("word",null,null,null,null,null,null);

                String words = "";

                //使用cursor.moveToNext()把游标下移一行。游标默认在第一行的上一行。
                while (cursor.moveToNext()) {
                    //使用GetString获取列中的值。参数为使用cursor.getColumnIndex("name")获取的序号。
                    String word =cursor.getString(cursor.getColumnIndex("mean_cn"));
                    words += (word+";");
                }
                db.close();
                Toast.makeText(MainActivity.this, words,Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        Button load_a_mp3 = (Button) findViewById(R.id.load_a_mp3);
        load_a_mp3.setOnClickListener(mSimpleListener);
        final ListView mediaView = (ListView) findViewById(R.id.mediaView);
        mediaView.setAdapter(mAdapter);
        mediaView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                    int position, long arg3) {
                if (mAdapter.isAudioMode()) {
                    playMediaAtPath((String) mAdapter.getItem(position));
                } else {
                    Intent intent = new Intent(MainActivity.this, VideoActivity.class);
                    intent.putExtra(VideoActivity.LOCATION, (String) mAdapter.getItem(position));
                    mPlayingVideo = true;
                    startActivity(intent);
                }
            }
        });
        RadioButton radioAudio = (RadioButton)findViewById(R.id.radioAudio);
        radioAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.setAudioMode(true);
                mAdapter.refresh();
            }
        });
        RadioButton radioVideo = (RadioButton)findViewById(R.id.radioVideo);
        radioVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.setAudioMode(false);
                mAdapter.refresh();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mPlayingVideo = false;
    };

    @Override
    public void onStop() {
        super.onStop();
        if(!mPlayingVideo) {
            if (mMediaPlayer!=null)
                mMediaPlayer.stop();
            if(mLibVLC!=null)
                mLibVLC.release();
            mLibVLC = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.action_settings:
            Log.d(TAG, "Setting item selected.");
            return true;
        case R.id.action_refresh:
            mAdapter.refresh();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
