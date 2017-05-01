package com.compdigitec.libvlcandroidsample;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.compdigitec.subtitle.subtitleFile.Caption;
import com.compdigitec.subtitle.subtitleFile.FormatSRT;
import com.compdigitec.subtitle.subtitleFile.TimedTextFileFormat;
import com.compdigitec.subtitle.subtitleFile.TimedTextObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.io.File;
import org.videolan.libvlc.MediaPlayer;

/**
 * Created by MHDante on 2015-07-26.
 */
public class SubtitleView extends TextView implements Runnable{
    private static final String TAG = "SubtitleView";
    private static final boolean DEBUG = false;
    private static final int UPDATE_INTERVAL = 300;
    private MediaPlayer player;
    private TreeMap<Long, Line> track;

    public SubtitleView(Context context) {
        super(context);
    }


    public SubtitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public SubtitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void run() {
        if (player !=null && track!= null){
            int seconds = (int)(player.getTime() / 1000);
            String text = getTimedText(player.getTime());
            setText(text);
        }
        postDelayed(this, UPDATE_INTERVAL);
    }

    private String getTimedText(long currentPosition) {
        String result = "";
        for(Map.Entry<Long, Line> entry: track.entrySet()){
            if (currentPosition < entry.getKey()) break;
            if (currentPosition < entry.getValue().to) result = entry.getValue().text;
        }
        return result;
    }

    // To display the seconds in the duration format 00:00:00
    public String secondsToDuration(int seconds) {
        return String.format("%02d:%02d:%02d", seconds / 3600,
                (seconds % 3600) / 60, (seconds % 60), Locale.CHINESE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        postDelayed(this, 300);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this);
    }
    public void setPlayer(MediaPlayer player) {
        this.player = player;
    }

    public void setSubSource(String path, String mime){
            track = getSubtitleFile(path);
    }

    /////////////Utility Methods:
    //Based on https://github.com/sannies/mp4parser/
    //Apache 2.0 Licence at: https://github.com/sannies/mp4parser/blob/master/LICENSE

    public static TreeMap<Long, Line> parse(InputStream is) throws Exception {
//        LineNumberReader r = new LineNumberReader(new InputStreamReader(is,"utf-8"));
        TreeMap<Long, Line> track = new TreeMap<>();
//        while ((r.readLine()) != null) /*Read cue number*/{
//            String timeString = r.readLine();
//            String lineString = "";
//            String s;
//            while (!((s = r.readLine()) == null || s.trim().equals(""))) {
//                lineString += s + "\n";
//            }
//            long startTime = parse(timeString.split("-->")[0]);
//            long endTime = parse(timeString.split("-->")[1]);
//            track.put(startTime, new Line(startTime, endTime, lineString));
//        }

        TimedTextObject tto;
        TimedTextFileFormat ttff;

        ttff = new FormatSRT();
        tto = ttff.parseFile(null, is);
        //first we check if the TimedTextObject had been built, otherwise...
        if(!tto.built)
            return track;

        //we will write the lines in an ArrayList,
        int index = 0;
        //the minimum size of the file is 4*number of captions, so we'll take some extra space.
        ArrayList<String> file = new ArrayList<>(5 * tto.captions.size());
        //we iterate over our captions collection, they are ordered since they come from a TreeMap
        Collection<Caption> c = tto.captions.values();
        Iterator<Caption> itr = c.iterator();
        int captionNumber = 1;

        while(itr.hasNext()){
            //new caption
            Caption current = itr.next();
            Log.d(TAG,current.toString());
            long startTime = current.start.getMseconds();
            long endTime = current.end.getMseconds();
            String lineString = current.content;
            track.put(startTime, new Line(startTime, endTime, lineString));
        }

        Log.d(TAG,"");
        return track;
    }

    private static long parse(String in) {
        long hours = Long.parseLong(in.split(":")[0].trim());
        long minutes = Long.parseLong(in.split(":")[1].trim());
        long seconds = Long.parseLong(in.split(":")[2].split(",")[0].trim());
        long millies = Long.parseLong(in.split(":")[2].split(",")[1].trim());

        return hours * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000 + millies;

    }

    private TreeMap<Long, Line> getSubtitleFile(String path) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(path));
            return parse(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static class Line {
        long from;
        long to;
        String text;


        public Line(long from, long to, String text) {
            this.from = from;
            this.to = to;
            this.text = text;
        }
    }
}
