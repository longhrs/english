package com.szprimary.english.core.quiz;

import com.szprimary.english.core.model.Example;
import com.szprimary.english.core.model.SentencePattern;
import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.model.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/** 根据词汇和句型自动生成练习题。 */
public final class QuizGenerator {

    private QuizGenerator() {
    }

    /** 单元练习：本单元词汇 + 句型，干扰项优先取同单元的词。 */
    public static List<Question> forUnit(Unit unit, List<Word> distractorPool, QuizOptions options) {
        List<SentencePattern> patterns = options.allowPatterns ? unit.patterns : new ArrayList<SentencePattern>();
        return build(unit.words, patterns, distractorPool, options);
    }

    /** 错题本 / 复习练习：题目来自指定词表。 */
    public static List<Question> forWords(List<Word> words, List<Word> distractorPool, QuizOptions options) {
        return build(words, new ArrayList<SentencePattern>(), distractorPool, options);
    }

    private static List<Question> build(List<Word> words, List<SentencePattern> patterns,
                                        List<Word> distractorPool, QuizOptions options) {
        Random random = new Random(options.seed);

        List<Word> shuffledWords = new ArrayList<Word>(words);
        Collections.shuffle(shuffledWords, random);

        List<Question> patternQuestions = new ArrayList<Question>();
        List<SentencePattern> shuffledPatterns = new ArrayList<SentencePattern>(patterns);
        Collections.shuffle(shuffledPatterns, random);
        for (int i = 0; i < shuffledPatterns.size(); i++) {
            Question q = patternQuestion(shuffledPatterns.get(i), distractorPool, random);
            if (q != null) {
                patternQuestions.add(q);
            }
            Question order = orderQuestion(shuffledPatterns.get(i), random);
            if (order != null) {
                patternQuestions.add(order);
            }
        }

        int wanted = Math.max(1, options.count);
        int patternQuota = patternQuestions.isEmpty() ? 0 : Math.min(patternQuestions.size(), Math.max(1, wanted / 4));
        int wordQuota = Math.max(0, wanted - patternQuota);

        QuestionType[] cycle = options.allowSpelling
                ? new QuestionType[]{QuestionType.CN_TO_EN, QuestionType.EN_TO_CN, QuestionType.SPELLING}
                : new QuestionType[]{QuestionType.CN_TO_EN, QuestionType.EN_TO_CN};

        List<Question> questions = new ArrayList<Question>();
        for (int i = 0; i < shuffledWords.size() && questions.size() < wordQuota; i++) {
            Word word = shuffledWords.get(i);
            QuestionType type = cycle[i % cycle.length];
            if (type == QuestionType.SPELLING && word.en.length() > 10) {
                type = QuestionType.CN_TO_EN;
            }
            Question q = wordQuestion(word, type, distractorPool, random);
            if (q != null) {
                questions.add(q);
            }
        }
        for (int i = 0; i < patternQuestions.size() && questions.size() < wanted; i++) {
            questions.add(patternQuestions.get(i));
        }
        // 词条不够时循环补足，保证题量稳定。
        for (int i = 0; questions.size() < wanted && !shuffledWords.isEmpty(); i++) {
            Word word = shuffledWords.get(i % shuffledWords.size());
            Question q = wordQuestion(word, cycle[(i + 1) % cycle.length], distractorPool, random);
            if (q != null) {
                questions.add(q);
            }
            if (i > wanted * 4) {
                break;
            }
        }
        Collections.shuffle(questions, random);
        return questions;
    }

    private static Question wordQuestion(Word word, QuestionType type, List<Word> pool, Random random) {
        String explain = word.en + "  " + word.ipa + "  " + word.pos + "  " + word.cn
                + "\n例：" + word.exEn + "\n    " + word.exCn;
        if (type == QuestionType.SPELLING) {
            String hint = "首字母 " + word.en.charAt(0) + "，共 " + word.en.length() + " 个字母";
            return new Question(type, "根据中文写出单词：" + word.cn, hint,
                    new ArrayList<String>(), -1, word.en, explain, word.en, word.key(), word.unitId);
        }
        boolean cnToEn = type == QuestionType.CN_TO_EN;
        List<Word> distractors = pickDistractors(word, pool, 3, random);
        if (distractors.size() < 3) {
            return null;
        }
        List<String> options = new ArrayList<String>();
        options.add(cnToEn ? word.en : word.cn);
        for (int i = 0; i < distractors.size(); i++) {
            options.add(cnToEn ? distractors.get(i).en : distractors.get(i).cn);
        }
        String correct = options.get(0);
        Collections.shuffle(options, random);
        int correctIndex = options.indexOf(correct);
        String prompt = cnToEn ? ("选出「" + word.cn + "」对应的单词") : ("选出 " + word.en + " 的中文意思");
        String hint = cnToEn ? word.pos : (word.ipa + "  " + word.pos);
        return new Question(type, prompt, hint, options, correctIndex, correct, explain,
                word.en, word.key(), word.unitId);
    }

