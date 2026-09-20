package com.szprimary.english;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.szprimary.english.core.progress.MemoryProgressStore;
import com.szprimary.english.core.progress.ProgressManager;
import com.szprimary.english.core.progress.ReviewScheduler;
import com.szprimary.english.core.progress.WordProgress;
import com.szprimary.english.core.quiz.Answer;
import com.szprimary.english.core.quiz.Question;
import com.szprimary.english.core.quiz.QuestionRecord;
import com.szprimary.english.core.quiz.QuestionType;
import com.szprimary.english.core.quiz.QuizResult;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 学习进度、错题本与间隔复习。 */
public class ProgressManagerTest {

    private static final long DAY = 24L * 60 * 60 * 1000;
    private static final long T0 = 1_700_000_000_000L;
    private static final String KEY = "g1u1/hello";

    private Question wordQuestion(String key, boolean correctFirst) {
        List<String> options = new ArrayList<String>(Arrays.asList("hello", "hi", "bye", "name"));
        return new Question(QuestionType.CN_TO_EN, "选出「你好」对应的单词", "int.", options,
                correctFirst ? 0 : 1, correctFirst ? "hello" : "hi", "hello 你好", "hello", key, "g1u1");
    }

    @Test
    public void markLearnedCountsTowardsUnit() {
        ProgressManager progress = new ProgressManager(new MemoryProgressStore());
        progress.markLearned("g1u1", KEY, T0);
        progress.markLearned("g1u1", KEY, T0);
        assertEquals(1, progress.unit("g1u1").learnedWords);
        assertEquals(1, progress.learnedWordCount());

        progress.unmarkLearned("g1u1", KEY);
        assertEquals(0, progress.unit("g1u1").learnedWords);
        assertEquals(0, progress.learnedWordCount());
    }

    @Test
    public void wrongAnswerEntersWrongBookAndLeavesAfterTwoCorrect() {
        ProgressManager progress = new ProgressManager(new MemoryProgressStore());
        progress.recordAnswer(KEY, false, T0);
        assertTrue(progress.inWrongBook(KEY));
        assertEquals(1, progress.wrongBook().size());

        progress.recordAnswer(KEY, true, T0 + DAY);
        assertTrue("只答对一次还不能移出错题本", progress.inWrongBook(KEY));

        progress.recordAnswer(KEY, true, T0 + 2 * DAY);
        assertFalse("连续答对两次后应移出错题本", progress.inWrongBook(KEY));
    }

    @Test
    public void leitnerBoxMovesUpAndResets() {
        ProgressManager progress = new ProgressManager(new MemoryProgressStore());
        progress.recordAnswer(KEY, true, T0);
        assertEquals(2, progress.word(KEY).box);
        progress.recordAnswer(KEY, true, T0 + DAY);
        assertEquals(3, progress.word(KEY).box);
        progress.recordAnswer(KEY, false, T0 + 2 * DAY);
        assertEquals("答错回到第 1 盒", 1, progress.word(KEY).box);
        assertEquals(0, progress.word(KEY).streak);
    }

    @Test
    public void boxNeverExceedsMax() {
        ProgressManager progress = new ProgressManager(new MemoryProgressStore());
        for (int i = 0; i < 10; i++) {
            progress.recordAnswer(KEY, true, T0 + i * DAY);
        }
        assertEquals(ReviewScheduler.MAX_BOX, progress.word(KEY).box);
        assertTrue(progress.word(KEY).mastered());
        assertEquals(1, progress.masteredWordCount());
    }

    @Test
    public void reviewIsDueAfterTheBoxInterval() {
        WordProgress wp = new WordProgress(KEY);
        assertTrue("没复习过的词应立即到期", ReviewScheduler.isDue(wp, T0));

        ReviewScheduler.onAnswer(wp, true, T0);      // box 2 -> 间隔 1 天
        assertFalse(ReviewScheduler.isDue(wp, T0 + DAY / 2));
        assertTrue(ReviewScheduler.isDue(wp, T0 + DAY));

        ReviewScheduler.onAnswer(wp, true, T0 + DAY); // box 3 -> 间隔 2 天
        assertFalse(ReviewScheduler.isDue(wp, T0 + 2 * DAY));
        assertTrue(ReviewScheduler.isDue(wp, T0 + 3 * DAY));
    }

