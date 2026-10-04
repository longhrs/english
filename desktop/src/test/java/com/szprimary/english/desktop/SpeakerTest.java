package com.szprimary.english.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.Test;

public class SpeakerTest {

    @Test
    public void cleanKeepsPlainEnglish() {
        assertEquals("I go to school by bus.", Speaker.clean("I go to school by bus."));
        assertEquals("Don't touch it.", Speaker.clean("Don\u2019t touch it."));
    }

    @Test
    public void cleanRemovesNewlinesControlCharsAndNonAscii() {
        assertEquals("Hello world", Speaker.clean("Hello\r\nworld"));
        assertEquals("apple / pl/", Speaker.clean("apple /ˈæpl/"));
        assertFalse(Speaker.clean("a\u0000b\tc").contains("\u0000"));
        assertEquals("", Speaker.clean(null));
        assertEquals("", Speaker.clean("中文"));
    }

    @Test
    public void cleanNeverStartsWithDash() {
        assertEquals("v en", Speaker.clean("--v en"));
        assertEquals("rf", Speaker.clean("- -rf"));
    }

    @Test
    public void cleanCapsLength() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append('a');
        }
        assertTrue(Speaker.clean(sb.toString()).length() <= 300);
    }

    @Test
    public void macVoiceLineParsing() {
        assertEquals("Samantha", ProcessSpeaker.parseMacVoiceLine("Samantha            en_US    # Hello, my name is Samantha."));
        assertEquals("Bad News", ProcessSpeaker.parseMacVoiceLine("Bad News            en_US    # The light you see"));
        assertNull(ProcessSpeaker.parseMacVoiceLine("Ting-Ting           zh_CN    # 你好"));
        assertNull(ProcessSpeaker.parseMacVoiceLine(""));
    }

    /** 单次朗读脚本里，要读的文字只以 Base64 出现，引号等字符不会进入 PowerShell 代码。 */
    @Test
    public void windowsOneShotScriptEmbedsTextAsBase64() {
        String text = "It's '; Remove-Item C:\\ -Recurse; '";
        String script = WindowsSpeaker.oneShotScript(text);
        assertFalse(script.contains("Remove-Item"));
        String b64 = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        assertTrue(script.contains("FromBase64String('" + b64 + "')"));
        String decoded = new String(Base64.getDecoder().decode(WindowsSpeaker.encode(script)), StandardCharsets.UTF_16LE);
        assertEquals(script, decoded);
    }

    @Test
    public void silentSpeakerIsNotAvailable() {
        Speaker s = new Speaker.Silent("x");
        assertFalse(s.isAvailable());
        s.speak("hello");
        assertEquals("x", s.status());
    }
}
