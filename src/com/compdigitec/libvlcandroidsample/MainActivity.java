package com.compdigitec.libvlcandroidsample;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.compdigitec.libvlcandroidsample.bean.Record;
import com.compdigitec.libvlcandroidsample.bean.Word;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.Extensions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {
    public final static String TAG = "MainActivity";

    Button choose_movie_btn = null;
    Button goto_word_review_btn = null;
    public static String pre_path = null;
    public static long pre_stop_time = 0l;

    private ListView lv = null;
    private ListAdapter la;
    private RadioGroup group = null;

    private RadioButton radioShowEn = null;
    private RadioButton radioShowAll = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choose_movie_btn = (Button) findViewById(R.id.choose_movie);
        choose_movie_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ChooseVideoActivity.class);
                startActivity(intent);
            }
        });

        goto_word_review_btn = (Button) findViewById(R.id.goto_word_review);
        goto_word_review_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ReviewActivity.class);
                startActivity(intent);
            }
        });



        this.radioShowEn = (RadioButton)this.findViewById(R.id.radioShowEn);
        this.radioShowAll = (RadioButton)this.findViewById(R.id.radioShowAll);

        String subshow = Dao.getInstance(MainActivity.this.getApplicationContext()).getSubShow();
        if(subshow.equals(Dao.KEY_SUB_SHOW_EN_ONLY))
        {
            this.radioShowEn.setChecked(true);
        }
        else
        {
            this.radioShowAll.setChecked(true);
        }


        this.group = (RadioGroup)this.findViewById(R.id.radioGroup);

        //绑定一个匿名监听器
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                int radioButtonId = arg0.getCheckedRadioButtonId();
                RadioButton rb = (RadioButton)MainActivity.this.findViewById(radioButtonId);
                String str = rb.getText().toString();

                if(str.equals(getString(R.string.sub_show_all)))
                {
                    Dao.getInstance(MainActivity.this.getApplicationContext()).confSubShow(Dao.KEY_SUB_SHOW_ALL);
                }
                else if(str.equals(getString(R.string.sub_show_en_only)))
                {
                    Dao.getInstance(MainActivity.this.getApplicationContext()).confSubShow(Dao.KEY_SUB_SHOW_EN_ONLY);
                }
            }
        });

        lv = (ListView)findViewById(R.id.pre_movie_path);
        la = new ListAdapter(getApplicationContext(),10);

        lv.setAdapter(la);
        la.notifyDataSetChanged();
    }

    @Override
    public void onResume()
    {
        la = new ListAdapter(getApplicationContext(),10);
        lv.setAdapter(la);
        la.notifyDataSetChanged();
        super.onResume();
    };

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return true;
    }

    static class ViewHolder{
        public TextView video_path;
        public TextView srt_path;
    }

    class ListAdapter  extends BaseAdapter
    {
        private List<String> pathes = null;
        private LayoutInflater mInflater;
        private Context ctx = null;
        private int count = 4;

        public ListAdapter(Context ctx,int count)
        {
            this.ctx = ctx;
            this.mInflater = LayoutInflater.from(ctx);
            Set<String > pathset = Dao.getInstance(ctx).getVideoPathes();
            if (pathset == null)
            {
                pathes = new ArrayList<String>(0);
            }
            else
            {
                pathes = new ArrayList<String>(pathset);
            }

        }

        @Override
        public int getCount() {
            return pathes.size() > this.count ? this.count : pathes.size();
        }

        @Override
        public Object getItem(int i)
        {
            return pathes.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {

            ViewHolder holder = null;
            if (convertView == null)
            {
                holder=new ViewHolder();
                convertView = mInflater.inflate(R.layout.video_path_item, null);
                holder.video_path = (TextView) convertView.findViewById(R.id.video_path);
                holder.srt_path = (TextView) convertView.findViewById(R.id.srt_path);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)convertView.getTag();
            }

            String  p = pathes.get(i);
            String[] ps = p.split("[\\|]");

            String video_path = (ps != null && ps.length >= 1) ? ps[0] : "没有视频喔";
            String srt_path = (ps != null && ps.length >= 2) ? ps[1] : "没有字幕喔";

            holder.video_path.setText(video_path);
            holder.srt_path.setText(srt_path);

            holder.video_path.setOnClickListener(new StartVideoListener(video_path));
            return convertView;
        }
    }


    class StartVideoListener implements View.OnClickListener {
        private String path  = null;

        public StartVideoListener(String path)
        {
            this.path = path;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, VideoActivity.class);
            intent.setData(Uri.fromFile(new File(this.path)));
            intent.putExtra(VideoActivity.LOCATION, pre_path);
            intent.putExtra("startAt", pre_stop_time);
            startActivity(intent);
        }
    }

}


