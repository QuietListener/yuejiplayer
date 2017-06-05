package com.compdigitec.libvlcandroidsample;

import android.database.DataSetObserver;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.videolan.libvlc.util.Extensions;

import java.io.File;
import java.util.ArrayList;

/**
 * List adapter used to drive the ListView in the activity.
 */
public class DirectoryAdapter extends BaseAdapter {
    public final static String TAG = "/DirectoryAdapter";
    private File curDir = null;
    private File preDir = null;
    private String baseDir = "juduplayer";

    private ArrayList<File> mFiles = new ArrayList<>();
    private boolean mAudio;

    public DirectoryAdapter() {
        mAudio = true;
        curDir = Environment.getExternalStorageDirectory();
        if(curDir.exists() && curDir.canWrite())
        {
            String path = curDir.getAbsolutePath()+"/"+ baseDir;
            File basePath = new File(path);
            if(!basePath.exists())
            {
                basePath.mkdirs();
                curDir = basePath.getAbsoluteFile();
            }
        }

        preDir = curDir;
        refresh();
    }


    public boolean isAudioMode() {
        return mAudio;
    }

    public void setAudioMode(boolean b) {
        mAudio = b;
    }

    public void refresh()
    {
        if(this.curDir != null)
            refresh(this.curDir);
    }


    public void goUp()
    {
        File pre = this.preDir.getParentFile();
        File cur = this.curDir.getParentFile();

        if(pre == null )
            pre = cur;
        if(cur == null)
            return;

        this.preDir = pre;
        this.curDir = cur;
        refresh();
    }

    public void goTo(File f)
    {
        if(f!= null && f.isDirectory())
        {
            this.preDir = f.getParentFile();
            this.curDir = f;
            refresh();
        }
    }

    public void refresh(File dir) {
        Log.d(TAG, "Refreshing adapter in " + (mAudio ? "audio mode" : "video mode"));
        File[] files = dir.listFiles();

        mFiles.clear();
        mFiles.add(preDir);

        if(files != null)
        {
            for (File f : files) {
                // Filter using libVLC's 'supported audio formats' filter.
                if (f.isDirectory()) {
                    mFiles.add(f);
                    continue;
                }


                String ext = Utils.fileExt(f.getAbsolutePath());
                if (ext == null)
                    continue;

                if (Extensions.VIDEO.contains(ext) || Extensions.SUBTITLES.contains(ext))
                    mFiles.add(f);
            }
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return mFiles.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getItemViewType(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if(v == null)
        {
            v = new TextView(parent.getContext());
        }

        File f = mFiles.get(position);
        if(f == null)
            return null;

        String text = f.getName();
        if(f.getAbsolutePath().equals(this.preDir.getAbsolutePath()))
        {
            text = "返回上一级";
        }

        if(f.isDirectory())
        {
            text = "/"+text;
        }

        ((TextView)v).setText(text);
        ((TextView)v).setPadding(0,6,0,0);

        return v;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEmpty() {
        return mFiles.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver arg0) {
        super.registerDataSetObserver(arg0);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver arg0) {
        super.unregisterDataSetObserver(arg0);
    }

    @Override
    public boolean areAllItemsEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEnabled(int arg0) {
        return true;
    }

}
