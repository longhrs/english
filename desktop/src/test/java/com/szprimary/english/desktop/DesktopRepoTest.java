package com.szprimary.english.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.szprimary.english.core.model.Grade;
import com.szprimary.english.core.progress.MemoryProgressStore;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class DesktopRepoTest {

    static DesktopRepo memoryRepo() {
        return new DesktopRepo(new ResourceContentSource(), new MemoryProgressStore(), null, new Speaker.Silent("测试"));
    }

    @Test
    public void loadsSameCurriculumAsAndroid() {
        DesktopRepo repo = memoryRepo();
        assertNull(repo.loadError());
        assertEquals(6, repo.curriculum().grades.size());
        int units = 0;
        for (Grade g : repo.curriculum().grades) {
            units += g.units.size();
        }
        // 与 app/src/main/assets/curriculum 是同一份数据
        assertEquals(repo.curriculum().allUnits().size(), units);
        assertTrue(units >= 42);
        assertTrue(repo.curriculum().totalWords() >= 470);
    }

    @Test
    public void distractorPoolPrefersSameGrade() {
        DesktopRepo repo = memoryRepo();
        assertEquals(repo.curriculum().grade(3).wordCount(), repo.distractorPool(3).size());
        assertEquals(repo.curriculum().totalWords(), repo.distractorPool(99).size());
    }

    @Test
    public void versionMatchesAndroidVersionName() throws Exception {
        File appGradle = new File("../app/build.gradle");
        String text = new String(Files.readAllBytes(appGradle.toPath()), StandardCharsets.UTF_8);
        Matcher m = Pattern.compile("versionName\\s+\"([^\"]+)\"").matcher(text);
        assertTrue(m.find());
        assertEquals(m.group(1), DesktopRepo.version());
    }

    @Test
    public void learnedPercent() {
        DesktopRepo repo = memoryRepo();
        com.szprimary.english.core.model.Unit unit = repo.curriculum().unit("g1u1");
        assertEquals(0, repo.unitLearnedPercent(unit));
        repo.progress().markLearned(unit.id, unit.words.get(0).key(), System.currentTimeMillis());
        assertTrue(repo.unitLearnedPercent(unit) > 0);
        assertFalse(repo.gradeLearnedPercent(repo.curriculum().grade(2)) > 0);
    }
}
