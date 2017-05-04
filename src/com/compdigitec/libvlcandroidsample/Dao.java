package com.compdigitec.libvlcandroidsample;

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
    public static final String ORDER_DESC = "desc";
    public static final String ORDER_ASC = "asc";
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
            StringBuffer sqlb = new StringBuffer(" word in (");
            String seperator = "";
            for(String word : words)
            {
                sqlb.append(seperator+"?");
                seperator = ",";
            }

            sqlb.append(")");
            String  sql = sqlb.toString();

            Cursor cursor = db.query(false,"words",null,sql,words,null,null,null,null);
            //使用cursor.moveToNext()把游标下移一行。游标默认在第一行的上一行。
            while (cursor.moveToNext()) {
                //使用GetString获取列中的值。参数为使用cursor.getColumnIndex("name")获取的序号。
                Integer  id = cursor.getInt(cursor.getColumnIndex("_id"));
                String word = cursor.getString(cursor.getColumnIndex("word"));
                String mean_cn = cursor.getString(cursor.getColumnIndex("mean_cn"));
                String accent = cursor.getString(cursor.getColumnIndex("accent"));
                String audio_file = cursor.getString(cursor.getColumnIndex("audio_file"));
                Word w = new Word(id, word, mean_cn, accent, audio_file);
                wordArray.add(w);
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
     * @param from
     * @param to
     * @param order ORDER_DESC or ORDER_ASC
     * @return
     */
    public List<Record> findRecordsByDate(Date from, Date to, String order)
    {
        return null;
    }

    public Record saveRecord(Record r)
    {
        return null;
    }
}
