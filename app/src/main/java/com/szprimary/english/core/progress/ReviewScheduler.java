package com.szprimary.english.core.progress;

/** Leitner 间隔复习：答对升盒、答错回到 1 号盒。 */
public final class ReviewScheduler {

    /** 各盒的复习间隔（天），下标即盒号。 */
    private static final int[] INTERVAL_DAYS = {0, 0, 1, 2, 4, 7};
    public static final int MAX_BOX = 5;
    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    private ReviewScheduler() {
    }

    public static void onAnswer(WordProgress progress, boolean correct, long now) {
        if (correct) {
            progress.rightCount++;
            progress.streak++;
            if (progress.box < MAX_BOX) {
                progress.box++;
            }
        } else {
            progress.wrongCount++;
            progress.streak = 0;
            progress.box = 1;
        }
        progress.lastReview = now;
    }

    public static boolean isDue(WordProgress progress, long now) {
        if (progress.lastReview == 0) {
            return true;
        }
        int box = Math.max(1, Math.min(MAX_BOX, progress.box));
        return now - progress.lastReview >= INTERVAL_DAYS[box] * DAY_MS;
    }

    public static long nextReviewAt(WordProgress progress) {
        int box = Math.max(1, Math.min(MAX_BOX, progress.box));
        return progress.lastReview + INTERVAL_DAYS[box] * DAY_MS;
    }
}
