package com.szprimary.english.desktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Windows 朗读：调用系统自带的 System.Speech（SAPI 语音）。
 *
 * 启动时开一个常驻的 PowerShell 进程，每行标准输入读一句，点「朗读」几乎没有延迟；
 * 常驻进程起不来或中途退出时，退回到每次朗读单独启动一个 PowerShell（慢一点但同样能读）。
 * 要读的文字只走标准输入或 Base64 编码后的参数，不会被当成命令执行。
 */
final class WindowsSpeaker implements Speaker {

    /** 优先英式英语（课本是牛津版），其次任意英语声音。 */
    private static final String SELECT_VOICE =
            "$voices = $s.GetInstalledVoices() | Where-Object { $_.Enabled }\n"
                    + "$v = $voices | Where-Object { $_.VoiceInfo.Culture.Name -eq 'en-GB' } | Select-Object -First 1\n"
                    + "if (-not $v) { $v = $voices | Where-Object { $_.VoiceInfo.Culture.Name -like 'en*' } | Select-Object -First 1 }\n"
                    + "if ($v) { $s.SelectVoice($v.VoiceInfo.Name) }\n";

    private static final String SETUP =
            "$ErrorActionPreference = 'Stop'\n"
                    + "Add-Type -AssemblyName System.Speech\n"
                    + "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer\n"
                    + "$s.SetOutputToDefaultAudioDevice()\n"
                    + "$s.Rate = -1\n"
                    + SELECT_VOICE;

    static final String PERSISTENT_SCRIPT = SETUP
            + "if ($v) { [Console]::Out.WriteLine('READY en ' + $v.VoiceInfo.Name) } "
            + "else { [Console]::Out.WriteLine('READY other ' + $s.Voice.Name) }\n"
            + "[Console]::Out.Flush()\n"
            + "while ($true) {\n"
            + "  $line = [Console]::In.ReadLine()\n"
            + "  if ($null -eq $line) { break }\n"
            + "  $s.SpeakAsyncCancelAll()\n"
            + "  if ($line.Length -gt 0) { [void]$s.SpeakAsync($line) }\n"
            + "}\n";

    private final String powershell;
    private Process persistent;
    private Writer input;
    private Process oneShot;
    private volatile boolean persistentDead;
    private volatile boolean shuttingDown;
    private volatile String status = "正在启动 Windows 语音…";
    private volatile boolean available = true;

    WindowsSpeaker() {
        String root = System.getenv("SystemRoot");
        File builtIn = root == null ? null : new File(root, "System32\\WindowsPowerShell\\v1.0\\powershell.exe");
        powershell = builtIn != null && builtIn.isFile() ? builtIn.getAbsolutePath() : "powershell.exe";
        startPersistent();
    }

    static String encode(String script) {
        return Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
    }

    /** 单次朗读的脚本：文字以 UTF-8 + Base64 嵌入，脚本里只有字母数字和 +/=。 */
    static String oneShotScript(String text) {
        String b64 = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        return SETUP
                + "$t = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('" + b64 + "'))\n"
                + "$s.Speak($t)\n";
    }

    private ProcessBuilder builder(String script) {
        return new ProcessBuilder(powershell, "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass",
                "-EncodedCommand", encode(script));
    }

    private void startPersistent() {
        try {
            persistent = builder(PERSISTENT_SCRIPT).redirectErrorStream(true).start();
            input = new OutputStreamWriter(persistent.getOutputStream(), StandardCharsets.US_ASCII);
        } catch (IOException e) {
            persistentDead = true;
            available = false;
            status = "未能启动 Windows 语音服务（PowerShell），朗读不可用。";
            return;
        }
        final Process process = persistent;
        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader r = new BufferedReader(
                            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    while ((line = r.readLine()) != null) {
                        if (line.startsWith("READY en ")) {
                            status = "使用英语语音「" + line.substring(9).trim() + "」朗读。";
                        } else if (line.startsWith("READY other")) {
                            status = "本机没有英语语音，正在用系统默认语音朗读，发音可能不准。"
                                    + "可在 Windows「设置 → 时间和语言 → 语音 → 添加语音」里添加 English 语音。";
                        }
                    }
                } catch (IOException ignored) {
                    // 进程结束
                }
                if (!shuttingDown) {
                    persistentDead = true;
                    if (status.startsWith("正在启动")) {
                        status = "使用 Windows 系统语音朗读（兼容模式）。";
                    }
                }
            }
        }, "windows-tts");
        reader.setDaemon(true);
        reader.start();
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String status() {
        return status;
    }

    @Override
    public synchronized void speak(String text) {
        String clean = Speaker.clean(text);
        if (clean.isEmpty() || !available) {
            return;
        }
        if (!persistentDead && persistent != null && persistent.isAlive()) {
            try {
                input.write(clean);
                input.write("\r\n");
                input.flush();
                return;
            } catch (IOException e) {
                persistentDead = true;
            }
        }
        if (oneShot != null && oneShot.isAlive()) {
            oneShot.destroy();
        }
        try {
            oneShot = builder(oneShotScript(clean)).redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        } catch (IOException e) {
            available = false;
            status = "朗读失败：" + e.getMessage();
        }
    }

    @Override
    public synchronized void shutdown() {
        shuttingDown = true;
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ignored) {
            // 已关闭
        }
        if (persistent != null) {
            persistent.destroy();
        }
        if (oneShot != null) {
            oneShot.destroy();
        }
    }
}
