package com.szprimary.english.core.model;

import java.util.Collections;
import java.util.List;

/** 一个核心句型，带若干中英对照例句。 */
public final class SentencePattern {
    public final String unitId;
    public final String en;
    public final String cn;
    public final List<Example> examples;

    public SentencePattern(String unitId, String en, String cn, List<Example> examples) {
        this.unitId = unitId;
        this.en = en;
        this.cn = cn;
        this.examples = Collections.unmodifiableList(examples);
    }

    public String key() {
        return unitId + "#" + en;
    }
}
