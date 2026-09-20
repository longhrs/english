package com.szprimary.english;

import com.szprimary.english.core.ContentSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/** 测试用的内容读取实现：直接读 app/src/main/assets 下的文件。 */
public final class FileContentSource implements ContentSource {

    @Override
    public String read(String name) throws IOException {
        File file = new File("src/main/assets/" + name);
        if (!file.exists()) {
            // 兼容从仓库根目录运行测试的情况
            file = new File("app/src/main/assets/" + name);
        }
        Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
        try {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[8192];
            int read = reader.read(buffer);
            while (read > 0) {
                sb.append(buffer, 0, read);
                read = reader.read(buffer);
            }
            return sb.toString();
        } finally {
            reader.close();
        }
    }
}
