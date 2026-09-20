package com.szprimary.english.core;

import com.szprimary.english.core.model.Curriculum;
import com.szprimary.english.core.model.Example;
import com.szprimary.english.core.model.Grade;
import com.szprimary.english.core.model.GrammarNote;
import com.szprimary.english.core.model.Phonics;
import com.szprimary.english.core.model.SentencePattern;
import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.model.Word;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 把 assets/curriculum/*.json 解析成 {@link Curriculum} 对象树。 */
public final class CurriculumLoader {

    /** 课程文件名，按年级顺序。 */
    public static final String[] FILES = {
            "curriculum/grade1.json",
            "curriculum/grade2.json",
            "curriculum/grade3.json",
            "curriculum/grade4.json",
            "curriculum/grade5.json",
            "curriculum/grade6.json",
    };

    private CurriculumLoader() {
    }

    public static Curriculum load(ContentSource source) throws IOException, JSONException {
        List<Grade> grades = new ArrayList<Grade>();
        for (int i = 0; i < FILES.length; i++) {
            grades.add(parseGrade(source.read(FILES[i])));
        }
        return new Curriculum(grades);
    }

    public static Grade parseGrade(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        int grade = root.getInt("grade");
        List<Unit> units = new ArrayList<Unit>();
        JSONArray unitArray = root.getJSONArray("units");
        for (int i = 0; i < unitArray.length(); i++) {
            units.add(parseUnit(grade, unitArray.getJSONObject(i)));
        }
        return new Grade(grade, root.getString("title"), root.optString("subtitle", ""), units);
    }

    private static Unit parseUnit(int grade, JSONObject o) throws JSONException {
        String id = o.getString("id");
        int no = o.getInt("no");

        List<Word> words = new ArrayList<Word>();
        JSONArray wordArray = o.getJSONArray("words");
        for (int i = 0; i < wordArray.length(); i++) {
            JSONObject w = wordArray.getJSONObject(i);
            words.add(new Word(id,
                    w.getString("en"),
                    w.getString("ipa"),
                    w.getString("pos"),
                    w.getString("cn"),
                    w.getString("ex_en"),
                    w.getString("ex_cn")));
        }

        List<SentencePattern> patterns = new ArrayList<SentencePattern>();
        JSONArray patternArray = o.optJSONArray("patterns");
        if (patternArray != null) {
            for (int i = 0; i < patternArray.length(); i++) {
                JSONObject p = patternArray.getJSONObject(i);
                patterns.add(new SentencePattern(id,
                        p.getString("en"),
                        p.getString("cn"),
                        parseExamples(p.optJSONArray("examples"))));
            }
        }

        List<GrammarNote> grammar = new ArrayList<GrammarNote>();
        JSONArray grammarArray = o.optJSONArray("grammar");
        if (grammarArray != null) {
            for (int i = 0; i < grammarArray.length(); i++) {
                JSONObject g = grammarArray.getJSONObject(i);
                grammar.add(new GrammarNote(
                        g.getString("title"),
                        g.getString("explain"),
                        parseExamples(g.optJSONArray("examples"))));
            }
        }

        Phonics phonics = null;
        JSONObject ph = o.optJSONObject("phonics");
        if (ph != null) {
            phonics = new Phonics(ph.getString("focus"), ph.optString("tip", ""),
                    parseStrings(ph.optJSONArray("examples")));
        }

        return new Unit(id, grade, no,
                o.getString("title_en"),
                o.getString("title_cn"),
                o.optString("topic", ""),
                o.optString("overview", ""),
                phonics, words, patterns, grammar,
                parseStrings(o.optJSONArray("tips")));
    }

    private static List<Example> parseExamples(JSONArray array) throws JSONException {
        List<Example> examples = new ArrayList<Example>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject e = array.getJSONObject(i);
                examples.add(new Example(e.getString("en"), e.getString("cn")));
            }
        }
        return examples;
    }

    private static List<String> parseStrings(JSONArray array) throws JSONException {
        List<String> list = new ArrayList<String>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
        }
        return list;
    }
}
