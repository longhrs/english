package com.szprimary.english.core.progress;

/** 单元层面的进度：学过多少词、练习最好成绩。 */
public final class UnitProgress {
    public final String unitId;
    public int bestScore;
    public int attempts;
    public int learnedWords;
    public long lastStudy;

    public UnitProgress(String unitId) {
        this.unitId = unitId;
    }

    public int stars() {
        return bestScore >= 90 ? 3 : bestScore >= 75 ? 2 : bestScore >= 60 ? 1 : 0;
    }
}
