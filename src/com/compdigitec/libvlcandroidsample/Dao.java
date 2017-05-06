package com.compdigitec.libvlcandroidsample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.compdigitec.libvlcandroidsample.bean.Record;
import com.compdigitec.libvlcandroidsample.bean.Word;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by junjun on 2017/5/4.
 */

public class Dao {

    DBHelper helper=null;
    public static final String ORDER_DESC = "DESC";
    public static final String ORDER_ASC = "ASC";
    static private Dao instance = null;

    static String TAG = "Dao";
    static  Dao getInstance(Context ctx)
    {
        try {
            if (instance == null) {
                synchronized (Dao.class) {
                    if (instance == null) {
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


    public List<Word> findWords(String [] words)
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
                //使用cursor.moveToNext()把游标下移一行。游标默认在第一行的上一行。
                while (cursor.moveToNext()) {
                    //使用GetString获取列中的值。参数为使用cursor.getColumnIndex("name")获取的序号。
                    Integer  id = cursor.getInt(cursor.getColumnIndex("_id"));
                    String word = cursor.getString(cursor.getColumnIndex("word"));
                    String mean_cn = cursor.getString(cursor.getColumnIndex("mean_cn"));
                    String accent = cursor.getString(cursor.getColumnIndex("accent"));
                    String audio_file = cursor.getString(cursor.getColumnIndex("audio_file"));

                    String word_variants = cursor.getString(cursor.getColumnIndex("word_variants"));

                    if(word !=null && word.equals(w_))
                    {
                        Word w = new Word(id, word, mean_cn, accent, audio_file);
                        wordArray.add(w);
                        break;
                    }
                    else if(word != null && !word.equals(w_) &&  word_variants != null )
                    {
                        String [] wvs =  word_variants.split(",");
                        for(String wv : wvs)
                        {
                            if(wv.trim().equals(w_))
                            {
                                Word w = new Word(id, word, mean_cn, accent, audio_file);
                                wordArray.add(w);
                                break;
                            }
                        }
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
                Record r = new Record( id,  word_id,  word,  movie_path,  movie_name,  date,  subtitle,  status) ;

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
            contentValues.put("subtitle",r.getDate().getTime());
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
}
