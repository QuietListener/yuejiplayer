package com.compdigitec.libvlcandroidsample;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

import com.compdigitec.libvlcandroidsample.bean.Word;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.Extensions;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {
    public final static String TAG = "MainActivity";

    DirectoryAdapter mAdapter;
    LibVLC mLibVLC = null;
    MediaPlayer mMediaPlayer = null;
    DBHelper helper=null;

    boolean mPlayingVideo = false; // Don't destroy libVLC if the video activity is playing.
    public static final int FILE_SELECT_CODE = 1234321;

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
//        Button btn_test = (Button)findViewById(R.id.main_btn);
//
//
//        Button load_a_mp3 = (Button) findViewById(R.id.load_a_mp3);
//        load_a_mp3.setOnClickListener(mSimpleListener);



//        Button button = (Button) findViewById(R.id.main_btn);
//        button.setOnClickListener(new View.OnClickListener() {
//
//            private void showFileChooser() {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//
//                try {
//                    MainActivity.this.startActivityForResult( Intent.createChooser(intent, "选择视频所在的文件夹"), FILE_SELECT_CODE);
//                } catch (android.content.ActivityNotFoundException ex) {
//                    Toast.makeText(MainActivity.this, "请安装一个文件选择器.",  Toast.LENGTH_SHORT).show();
//                }
//            }
//
//
//            @Override
//            public void onClick(View view) {
//               showFileChooser();
//            }
//        });

        final ListView mediaView = (ListView) findViewById(R.id.mediaView);
        mediaView.setAdapter(mAdapter);
        mediaView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                //返回上一级
                if(position == 0)
                {
                    mAdapter.goUp();
                    return;
                }

                File f = (File)mAdapter.getItem(position);
                String file_path = f.getAbsolutePath();

                if(f.isDirectory())
                {
                    mAdapter.goTo(f);
                    return;
                }
//              if (mAdapter.isAudioMode()) {
//                    playMediaAtPath((String) mAdapter.getItem(position));
//              }

                String ext = Utils.fileExt(file_path);

                if(ext == null)
                    return;

                if(Extensions.VIDEO.contains(ext))
                {
                    Intent intent = new Intent(MainActivity.this, VideoActivity.class);
                    intent.putExtra(VideoActivity.LOCATION, file_path);
                    mPlayingVideo = true;
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "请选择视频文件，字幕与视频同名喔~" ,Toast.LENGTH_SHORT).show();
                }
            }
        });
//        RadioButton radioAudio = (RadioButton)findViewById(R.id.radioAudio);
//        radioAudio.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mAdapter.setAudioMode(true);
//                mAdapter.refresh();
//            }
//        });
//        RadioButton radioVideo = (RadioButton)findViewById(R.id.radioVideo);
//        radioVideo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mAdapter.setAudioMode(false);
//                mAdapter.refresh();
//            }
//        });
    }

//
//
//    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
//        switch (requestCode)
//        {
//            case FILE_SELECT_CODE:
//                if (resultCode == RESULT_OK)
//                {
//                    Uri uri = data.getData();
//                    Toast.makeText(this,"file:"+uri,Toast.LENGTH_SHORT);
//                }
//                break;
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

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
