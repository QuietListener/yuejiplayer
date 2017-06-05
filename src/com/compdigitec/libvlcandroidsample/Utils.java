package com.compdigitec.libvlcandroidsample;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static  String formatDate(Date date)throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static Date parse(String strDate) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.parse(strDate);
    }
}
