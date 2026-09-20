package com.szprimary.english;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.szprimary.english.core.CurriculumLoader;
import com.szprimary.english.core.model.Curriculum;
import com.szprimary.english.core.model.Grade;
import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.model.Word;
import com.szprimary.english.core.quiz.Answer;
import com.szprimary.english.core.quiz.Grader;
import com.szprimary.english.core.quiz.Question;
import com.szprimary.english.core.quiz.QuestionType;
import com.szprimary.english.core.quiz.QuizGenerator;
import com.szprimary.english.core.quiz.QuizOptions;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 出题器：题目结构、可解性和确定性。 */
public class QuizGeneratorTest {

    private static Curriculum curriculum;

    @BeforeClass
    public static void load() throws Exception {
        curriculum = CurriculumLoader.load(new FileContentSource());
    }

    private static List<Word> pool(Grade grade) {
        List<Word> pool = new ArrayList<Word>();
        for (int i = 0; i < grade.units.size(); i++) {
            pool.addAll(grade.units.get(i).words);
        }
        return pool;
    }

    @Test
    public void everyUnitProducesValidQuestions() {
        List<Grade> grades = curriculum.grades;
        for (int g = 0; g < grades.size(); g++) {
            Grade grade = grades.get(g);
            List<Word> pool = pool(grade);
            for (int u = 0; u < grade.units.size(); u++) {
                Unit unit = grade.units.get(u);
                QuizOptions options = QuizOptions.of(unit.words.size(), 20260920L).forGrade(grade.grade);
                List<Question> questions = QuizGenerator.forUnit(unit, pool, options);
                assertEquals(unit.id + " 题量不符", unit.words.size(), questions.size());
                for (int q = 0; q < questions.size(); q++) {
                    checkQuestion(unit.id, questions.get(q));
                }
            }
        }
    }

    private void checkQuestion(String unitId, Question question) {
        String at = unitId + "/" + question.type;
        assertNotNull(at, question.prompt);
        assertFalse(at + " 题干为空", question.prompt.trim().isEmpty());
        assertFalse(at + " 缺少标准答案", question.correctText.trim().isEmpty());
        assertFalse(at + " 缺少解析", question.explanation.trim().isEmpty());

        if (question.type.isChoice()) {
            assertEquals(at + " 选项数应为 4", 4, question.options.size());
            Set<String> unique = new HashSet<String>(question.options);
            assertEquals(at + " 选项重复: " + question.options, 4, unique.size());
            assertTrue(at + " 正确项下标越界",
                    question.correctIndex >= 0 && question.correctIndex < question.options.size());
            assertEquals(at + " 正确项与答案不一致",
                    question.options.get(question.correctIndex), question.correctText);
            // 选对即判对
            assertTrue(at, Grader.isCorrect(question, Answer.choice(question.correctIndex)));
        } else if (question.type == QuestionType.SENTENCE_ORDER) {
            // 打乱的词块必须能还原出标准答案
            List<String> tokens = new ArrayList<String>(question.options);
            List<String> expected = new ArrayList<String>();
            String[] parts = question.correctText.split(" ");
            for (int i = 0; i < parts.length; i++) {
                expected.add(parts[i]);
            }
            assertEquals(at + " 词块与答案不匹配", expected.size(), tokens.size());
            List<String> copy = new ArrayList<String>(tokens);
            for (int i = 0; i < expected.size(); i++) {
                assertTrue(at + " 缺少词块 " + expected.get(i), copy.remove(expected.get(i)));
            }
            assertTrue(at, Grader.isCorrect(question, Answer.text(question.correctText)));
        } else {
            assertTrue(at, Grader.isCorrect(question, Answer.text(question.correctText)));
        }
    }

    @Test
    public void patternBlankKeepsABlank() {
        Unit unit = curriculum.unit("g3u1");
        List<Question> questions = QuizGenerator.forUnit(unit, pool(curriculum.grade(3)),
                QuizOptions.of(40, 7L));
        int blanks = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).type == QuestionType.PATTERN_BLANK) {
                assertTrue("填空题应含下划线", questions.get(i).prompt.contains("_____"));
                blanks++;
            }
        }
        assertTrue("应至少生成一道句型填空题", blanks > 0);
    }

    @Test
    public void lowerGradesSkipSpelling() {
        Grade grade = curriculum.grade(1);
        for (int u = 0; u < grade.units.size(); u++) {
            Unit unit = grade.units.get(u);
            List<Question> questions = QuizGenerator.forUnit(unit, pool(grade),
                    QuizOptions.of(10, 3L).forGrade(1));
            for (int i = 0; i < questions.size(); i++) {
                assertFalse("一年级不应出默写题",
                        questions.get(i).type == QuestionType.SPELLING);
            }
        }
    }

    @Test
    public void higherGradesIncludeSpelling() {
        Grade grade = curriculum.grade(5);
        int spelling = 0;
        for (int u = 0; u < grade.units.size(); u++) {
            Unit unit = grade.units.get(u);
            List<Question> questions = QuizGenerator.forUnit(unit, pool(grade),
                    QuizOptions.of(12, 11L).forGrade(5));
            for (int i = 0; i < questions.size(); i++) {
                if (questions.get(i).type == QuestionType.SPELLING) {
                    spelling++;
                }
            }
        }
        assertTrue("五年级应包含拼写题", spelling > 0);
    }

    @Test
    public void sameSeedGivesSameQuiz() {
        Unit unit = curriculum.unit("g4u3");
        List<Word> pool = pool(curriculum.grade(4));
        List<Question> a = QuizGenerator.forUnit(unit, pool, QuizOptions.of(12, 123L));
        List<Question> b = QuizGenerator.forUnit(unit, pool, QuizOptions.of(12, 123L));
        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i).prompt, b.get(i).prompt);
            assertEquals(a.get(i).correctText, b.get(i).correctText);
        }
    }

    @Test
    public void reviewQuizUsesGivenWords() {
        List<Word> words = new ArrayList<Word>();
        words.add(curriculum.unit("g2u2").words.get(0));
        words.add(curriculum.unit("g5u4").words.get(1));
        List<Question> questions = QuizGenerator.forWords(words, curriculum.allWords(),
                QuizOptions.of(4, 99L));
        assertEquals(4, questions.size());
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            assertTrue("复习题应关联到给定词条",
                    question.wordKey == null || question.wordKey.equals(words.get(0).key())
                            || question.wordKey.equals(words.get(1).key()));
        }
    }
}
