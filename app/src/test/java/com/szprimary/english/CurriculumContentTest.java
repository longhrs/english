package com.szprimary.english;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.szprimary.english.core.CurriculumLoader;
import com.szprimary.english.core.model.Curriculum;
import com.szprimary.english.core.model.Example;
import com.szprimary.english.core.model.Grade;
import com.szprimary.english.core.model.SentencePattern;
import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.model.Word;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 课程内容的结构与质量校验。 */
public class CurriculumContentTest {

    private static Curriculum curriculum;

    @BeforeClass
    public static void load() throws Exception {
        curriculum = CurriculumLoader.load(new FileContentSource());
    }

    @Test
    public void hasSixGrades() {
        assertEquals(6, curriculum.grades.size());
        for (int i = 0; i < curriculum.grades.size(); i++) {
            assertEquals(i + 1, curriculum.grades.get(i).grade);
        }
    }

    @Test
    public void hasEnoughUnitsAndWords() {
        assertEquals(56, curriculum.allUnits().size());
        assertTrue("词汇总量应不少于 610", curriculum.totalWords() >= 610);
        List<Grade> grades = curriculum.grades;
        for (int i = 0; i < grades.size(); i++) {
            assertTrue(grades.get(i).title + " 单元数不足", grades.get(i).units.size() >= 4);
        }
    }

    /** 三年级按 2024 新版沪教版课本编排：3A 的 8 个单元按顺序排在最前，标题与课本一致。 */
    @Test
    public void grade3FollowsNewTextbook() {
        String[] titles = {"How do we feel?", "What's interesting about families?", "What do we look like?",
                "How do we have fun?", "What do we eat?", "What do we like about small animals?",
                "What do we know about weather?", "Why do we like birthdays?"};
        List<Unit> units = curriculum.grade(3).units;
        for (int i = 0; i < titles.length; i++) {
            Unit unit = units.get(i);
            assertEquals("3A", unit.book);
            assertEquals(i + 1, unit.bookUnit);
            assertEquals(titles[i], unit.titleEn);
            assertEquals("3A Unit " + (i + 1) + " " + titles[i], unit.displayTitle());
        }
        Unit extra = curriculum.unit("g3u1");
        assertEquals("拓展", extra.book);
        assertTrue(extra.displayTitle().startsWith("拓展 · "));
        assertEquals("Unit 1 Hello!", curriculum.unit("g1u1").displayTitle());
        // 四年级上册已核实的新版单元排在四年级最前
        assertEquals("4A Unit 4 How do we use numbers?", curriculum.grade(4).units.get(0).displayTitle());
        assertEquals("4A Unit 5 How are the seasons different?", curriculum.grade(4).units.get(1).displayTitle());
    }

    @Test
    public void everyWordIsComplete() {
        List<Unit> units = curriculum.allUnits();
        Set<String> keys = new HashSet<String>();
        for (int u = 0; u < units.size(); u++) {
            Unit unit = units.get(u);
            assertTrue(unit.id + " 词汇不足 4 个，无法出选择题", unit.words.size() >= 4);
            for (int w = 0; w < unit.words.size(); w++) {
                Word word = unit.words.get(w);
                String at = unit.id + "/" + word.en;
                assertFalse(at + " 单词为空", word.en.trim().isEmpty());
                assertFalse(at + " 释义为空", word.cn.trim().isEmpty());
                assertFalse(at + " 词性为空", word.pos.trim().isEmpty());
                assertFalse(at + " 例句为空", word.exEn.trim().isEmpty());
                assertFalse(at + " 例句翻译为空", word.exCn.trim().isEmpty());
                assertTrue(at + " 音标格式应为 /.../: " + word.ipa,
                        word.ipa.startsWith("/") && word.ipa.endsWith("/") && word.ipa.length() > 2);
                assertTrue(at + " 词条键重复", keys.add(word.key()));
            }
        }
    }

    @Test
    public void everyUnitHasUsablePatterns() {
        List<Unit> units = curriculum.allUnits();
        for (int u = 0; u < units.size(); u++) {
            Unit unit = units.get(u);
            assertTrue(unit.id + " 缺少句型", unit.patterns.size() >= 2);
            for (int p = 0; p < unit.patterns.size(); p++) {
                SentencePattern pattern = unit.patterns.get(p);
                assertFalse(unit.id + " 句型缺中文", pattern.cn.trim().isEmpty());
                assertTrue(unit.id + " 句型缺例句", pattern.examples.size() >= 1);
                for (int e = 0; e < pattern.examples.size(); e++) {
                    Example example = pattern.examples.get(e);
                    assertTrue(unit.id + " 例句太短，无法连词成句: " + example.en,
                            example.en.trim().split("\\s+").length >= 2);
                    assertFalse(unit.id + " 例句缺中文", example.cn.trim().isEmpty());
                }
            }
        }
    }

    @Test
    public void unitIdsFollowConvention() {
        List<Grade> grades = curriculum.grades;
        for (int g = 0; g < grades.size(); g++) {
            Grade grade = grades.get(g);
            for (int u = 0; u < grade.units.size(); u++) {
                Unit unit = grade.units.get(u);
                assertEquals("g" + grade.grade + "u" + unit.no, unit.id);
                assertEquals(grade.grade, unit.grade);
            }
        }
    }

    @Test
    public void lookupWorks() {
        Unit unit = curriculum.unit("g1u1");
        assertTrue(unit != null);
        Word word = unit.words.get(0);
        assertEquals(word.en, curriculum.word(word.key()).en);
    }
}
