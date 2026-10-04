package com.szprimary.english.desktop;

import com.szprimary.english.core.ContentSource;
import com.szprimary.english.core.CurriculumLoader;
import com.szprimary.english.core.model.Curriculum;
import com.szprimary.english.core.model.Grade;
import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.model.Word;
import com.szprimary.english.core.progress.MemoryProgressStore;
import com.szprimary.english.core.progress.ProgressManager;
import com.szprimary.english.core.progress.ProgressStore;
import com.szprimary.english.core.progress.WordProgress;
import com.szprimary.english.core.quiz.QuizResult;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** PC 版的全局数据：课程内容、学习进度和朗读器。对应 Android 版的 AppRepo，逻辑保持一致。 */
public final class DesktopRepo {

    private final Curriculum curriculum;
    private final ProgressManager progress;
    private final Speaker speaker;
    private final String loadError;
    private final String progressLocation;
    private final TextbookImages images;

    /** 练习结束后由练习页写入，供成绩页读取。 */
    public QuizResult lastResult;
    public String lastQuizUnitId;
    public String lastQuizTitle;

    public DesktopRepo(ContentSource content, ProgressStore store, String progressLocation, Speaker speaker) {
        this(content, store, progressLocation, speaker, new TextbookImages(null));
    }

    public DesktopRepo(ContentSource content, ProgressStore store, String progressLocation, Speaker speaker,
                       TextbookImages images) {
        Curriculum loaded;
        String error = null;
        try {
            loaded = CurriculumLoader.load(content);
        } catch (Exception e) {
            error = String.valueOf(e.getMessage());
            loaded = new Curriculum(new ArrayList<Grade>());
        }
        ProgressManager pm;
        try {
            pm = new ProgressManager(store);
        } catch (Exception e) {
            pm = new ProgressManager(new MemoryProgressStore());
        }
        this.curriculum = loaded;
        this.loadError = error;
        this.progress = pm;
        this.speaker = speaker;
        this.progressLocation = progressLocation;
        this.images = images;
    }

    /** 正式运行时的组装：classpath 课程数据 + 本机进度文件 + 系统朗读。 */
    public static DesktopRepo createDefault() {
        FileProgressStore store = new FileProgressStore(AppPaths.progressFile());
        return new DesktopRepo(new ResourceContentSource(), store,
                store.file().getAbsolutePath(), Speaker.create(),
                new TextbookImages(new java.io.File(AppPaths.dataDir(), "images")));
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

    public TextbookImages images() {
        return images;
    }

    public String progressLocation() {
        return progressLocation;
    }

    public static String version() {
        try {
            InputStream in = DesktopRepo.class.getResourceAsStream("/desktop-version.properties");
            if (in != null) {
                try {
                    Properties p = new Properties();
                    p.load(in);
                    String v = p.getProperty("version", "").trim();
                    if (v.length() > 0 && !v.startsWith("$")) {
                        return v;
                    }
                } finally {
                    in.close();
                }
            }
        } catch (Exception ignored) {
            // 用默认值
        }
        return "开发版";
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

    public boolean isLearned(Word word) {
        WordProgress wp = progress.peekWord(word.key());
        return wp != null && wp.learned;
    }

    /** 单元学习进度百分比：已学词条占比。 */
    public int unitLearnedPercent(Unit unit) {
        if (unit.words.isEmpty()) {
            return 0;
        }
        int learned = 0;
        for (int i = 0; i < unit.words.size(); i++) {
            if (isLearned(unit.words.get(i))) {
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
                if (isLearned(unit.words.get(j))) {
                    learned++;
                }
            }
        }
        return total == 0 ? 0 : Math.round(learned * 100f / total);
    }
}
