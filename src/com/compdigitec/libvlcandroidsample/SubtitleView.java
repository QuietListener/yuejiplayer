package com.compdigitec.libvlcandroidsample;

import android.content.Context;
import android.graphics.Color;
import android.text.Spanned;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.io.File;
import java.util.regex.Pattern;

import org.videolan.libvlc.MediaPlayer;
import android.text.Html;

/**
 * Created by MHDante on 2015-07-26.
 */
public class SubtitleView extends TextView implements Runnable{
    private static final String TAG = "SubtitleView";
    private static final boolean DEBUG = false;
    private static final int UPDATE_INTERVAL = 100;
    private MediaPlayer player;
    private TreeMap<Long, Line> track;
    private long pre_time = -1l;
    private long cur_time = -1l;
    private long next_time = 0l;
    private boolean onlyShowEn = true;
    private int subIndex = 0;

    private static final String seperator = "<br\\s*>|<br\\s*/>|\r\n|\r|\n";

    public SubtitleView(Context context) {
        super(context);
    }


    public SubtitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public SubtitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private void updateText()
    {
        if (player !=null && track!= null){
            int seconds = (int)(player.getTime() / 1000);
            Line line = getTimedText(player.getTime());
            if( line != null  && cur_time != line.from )
            {
                pre_time = cur_time;
                cur_time = line.from;
            }

            String text = line ==null || line.text == null ? "" :line.text;

            if(onlyShowEn == true)
            {
                String[] texts = text.split(seperator);

                for (String s : texts) {
                    Spanned sp = Html.fromHtml(s);
                    String content = sp.toString().trim();
                    if (isEn(content)) {
                        setText(sp);
                    }
                }
            }
            else {
                Spanned sp = Html.fromHtml(text);
                setText(sp);
            }
        }
    }


    @Override
    public void run() {

        updateText();
        postDelayed(this, UPDATE_INTERVAL);
    }

    private boolean isEn(String text)
    {
        if(text == null)
            return true;

        String [] s1 = text.replaceAll("\\s+","").split("[0-9]|[a-zA-Z]|'|,|;|\\.|!|，|。|-");
        Set ss = new HashSet<String>(Arrays.asList(s1));

        int length = text.length();
        int size = ss.size();

        if( (float)(length-size)/length > 0.9)
        {
            return true;
        }
        return false;
    }

    private Line getTimedText(long currentPosition) {
        Line result = null;

        subIndex = 0;
        for(Map.Entry<Long, Line> entry: track.entrySet())
        {

            if (currentPosition < entry.getKey())
                break;
            if (currentPosition < entry.getValue().to)
                result = entry.getValue();

            subIndex+=1;
        }
        return result;
    }


    private Line getPreTimedText(long currentPosition) {

        List<Line> ls = new ArrayList<>(this.track.values());
        if(this.subIndex>1 && this.subIndex <= ls.size())
        {
            this.subIndex-=1;
            return ls.get(this.subIndex);
        }

        return null;
    }


    private Line getNextTimedText(long currentPosition) {

        List<Line> ls = new ArrayList<>(this.track.values());
        if(this.subIndex>=0 && this.subIndex < ls.size()-1)
        {
            //this.subIndex+=1;
            return ls.get(this.subIndex);
        }

        return null;
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



    private void init()
    {
        this.setBackgroundColor(Color.GRAY);
        this.getBackground().setAlpha(50);
    }
    public void setSubSource(String path, String mime){
        init();
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

    public void setOnlyShowEn(boolean onlyShowEn) {
        this.onlyShowEn = onlyShowEn;
        updateText();
    }

    private static long parse(String in) {
        long hours = Long.parseLong(in.split(":")[0].trim());
        long minutes = Long.parseLong(in.split(":")[1].trim());
        long seconds = Long.parseLong(in.split(":")[2].split(",")[0].trim());
        long millies = Long.parseLong(in.split(":")[2].split(",")[1].trim());

        return hours * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000 + millies;

    }

    public long getPre_time() {
        Line line = this.getPreTimedText(this.player.getTime());
        pre_time = line == null ? 0l : line.from;
        return pre_time;
    }

    public long getNext_time() {
        Line line = this.getNextTimedText(this.player.getTime());
        next_time = line == null ? player.getMedia().getDuration() : line.from;
        return next_time;
    }

    public long getCur_time() {
        return cur_time;
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