    @Test
    public void dueListOnlyContainsLearnedWords() {
        ProgressManager progress = new ProgressManager(new MemoryProgressStore());
        progress.recordAnswer(KEY, false, T0);
        assertTrue("没标记学过的词不进复习队列", progress.dueForReview(T0 + DAY, 10).isEmpty());

        progress.markLearned("g1u1", KEY, T0);
        List<String> due = progress.dueForReview(T0 + DAY, 10);
        assertEquals(1, due.size());
        assertEquals(KEY, due.get(0));
    }

    @Test
    public void quizResultUpdatesUnitBestScore() {
        ProgressManager progress = new ProgressManager(new MemoryProgressStore());
        progress.recordQuiz("g1u1", result(1, 1), T0);
        assertEquals(100, progress.unit("g1u1").bestScore);
        assertEquals(1, progress.unit("g1u1").attempts);

        progress.recordQuiz("g1u1", result(0, 1), T0 + DAY);
        assertEquals("最好成绩不应被低分覆盖", 100, progress.unit("g1u1").bestScore);
        assertEquals(2, progress.unit("g1u1").attempts);
        assertEquals(2, progress.totalAnswered());
        assertEquals(1, progress.totalCorrect());
        assertEquals(50, progress.accuracyPercent());
    }

    @Test
    public void resultScoringAndStars() {
        assertEquals(100, result(4, 4).score);
        assertEquals(3, result(4, 4).stars);
        assertEquals(75, result(3, 4).score);
        assertEquals(2, result(3, 4).stars);
        assertEquals(50, result(2, 4).score);
        assertEquals(0, result(2, 4).stars);
        assertEquals("4 题错 2 题，应记录 2 个错词", 2, result(2, 4).wrongWordKeys().size());
    }

    @Test
    public void stateSurvivesSaveAndReload() {
        MemoryProgressStore store = new MemoryProgressStore();
        ProgressManager first = new ProgressManager(store);
        first.markLearned("g1u1", KEY, T0);
        first.recordAnswer(KEY, false, T0);
        first.recordQuiz("g1u1", result(1, 2), T0);
        first.save();

        ProgressManager second = new ProgressManager(store);
        assertEquals(1, second.unit("g1u1").learnedWords);
        assertTrue(second.inWrongBook(KEY));
        assertTrue(second.word(KEY).learned);
        assertEquals(first.totalAnswered(), second.totalAnswered());
        assertEquals(first.accuracyPercent(), second.accuracyPercent());
        assertEquals(first.studyDayCount(), second.studyDayCount());
    }

    @Test
    public void corruptedStateFallsBackToEmpty() {
        ProgressManager progress = new ProgressManager(new MemoryProgressStore("{ not json at all"));
        assertEquals(0, progress.learnedWordCount());
        assertEquals(0, progress.totalAnswered());
    }

    @Test
    public void resetClearsEverything() {
        MemoryProgressStore store = new MemoryProgressStore();
        ProgressManager progress = new ProgressManager(store);
        progress.markLearned("g1u1", KEY, T0);
        progress.recordAnswer(KEY, false, T0);
        progress.reset();
        assertEquals(0, progress.learnedWordCount());
        assertTrue(progress.wrongBook().isEmpty());
        assertEquals(0, progress.studyDayCount());
        assertEquals(0, new ProgressManager(store).totalAnswered());
    }

    @Test
    public void studyStreakCountsConsecutiveDays() {
        ProgressManager progress = new ProgressManager(new MemoryProgressStore());
        progress.recordAnswer(KEY, true, T0 - DAY);
        progress.recordAnswer(KEY, true, T0);
        assertEquals(2, progress.studyDayCount());
        assertEquals(2, progress.streakDays(T0));
        assertTrue(progress.studiedToday(T0));
        assertEquals("中间断一天则连续天数归零", 0, progress.streakDays(T0 + 2 * DAY));
    }

    /** 造一个 correct 对 total 的练习结果。 */
    private QuizResult result(int correct, int total) {
        List<QuestionRecord> records = new ArrayList<QuestionRecord>();
        for (int i = 0; i < total; i++) {
            boolean ok = i < correct;
            Question question = wordQuestion("g1u1/word" + i, true);
            records.add(new QuestionRecord(question,
                    ok ? Answer.choice(0) : Answer.choice(1), ok, 1000));
        }
        return new QuizResult(records, 60_000);
    }
}
