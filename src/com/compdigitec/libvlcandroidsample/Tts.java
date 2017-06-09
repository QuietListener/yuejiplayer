package com.compdigitec.libvlcandroidsample;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by junjun on 2017/6/6.
 */

public class Tts {

    private TextToSpeech mTextToSpeech = null;
    private Context ctx;
    public Tts(Context ctx)
    {
        this.ctx = ctx;

        if (true)
        {
            try {
            //实例并初始化TTS对象
                mTextToSpeech = new TextToSpeech(ctx, new TextToSpeech.OnInitListener() {

                    @Override
                    public void onInit(int status) {
                        // TODO Auto-generated method stub
                        if (status == TextToSpeech.SUCCESS) {
                            //设置朗读语言
                            int supported = mTextToSpeech.setLanguage(Locale.US);
                            if ((supported != TextToSpeech.LANG_AVAILABLE) && (supported != TextToSpeech.LANG_COUNTRY_AVAILABLE)) {
                                Toast.makeText(Tts.this.ctx, "不支持当前语言！", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                });
            }
        catch(Exception e)
            {
                Log.e("Tts", e.toString());
            }
        }
    }

    public void speak(String str)
    {

        if(str == null)
            return;


        if(true) {
            if (mTextToSpeech.isSpeaking()) {
                mTextToSpeech.stop();
            }

            mTextToSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null);
        }

    }


    public void destroy()
    {
        if (mTextToSpeech!=null)
        {
            mTextToSpeech.stop();
        }
    }
}