    private static Question patternQuestion(SentencePattern pattern, List<Word> pool, Random random) {
        Example example = pickExample(pattern, random);
        if (example == null) {
            return null;
        }
        String[] tokens = example.en.split(" ");
        int blankIndex = -1;
        for (int i = 0; i < tokens.length; i++) {
            String bare = strip(tokens[i]);
            if (bare.length() >= 3 && (blankIndex < 0 || bare.length() > strip(tokens[blankIndex]).length())) {
                blankIndex = i;
            }
        }
        if (blankIndex < 0) {
            return null;
        }
        String answer = strip(tokens[blankIndex]);
        StringBuilder sentence = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sentence.append(' ');
            }
            sentence.append(i == blankIndex ? tokens[i].replace(answer, "_____") : tokens[i]);
        }

        List<String> options = new ArrayList<String>();
        options.add(answer);
        Set<String> used = new HashSet<String>();
        used.add(answer.toLowerCase(Locale.ENGLISH));
        for (int guard = 0; options.size() < 4 && guard < 200 && !pool.isEmpty(); guard++) {
            String candidate = pool.get(random.nextInt(pool.size())).en;
            if (used.add(candidate.toLowerCase(Locale.ENGLISH))) {
                options.add(candidate);
            }
        }
        if (options.size() < 4) {
            return null;
        }
        Collections.shuffle(options, random);
        int correctIndex = options.indexOf(answer);
        String explain = "句型：" + pattern.en + "\n" + pattern.cn + "\n完整句子：" + example.en + "\n" + example.cn;
        return new Question(QuestionType.PATTERN_BLANK, "补全句子：" + sentence, example.cn,
                options, correctIndex, answer, explain, example.en, null, pattern.unitId);
    }

    private static Question orderQuestion(SentencePattern pattern, Random random) {
        Example example = pickExample(pattern, random);
        if (example == null) {
            return null;
        }
        String[] tokens = example.en.split(" ");
        if (tokens.length < 3 || tokens.length > 8) {
            return null;
        }
        List<String> shuffled = new ArrayList<String>();
        for (int i = 0; i < tokens.length; i++) {
            shuffled.add(tokens[i]);
        }
        for (int guard = 0; guard < 8; guard++) {
            Collections.shuffle(shuffled, random);
            if (!join(shuffled).equals(example.en)) {
                break;
            }
        }
        String explain = "句型：" + pattern.en + "\n" + pattern.cn + "\n正确语序：" + example.en;
        return new Question(QuestionType.SENTENCE_ORDER, "连词成句：" + example.cn, "点击词块按正确顺序组成句子",
                shuffled, -1, example.en, explain, example.en, null, pattern.unitId);
    }

    private static Example pickExample(SentencePattern pattern, Random random) {
        if (pattern.examples.isEmpty()) {
            return null;
        }
        return pattern.examples.get(random.nextInt(pattern.examples.size()));
    }

    private static List<Word> pickDistractors(Word target, List<Word> pool, int n, Random random) {
        List<Word> samePos = new ArrayList<Word>();
        List<Word> others = new ArrayList<Word>();
        for (int i = 0; i < pool.size(); i++) {
            Word w = pool.get(i);
            if (w.en.equalsIgnoreCase(target.en) || w.cn.equals(target.cn)) {
                continue;
            }
            if (w.pos.equals(target.pos)) {
                samePos.add(w);
            } else {
                others.add(w);
            }
        }
        Collections.shuffle(samePos, random);
        Collections.shuffle(others, random);

        List<Word> picked = new ArrayList<Word>();
        Set<String> usedEn = new HashSet<String>();
        Set<String> usedCn = new HashSet<String>();
        usedEn.add(target.en.toLowerCase(Locale.ENGLISH));
        usedCn.add(target.cn);
        List<Word> ordered = new ArrayList<Word>(samePos);
        ordered.addAll(others);
        for (int i = 0; i < ordered.size() && picked.size() < n; i++) {
            Word w = ordered.get(i);
            if (usedEn.add(w.en.toLowerCase(Locale.ENGLISH)) && usedCn.add(w.cn)) {
                picked.add(w);
            }
        }
        return picked;
    }

    private static String strip(String token) {
        return token.replaceAll("[^A-Za-z'’-]", "");
    }

    private static String join(List<String> tokens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(tokens.get(i));
        }
        return sb.toString();
    }
}
