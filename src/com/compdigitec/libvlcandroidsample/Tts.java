package com.compdigitec.libvlcandroidsample;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by junjun on 2017/6/6.
 */

public class Tts {

    private  static Context ctx = null;
    private TextToSpeech mTextToSpeech = null;
    static private Tts tts = null;

    private Tts(Context ctx)
    {

        Tts.ctx = ctx;

        //实例并初始化TTS对象
        mTextToSpeech = new TextToSpeech(this.ctx,new TextToSpeech.OnInitListener()
        {

            @Override
            public void onInit(int status)
            {
                // TODO Auto-generated method stub
                if(status == TextToSpeech.SUCCESS)
                {
                    //设置朗读语言
                    int supported = mTextToSpeech.setLanguage(Locale.US);
                    if((supported != TextToSpeech.LANG_AVAILABLE)&&(supported != TextToSpeech.LANG_COUNTRY_AVAILABLE))
                    {
                        Toast.makeText(Tts.this.ctx,"不支持当前语言！",Toast.LENGTH_LONG).show();
                    }
                }
            }

        });
    }

    public void speak(String str)
    {
        if(str == null)
            return;
        

        if(mTextToSpeech.isSpeaking())
        {
            mTextToSpeech.stop();
        }

        mTextToSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null);
    }

    static public synchronized  Tts instance(Context ctx) {

        if(tts == null || Tts.ctx != ctx)
        {
            tts = new Tts(ctx);
        }

        return tts;
    }



}
