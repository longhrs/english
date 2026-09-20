package com.szprimary.english.core.model;

import java.util.Collections;
import java.util.List;

/** 自然拼读 / 语音板块。 */
public final class Phonics {
    public final String focus;
    public final String tip;
    public final List<String> examples;

    public Phonics(String focus, String tip, List<String> examples) {
        this.focus = focus;
        this.tip = tip;
        this.examples = Collections.unmodifiableList(examples);
    }
}
