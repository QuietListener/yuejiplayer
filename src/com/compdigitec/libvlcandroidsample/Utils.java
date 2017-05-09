package com.compdigitec.libvlcandroidsample;

/**
 * Created by junjun on 2017/5/9.
 */

public class Utils {

    static public String fileExt(String file_path)
    {
        if(file_path == null)
            return null;

        int index =  file_path.lastIndexOf(".");
        if(index < 0 )
            return null;

        String ext = file_path.substring(index).toLowerCase().trim();
        return ext;
    }
}
