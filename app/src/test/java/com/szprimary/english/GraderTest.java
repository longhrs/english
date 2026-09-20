package com.szprimary.english;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.szprimary.english.core.quiz.Answer;
import com.szprimary.english.core.quiz.Grader;
import com.szprimary.english.core.quiz.Question;
import com.szprimary.english.core.quiz.QuestionType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 判分规则。 */
public class GraderTest {

    private Question spelling(String answer) {
        return new Question(QuestionType.SPELLING, "根据中文写出单词：苹果", "首字母 a",
                new ArrayList<String>(), -1, answer, "apple", "apple", "g1u1/apple", "g1u1");
    }

    private Question choice() {
        List<String> options = new ArrayList<String>(Arrays.asList("red", "blue", "green", "black"));
        return new Question(QuestionType.EN_TO_CN, "选出 red 的中文意思", "", options, 0,
                "red", "red 红色", "red", "g1u2/red", "g1u2");
    }

    @Test
    public void normalizeIgnoresCaseSpaceAndPunctuation() {
        assertEquals("i am happy", Grader.normalize("  I am   happy!  "));
        assertEquals("it is a pen", Grader.normalize("It is a pen."));
        assertEquals("i'm tom", Grader.normalize("I’m Tom"));
    }

    @Test
    public void spellingAcceptsCaseAndPunctuationDifferences() {
        assertTrue(Grader.isCorrect(spelling("apple"), Answer.text("Apple")));
        assertTrue(Grader.isCorrect(spelling("apple"), Answer.text("  apple ")));
        assertTrue(Grader.isCorrect(spelling("I am six."), Answer.text("i am six")));
        assertFalse(Grader.isCorrect(spelling("apple"), Answer.text("aple")));
        assertFalse(Grader.isCorrect(spelling("apple"), Answer.skipped()));
    }

    @Test
    public void choiceNeedsExactIndex() {
        assertTrue(Grader.isCorrect(choice(), Answer.choice(0)));
        assertFalse(Grader.isCorrect(choice(), Answer.choice(2)));
        assertFalse(Grader.isCorrect(choice(), Answer.text("red")));
        assertFalse(Grader.isCorrect(choice(), null));
    }

    @Test
    public void sentenceOrderIgnoresExtraSpaces() {
        List<String> tokens = new ArrayList<String>(Arrays.asList("am", "I", "six."));
        Question question = new Question(QuestionType.SENTENCE_ORDER, "连词成句：我六岁。", "",
                tokens, -1, "I am six.", "语序", "I am six.", null, "g1u3");
        assertTrue(Grader.isCorrect(question, Answer.text("I  am six")));
        assertTrue(Grader.isCorrect(question, Answer.text("i am six.")));
        assertFalse(Grader.isCorrect(question, Answer.text("six I am")));
    }
}
