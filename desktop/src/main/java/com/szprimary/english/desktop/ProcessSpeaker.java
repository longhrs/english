package com.szprimary.english.desktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 每次朗读启动一个命令行进程（macOS 的 say、Linux 的 espeak 等）。参数按列表传递，不经过 shell。 */
final class ProcessSpeaker implements Speaker {

    private final List<String> prefix;
    private volatile String status;
    private Process current;

    private ProcessSpeaker(List<String> prefix, String status) {
        this.prefix = prefix;
        this.status = status;
    }

    static ProcessSpeaker espeak(File exe) {
        return new ProcessSpeaker(Arrays.asList(exe.getAbsolutePath(), "-v", "en-us", "-s", "145"),
                "使用 " + exe.getName() + " 朗读。");
    }

    static ProcessSpeaker spdSay(File exe) {
        return new ProcessSpeaker(Arrays.asList(exe.getAbsolutePath(), "-l", "en", "-r", "-15"),
                "使用 spd-say 朗读。");
    }

    /** macOS：后台查一次系统里的英语声音，避免中文系统默认用中文声音读英文。 */
    static ProcessSpeaker mac(final File say) {
        final ProcessSpeaker speaker = new ProcessSpeaker(
                new ArrayList<String>(Arrays.asList(say.getAbsolutePath(), "-r", "170")), "使用 macOS 系统语音朗读。");
        Thread probe = new Thread(new Runnable() {
            @Override
            public void run() {
                String voice = pickMacVoice(say);
                if (voice != null) {
                    synchronized (speaker) {
                        speaker.prefix.add("-v");
                        speaker.prefix.add(voice);
                    }
                    speaker.status = "使用 macOS 英语语音 " + voice + " 朗读。";
                } else {
                    speaker.status = "未找到 macOS 英语语音，正在用系统默认语音朗读；可在「系统设置 → 辅助功能 → 朗读内容」里添加英语声音。";
                }
            }
        }, "say-voice-probe");
        probe.setDaemon(true);
        probe.start();
        return speaker;
    }

    static String pickMacVoice(File say) {
        String[] preferred = {"Samantha", "Alex", "Daniel", "Karen", "Moira", "Serena"};
        List<String> english = new ArrayList<String>();
        try {
            Process p = new ProcessBuilder(say.getAbsolutePath(), "-v", "?").redirectErrorStream(true).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                String name = parseMacVoiceLine(line);
                if (name != null) {
                    english.add(name);
                }
            }
            p.waitFor();
        } catch (Exception e) {
            return null;
        }
        for (int i = 0; i < preferred.length; i++) {
            if (english.contains(preferred[i])) {
                return preferred[i];
            }
        }
        return english.isEmpty() ? null : english.get(0);
    }

    /** 解析 `say -v ?` 的一行，如 "Samantha            en_US    # Hello..."，是英语声音时返回名字。 */
    static String parseMacVoiceLine(String line) {
        int hash = line.indexOf('#');
        String head = (hash >= 0 ? line.substring(0, hash) : line).trim();
        String[] parts = head.split("\\s+");
        if (parts.length < 2) {
            return null;
        }
        String locale = parts[parts.length - 1];
        if (!locale.startsWith("en_") && !locale.startsWith("en-")) {
            return null;
        }
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (i > 0) {
                name.append(' ');
            }
            name.append(parts[i]);
        }
        return name.toString();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String status() {
        return status;
    }

    @Override
    public synchronized void speak(String text) {
        String clean = Speaker.clean(text);
        if (clean.isEmpty()) {
            return;
        }
        if (current != null && current.isAlive()) {
            current.destroy();
        }
        List<String> cmd = new ArrayList<String>(prefix);
        cmd.add(clean);
        try {
            current = new ProcessBuilder(cmd).redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        } catch (Exception e) {
            status = "朗读失败：" + e.getMessage();
        }
    }

    @Override
    public synchronized void shutdown() {
        if (current != null) {
            current.destroy();
        }
    }
}
