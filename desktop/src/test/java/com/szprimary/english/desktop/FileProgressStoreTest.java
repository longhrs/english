package com.szprimary.english.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.szprimary.english.core.progress.ProgressManager;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;

public class FileProgressStoreTest {

    @Test
    public void missingFileReadsEmpty() throws Exception {
        File dir = Files.createTempDirectory("szprogress").toFile();
        assertEquals("", new FileProgressStore(new File(dir, "nope.json")).read());
    }

    @Test
    public void writeCreatesDirectoriesAndRoundTrips() throws Exception {
        File dir = Files.createTempDirectory("szprogress").toFile();
        File file = new File(dir, "a/b/progress.json");
        FileProgressStore store = new FileProgressStore(file);
        store.write("{\"中文\":1}");
        assertTrue(file.isFile());
        assertEquals("{\"中文\":1}", store.read());
        assertFalse("临时文件应已改名", new File(file.getParentFile(), "progress.json.tmp").exists());
        store.write("{}");
        assertEquals("{}", store.read());
    }

    @Test
    public void progressSurvivesRestart() throws Exception {
        File file = new File(Files.createTempDirectory("szprogress").toFile(), "progress.json");
        ProgressManager first = new ProgressManager(new FileProgressStore(file));
        first.markLearned("g1u1", "g1u1/hello", System.currentTimeMillis());
        first.save();
        ProgressManager second = new ProgressManager(new FileProgressStore(file));
        assertEquals(1, second.learnedWordCount());
    }

    @Test
    public void corruptFileFallsBackToEmptyProgress() throws Exception {
        File file = new File(Files.createTempDirectory("szprogress").toFile(), "progress.json");
        Files.write(file.toPath(), "{这不是 JSON".getBytes("UTF-8"));
        ProgressManager pm = new ProgressManager(new FileProgressStore(file));
        assertEquals(0, pm.learnedWordCount());
    }
}
