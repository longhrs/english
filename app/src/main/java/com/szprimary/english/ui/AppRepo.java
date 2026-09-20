package com.szprimary.english.ui;

import android.content.Context;

import com.szprimary.english.core.CurriculumLoader;
import com.szprimary.english.core.model.Curriculum;
import com.szprimary.english.core.model.Grade;
import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.model.Word;
import com.szprimary.english.core.progress.MemoryProgressStore;
import com.szprimary.english.core.progress.ProgressManager;
import com.szprimary.english.core.quiz.QuizResult;

import java.util.ArrayList;
import java.util.List;

/** 全局单例：课程内容、学习进度和朗读器。 */
public final class AppRepo {

    private static AppRepo instance;

    private Curriculum curriculum;
    private ProgressManager progress;
    private Speaker speaker;
    private String loadError;

    /** 练习结束后由 QuizActivity 写入，供 ResultActivity 读取。 */
    public QuizResult lastResult;
    public String lastQuizUnitId;
    public String lastQuizTitle;

    private AppRepo() {
    }

    public static synchronized AppRepo get(Context context) {
        if (instance == null) {
            instance = new AppRepo();
            instance.init(context.getApplicationContext());
        }
        return instance;
    }

    private void init(Context context) {
        try {
            curriculum = CurriculumLoader.load(new AssetContentSource(context));
        } catch (Exception e) {
            loadError = String.valueOf(e.getMessage());
            curriculum = new Curriculum(new ArrayList<Grade>());
        }
        try {
            progress = new ProgressManager(new PrefsProgressStore(context));
        } catch (Exception e) {
            progress = new ProgressManager(new MemoryProgressStore());
        }
        speaker = new Speaker(context);
    }

    public Curriculum curriculum() {
        return curriculum;
    }

    public ProgressManager progress() {
        return progress;
    }

    public Speaker speaker() {
        return speaker;
    }

    public String loadError() {
        return loadError;
    }

    /** 出选择题用的干扰项池：优先同年级，词不够时用全部词汇。 */
    public List<Word> distractorPool(int grade) {
        List<Word> pool = new ArrayList<Word>();
        Grade g = curriculum.grade(grade);
        if (g != null) {
            for (int i = 0; i < g.units.size(); i++) {
                pool.addAll(g.units.get(i).words);
            }
        }
        if (pool.size() < 8) {
            pool = curriculum.allWords();
        }
        return pool;
    }

    public List<Word> wordsForKeys(List<String> keys) {
        List<Word> words = new ArrayList<Word>();
        for (int i = 0; i < keys.size(); i++) {
            Word word = curriculum.word(keys.get(i));
            if (word != null) {
                words.add(word);
            }
        }
        return words;
    }

    /** 单元学习进度百分比：已学词条占比。 */
    public int unitLearnedPercent(Unit unit) {
        if (unit.words.isEmpty()) {
            return 0;
        }
        int learned = 0;
        for (int i = 0; i < unit.words.size(); i++) {
            com.szprimary.english.core.progress.WordProgress wp = progress.peekWord(unit.words.get(i).key());
            if (wp != null && wp.learned) {
                learned++;
            }
        }
        return Math.round(learned * 100f / unit.words.size());
    }

    public int gradeLearnedPercent(Grade grade) {
        int total = 0;
        int learned = 0;
        for (int i = 0; i < grade.units.size(); i++) {
            Unit unit = grade.units.get(i);
            total += unit.words.size();
            for (int j = 0; j < unit.words.size(); j++) {
                com.szprimary.english.core.progress.WordProgress wp = progress.peekWord(unit.words.get(j).key());
                if (wp != null && wp.learned) {
                    learned++;
                }
            }
        }
        return total == 0 ? 0 : Math.round(learned * 100f / total);
    }
}
