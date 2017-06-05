package com.compdigitec.libvlcandroidsample;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.compdigitec.libvlcandroidsample.bean.Record;
import com.compdigitec.libvlcandroidsample.bean.Word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by junjun on 2017/5/4.
 */

public class Dao {


    DBHelper helper=null;
    public static final String ORDER_DESC = "DESC";
    public static final String ORDER_ASC = "ASC";
    static private Dao instance = null;
    static Context ctx;
    static String KEY_VIDEO_PATH = "video_pathes";

    public static String KEY_SUB_SHOW = "key_sub_show";
    public static String KEY_SUB_SHOW_EN_ONLY = "key_sub_show_en_only";
    public static String KEY_SUB_SHOW_ALL = "key_sub_show_all";


    static String TAG = "Dao";
    static  Dao getInstance(Context ctx)
    {
        try {
            if (instance == null) {
                synchronized (Dao.class) {
                    if (instance == null) {
                        Dao.ctx = ctx;
                        instance = new Dao(ctx.getApplicationContext());
                    }
                }
            }
        }
        catch(Exception e)
        {
            Log.e(TAG,e.getLocalizedMessage());
            Log.e(TAG,e.getStackTrace().toString());
        }

        return instance;
    }

    private Dao(Context ctx)throws Exception
    {
        this.helper = new DBHelper(ctx);
        this.helper.createDataBase();
    }

    public Word findWordsByWord(String w)
    {
        SQLiteDatabase db = null;
        Word word = null;
        if(w == null )
        {
            return word;
        }

        try
        {

            db = this.helper.getReadableDatabase();
            StringBuffer sqlb = new StringBuffer("word = ?");

            String  sql = sqlb.toString();

            String [] params = new String [1];
            params[0] = w;
            Cursor cursor = db.query(false,"words",null,sql,params,null,null,null,null);
            List<Word> words = wordMap(cursor);
            if(words == null || words.size() == 0 )
            {
                return null;
            }

            return words.get(0);
        }
        finally
        {
            if(db != null)
            {
                db.close();
            }
        }
    }

    private List<Word> wordMap(Cursor cursor)
    {
        List<Word> wordArray = new ArrayList<>();
        List<String> wses = new ArrayList<>();
        //使用cursor.moveToNext()把游标下移一行。游标默认在第一行的上一行。
        while (cursor.moveToNext()) {
            //使用GetString获取列中的值。参数为使用cursor.getColumnIndex("name")获取的序号。
            Integer  id = cursor.getInt(cursor.getColumnIndex("_id"));
            String word = cursor.getString(cursor.getColumnIndex("word"));
            String mean_cn = cursor.getString(cursor.getColumnIndex("mean_cn"));
            String accent = cursor.getString(cursor.getColumnIndex("accent"));
            String audio_file = cursor.getString(cursor.getColumnIndex("audio_file"));

            String word_variants = cursor.getString(cursor.getColumnIndex("word_variants"));
            if(wses.contains(word))
                continue;
            wses.add(word);
            Word w = new Word(id, word, mean_cn, accent, audio_file,word_variants);
            wordArray.add(w);
        }

        return wordArray;
    }

    public List<Word> findWordsLikely(String [] words)
    {
        SQLiteDatabase db = null;
        List<Word> wordArray = new ArrayList<>();
        if(words == null || words.length == 0)
        {
            return wordArray;
        }

        try
        {

            db = this.helper.getReadableDatabase();
            StringBuffer sqlb = new StringBuffer(" word = ? or word_variants like ?");

            String  sql = sqlb.toString();

            for(String w_ : words)
            {
                String [] params = new String [2];
                params[0] = w_;
                params[1] = "%"+w_+"%";
                Cursor cursor = db.query(false,"words",null,sql,params,null,null,null,null);
                List<Word> words_ = wordMap(cursor);

                for(Word w:words_)
                {
                    List<String> wvs_ = new ArrayList<>();
                    String wvss = w.getWord_variants();

                    if(wvss != null && wvss.length() > 0)
                    {
                        String[] wvs = wvss.split(",");
                        wvs_ = Arrays.asList(wvs);
                    }

                    if(w.getWord().equals(w_) || wvs_.contains(w_))
                    {
                        wordArray.add(w);
                    }
                }


            }
        }
        finally
        {
            if(db != null)
            {
                db.close();
            }
        }

        return wordArray;

    }


