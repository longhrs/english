package com.szprimary.english.core.progress;

/** 单个词条的掌握情况，box 是 Leitner 复习盒（1 最生疏，5 最熟）。 */
public final class WordProgress {
    public final String key;
    public int box = 1;
    public int streak;
    public int wrongCount;
    public int rightCount;
    public long lastReview;
    public boolean learned;

    public WordProgress(String key) {
        this.key = key;
    }

    public boolean mastered() {
        return box >= 5 && streak >= 3;
    }
}
