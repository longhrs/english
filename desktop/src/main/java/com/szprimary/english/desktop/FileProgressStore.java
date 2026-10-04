package com.szprimary.english.desktop;

import com.szprimary.english.core.progress.ProgressStore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** 把学习进度写到本机文件。先写临时文件再改名，写到一半断电也不会损坏原有进度。 */
public final class FileProgressStore implements ProgressStore {

    private final File file;

    public FileProgressStore(File file) {
        this.file = file;
    }

    public File file() {
        return file;
    }

    @Override
    public String read() {
        if (!file.isFile()) {
            return "";
        }
        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public void write(String json) {
        File dir = file.getAbsoluteFile().getParentFile();
        try {
            if (dir != null) {
                Files.createDirectories(dir.toPath());
            }
            File tmp = new File(dir, file.getName() + ".tmp");
            Files.write(tmp.toPath(), json.getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(tmp.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("保存学习进度失败：" + e.getMessage());
        }
    }
}
