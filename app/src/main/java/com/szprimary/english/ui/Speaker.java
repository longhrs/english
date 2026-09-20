package com.szprimary.english.ui;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/** 单词和句子朗读。设备没有英语语音引擎时自动降级，不影响其它功能。 */
public final class Speaker implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private boolean ready;

    public Speaker(Context context) {
        try {
            tts = new TextToSpeech(context.getApplicationContext(), this);
        } catch (Exception e) {
            tts = null;
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS && tts != null) {
            int result = tts.setLanguage(Locale.UK);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                result = tts.setLanguage(Locale.US);
            }
            ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
            if (ready) {
                tts.setSpeechRate(0.85f);
            }
        }
    }

    public boolean isReady() {
        return ready;
    }

    public void speak(String text) {
        if (ready && tts != null && text != null && text.length() > 0) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sz-english");
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            ready = false;
        }
    }
}
