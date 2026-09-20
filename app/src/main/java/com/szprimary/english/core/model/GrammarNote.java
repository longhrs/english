package com.szprimary.english.core.model;

import java.util.Collections;
import java.util.List;

/** 知识引导中的语法/用法讲解。 */
public final class GrammarNote {
    public final String title;
    public final String explain;
    public final List<Example> examples;

    public GrammarNote(String title, String explain, List<Example> examples) {
        this.title = title;
        this.explain = explain;
        this.examples = Collections.unmodifiableList(examples);
    }
}