    public int recordsCount() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.helper.getReadableDatabase();
            cursor = db.rawQuery("select count(*) from records",null);
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            return count;
        }
        finally
        {
            if(cursor != null)
            {
                cursor.close();
            }

            if(db != null)
            {
                db.close();
            }
        }
    }
        /**
         *
         * @param offset
         * @param limit
         * @param order  order ORDER_DESC or ORDER_ASC
         * @return
         */
        public List<Record> findRecords(int offset,int limit, String order)
        {

            if(order == null)
            {
                order = ORDER_DESC;
            }
            SQLiteDatabase db = null;
            List<Record> ret = new ArrayList<>();

        try {
            db = this.helper.getReadableDatabase();

            Cursor cursor = db.rawQuery("select * from records order by `date`  "+order+"  limit ?, ? ",new String []{ offset+"",limit+""});

            while (cursor.moveToNext())
            {
                Integer id = cursor.getInt(cursor.getColumnIndex("_id"));
                Integer word_id = cursor.getInt(cursor.getColumnIndex("word_id"));
                String word = cursor.getString(cursor.getColumnIndex("word"));
                String movie_path = cursor.getString(cursor.getColumnIndex("movie_path"));
                String movie_name = cursor.getString(cursor.getColumnIndex("movie_name"));

                Long  datel = cursor.getLong(cursor.getColumnIndex("date"));

                Date date = new Date(datel);

                String subtitle = cursor.getString(cursor.getColumnIndex("subtitle"));
                int status = cursor.getInt(cursor.getColumnIndex("status"));
                long time_from = cursor.getLong(cursor.getColumnIndex("time_from"));
                long time_to = cursor.getLong(cursor.getColumnIndex("time_to"));

                Record r = new Record( id,  word_id,  word,  movie_path,  movie_name,  date,  subtitle,time_from,time_to,  status) ;

                ret.add(r);
            }


        }
        finally
        {
            if(db != null)
            {
                db.close();
            }
        }

        return ret;
    }



    public Record saveRecord(Record r)
    {
        SQLiteDatabase db = null;

        try {

            db = this.helper.getReadableDatabase();
            ContentValues contentValues = new ContentValues();


            contentValues.put("word_id",r.getId());
            contentValues.put("word",r.getWord());
            contentValues.put("movie_path",r.getMovie_path());
            contentValues.put("movie_name",r.getMovie_name());
            contentValues.put("date",r.getDate().getTime());
            contentValues.put("time_from",r.getTime_from());
            contentValues.put("time_to",r.getTime_to());
            contentValues.put("subtitle",r.getSubtitle());
            contentValues.put("status",r.getStatus());

            long id = db.insert("records",  null,  contentValues);
            r.setId((int)id);
        }
        finally
        {
            if(db != null)
            {
                db.close();
            }
        }

        return r;
    }


    public Set<String> saveVideoPathes(String video_path, String srt_path)
    {
        SharedPreferences preferences=ctx.getSharedPreferences("juduvideo",Context.MODE_PRIVATE);
        Set<String> pathes = preferences.getStringSet(KEY_VIDEO_PATH,null);

        pathes = (pathes == null) ? new TreeSet<String>() : pathes;
        String data = video_path+"|"+srt_path;

        if(!pathes.contains(data))
        {
            pathes.add(data);
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(KEY_VIDEO_PATH,pathes);
        editor.commit();

        return pathes;
    }


    public Set<String> getVideoPathes() {
        SharedPreferences preferences = ctx.getSharedPreferences("juduvideo", Context.MODE_PRIVATE);
        Set<String> pathes = preferences.getStringSet(KEY_VIDEO_PATH, null);
        return pathes;
    }

    public String getSubShow() {
        SharedPreferences preferences = ctx.getSharedPreferences("juduvideo", Context.MODE_PRIVATE);
        String ret = preferences.getString(KEY_SUB_SHOW,KEY_SUB_SHOW_ALL);
        return ret;
    }

    public String  confSubShow(String showStrategy)
    {
        SharedPreferences preferences=ctx.getSharedPreferences("juduvideo",Context.MODE_PRIVATE);

        String old_sub = getSubShow();
        if(old_sub != null && old_sub.equals(showStrategy))
        {
            return old_sub;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_SUB_SHOW,showStrategy);
        editor.apply();

        return showStrategy;
    }



}
