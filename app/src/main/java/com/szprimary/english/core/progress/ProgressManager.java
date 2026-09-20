package com.szprimary.english.core.progress;

import com.szprimary.english.core.quiz.QuestionRecord;
import com.szprimary.english.core.quiz.QuizResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/** 学习进度、错题本和统计数据的读写与更新。 */
public final class ProgressManager {

    private static final int VERSION = 1;

    private final ProgressStore store;
    private final Map<String, WordProgress> words = new LinkedHashMap<String, WordProgress>();
    private final Map<String, UnitProgress> units = new LinkedHashMap<String, UnitProgress>();
    private final Set<String> wrongBook = new LinkedHashSet<String>();
    private final Set<String> studyDays = new LinkedHashSet<String>();
    private int totalAnswered;
    private int totalCorrect;

    public ProgressManager(ProgressStore store) {
        this.store = store;
        load();
    }

    // ---------------------------------------------------------------- 更新

    /** 在「知识引导」里把一个词标记为已学。 */
    public void markLearned(String unitId, String wordKey, long now) {
        WordProgress wp = word(wordKey);
        if (!wp.learned) {
            wp.learned = true;
            unit(unitId).learnedWords++;
        }
        unit(unitId).lastStudy = now;
        touchDay(now);
    }

    public void unmarkLearned(String unitId, String wordKey) {
        WordProgress wp = word(wordKey);
        if (wp.learned) {
            wp.learned = false;
            UnitProgress up = unit(unitId);
            up.learnedWords = Math.max(0, up.learnedWords - 1);
        }
    }

    /** 记录一次作答，并维护错题本。 */
    public void recordAnswer(String wordKey, boolean correct, long now) {
        totalAnswered++;
        if (correct) {
            totalCorrect++;
        }
        if (wordKey == null) {
            touchDay(now);
            return;
        }
        WordProgress wp = word(wordKey);
        ReviewScheduler.onAnswer(wp, correct, now);
        if (correct) {
            // 连续答对两次才移出错题本。
            if (wp.streak >= 2) {
                wrongBook.remove(wordKey);
            }
        } else {
            wrongBook.add(wordKey);
        }
        touchDay(now);
    }

    /** 记录一次完整练习的结果。 */
    public void recordQuiz(String unitId, QuizResult result, long now) {
        for (int i = 0; i < result.records.size(); i++) {
            QuestionRecord r = result.records.get(i);
            recordAnswer(r.question.wordKey, r.correct, now);
        }
        if (unitId != null) {
            UnitProgress up = unit(unitId);
            up.attempts++;
            up.lastStudy = now;
            if (result.score > up.bestScore) {
                up.bestScore = result.score;
            }
        }
        touchDay(now);
        save();
    }

    public void reset() {
        words.clear();
        units.clear();
        wrongBook.clear();
        studyDays.clear();
        totalAnswered = 0;
        totalCorrect = 0;
        save();
    }

    // ---------------------------------------------------------------- 查询

    public WordProgress word(String key) {
        WordProgress wp = words.get(key);
        if (wp == null) {
            wp = new WordProgress(key);
            words.put(key, wp);
        }
        return wp;
    }

    public WordProgress peekWord(String key) {
        return words.get(key);
    }

    public UnitProgress unit(String unitId) {
        UnitProgress up = units.get(unitId);
        if (up == null) {
            up = new UnitProgress(unitId);
            units.put(unitId, up);
        }
        return up;
    }

    public List<String> wrongBook() {
        return new ArrayList<String>(wrongBook);
    }

    public boolean inWrongBook(String key) {
        return wrongBook.contains(key);
    }

    /** 今天该复习的词条键，按生疏程度排序。 */
    public List<String> dueForReview(long now, int limit) {
        List<String> due = new ArrayList<String>();
        for (int box = 1; box <= ReviewScheduler.MAX_BOX && due.size() < limit; box++) {
            Iterator<Map.Entry<String, WordProgress>> it = words.entrySet().iterator();
            while (it.hasNext() && due.size() < limit) {
                Map.Entry<String, WordProgress> e = it.next();
                WordProgress wp = e.getValue();
                if (wp.box == box && wp.learned && ReviewScheduler.isDue(wp, now)) {
                    due.add(e.getKey());
                }
            }
        }
        return due;
    }

    public int learnedWordCount() {
        int n = 0;
        Iterator<WordProgress> it = words.values().iterator();
        while (it.hasNext()) {
            if (it.next().learned) {
                n++;
            }
        }
        return n;
    }

