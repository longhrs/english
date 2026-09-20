package com.szprimary.english.core.model;

/** 一个词汇条目：单词、音标、词性、中文释义和例句。 */
public final class Word {
    public final String unitId;
    public final String en;
    public final String ipa;
    public final String pos;
    public final String cn;
    public final String exEn;
    public final String exCn;

    public Word(String unitId, String en, String ipa, String pos, String cn, String exEn, String exCn) {
        this.unitId = unitId;
        this.en = en;
        this.ipa = ipa;
        this.pos = pos;
        this.cn = cn;
        this.exEn = exEn;
        this.exCn = exCn;
    }

    /** 全局唯一键，用于记录学习进度和错题。 */
    public String key() {
        return unitId + "/" + en;
    }

    @Override
    public String toString() {
        return en + " " + ipa + " " + cn;
    }
}