    public int masteredWordCount() {
        int n = 0;
        Iterator<WordProgress> it = words.values().iterator();
        while (it.hasNext()) {
            if (it.next().mastered()) {
                n++;
            }
        }
        return n;
    }

    public int totalAnswered() {
        return totalAnswered;
    }

    public int totalCorrect() {
        return totalCorrect;
    }

    public int accuracyPercent() {
        return totalAnswered == 0 ? 0 : Math.round(totalCorrect * 100f / totalAnswered);
    }

    public int studyDayCount() {
        return studyDays.size();
    }

    /** 到 now 为止的连续学习天数。 */
    public int streakDays(long now) {
        int streak = 0;
        long cursor = now;
        while (studyDays.contains(dayKey(cursor))) {
            streak++;
            cursor -= 24L * 60 * 60 * 1000;
        }
        return streak;
    }

    public boolean studiedToday(long now) {
        return studyDays.contains(dayKey(now));
    }

    // ---------------------------------------------------------------- 持久化

    public static String dayKey(long ts) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return format.format(new Date(ts));
    }

    private void touchDay(long now) {
        studyDays.add(dayKey(now));
    }

    public void save() {
        store.write(toJson());
    }

    public String toJson() {
        try {
            JSONObject root = new JSONObject();
            root.put("v", VERSION);
            JSONObject wordObj = new JSONObject();
            Iterator<Map.Entry<String, WordProgress>> wit = words.entrySet().iterator();
            while (wit.hasNext()) {
                Map.Entry<String, WordProgress> e = wit.next();
                WordProgress wp = e.getValue();
                JSONObject w = new JSONObject();
                w.put("box", wp.box);
                w.put("streak", wp.streak);
                w.put("wrong", wp.wrongCount);
                w.put("right", wp.rightCount);
                w.put("last", wp.lastReview);
                w.put("learned", wp.learned);
                wordObj.put(e.getKey(), w);
            }
            root.put("words", wordObj);

            JSONObject unitObj = new JSONObject();
            Iterator<Map.Entry<String, UnitProgress>> uit = units.entrySet().iterator();
            while (uit.hasNext()) {
                Map.Entry<String, UnitProgress> e = uit.next();
                UnitProgress up = e.getValue();
                JSONObject u = new JSONObject();
                u.put("best", up.bestScore);
                u.put("attempts", up.attempts);
                u.put("learned", up.learnedWords);
                u.put("last", up.lastStudy);
                unitObj.put(e.getKey(), u);
            }
            root.put("units", unitObj);
            root.put("wrong", new JSONArray(wrongBook));
            root.put("days", new JSONArray(studyDays));
            root.put("answered", totalAnswered);
            root.put("correct", totalCorrect);
            return root.toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    private void load() {
        String json = store.read();
        if (json == null || json.trim().length() == 0) {
            return;
        }
        try {
            JSONObject root = new JSONObject(json);
            JSONObject wordObj = root.optJSONObject("words");
            if (wordObj != null) {
                Iterator<String> keys = wordObj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject w = wordObj.getJSONObject(key);
                    WordProgress wp = new WordProgress(key);
                    wp.box = w.optInt("box", 1);
                    wp.streak = w.optInt("streak", 0);
                    wp.wrongCount = w.optInt("wrong", 0);
                    wp.rightCount = w.optInt("right", 0);
                    wp.lastReview = w.optLong("last", 0);
                    wp.learned = w.optBoolean("learned", false);
                    words.put(key, wp);
                }
            }
            JSONObject unitObj = root.optJSONObject("units");
            if (unitObj != null) {
                Iterator<String> keys = unitObj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject u = unitObj.getJSONObject(key);
                    UnitProgress up = new UnitProgress(key);
                    up.bestScore = u.optInt("best", 0);
                    up.attempts = u.optInt("attempts", 0);
                    up.learnedWords = u.optInt("learned", 0);
                    up.lastStudy = u.optLong("last", 0);
                    units.put(key, up);
                }
            }
            JSONArray wrong = root.optJSONArray("wrong");
            if (wrong != null) {
                for (int i = 0; i < wrong.length(); i++) {
                    wrongBook.add(wrong.getString(i));
                }
            }
            JSONArray days = root.optJSONArray("days");
            if (days != null) {
                for (int i = 0; i < days.length(); i++) {
                    studyDays.add(days.getString(i));
                }
            }
            totalAnswered = root.optInt("answered", 0);
            totalCorrect = root.optInt("correct", 0);
        } catch (JSONException e) {
            // 存档损坏时从空进度重新开始，不影响使用。
            words.clear();
            units.clear();
            wrongBook.clear();
            studyDays.clear();
        }
    }
}
