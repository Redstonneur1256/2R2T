package fr.redstonneur1256.omega.bot.util;

import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class StringUtil {

    public static final String NUMBERS = createRange('0', '9');
    public static final String UPPERCASE_LETTERS = createRange('A', 'Z');
    public static final String LOWERCASE_LETTERS = createRange('a', 'z');

    @NotNull
    public static String createRange(char start, char end) {
        char[] chars = new char[end - start + 1];
        for(int i = 0; i < chars.length; i++) {
            chars[i] = (char) (i + start);
        }
        return new String(chars);
    }

    /**
     * Generate a random string of the specified length using the characters
     * Uses the random from {@link ThreadLocalRandom#current()}
     *
     * @see StringUtil#generateString(int, Random, String...)
     */
    @NotNull
    public static String generateString(int length, String... characters) {
        return generateString(length, ThreadLocalRandom.current(), characters);
    }

    /**
     * Generate a random string
     *
     * @param length     the length of the string to generate
     * @param random     the random number generator to use
     * @param characters one of multiple character sets to use for the generated string
     * @return the randomly generated string
     */
    @NotNull
    public static String generateString(int length, Random random, String... characters) {
        char[] chars = new char[length];
        for(int i = 0; i < chars.length; i++) {
            var set = characters[random.nextInt(characters.length)];
            chars[i] = set.charAt(random.nextInt(set.length()));
        }
        return new String(chars);
    }

    public static int escapedIndexOf(String string, char c, char escape) {
        if(string.isEmpty()) {
            return -1;
        }
        if(string.charAt(0) == c) {
            return 0;
        }
        for(int i = 1; i < string.length(); i++) {
            if(string.charAt(i) == c && string.charAt(i - 1) != escape) {
                return i;
            }
        }
        return -1;
    }

    public static int levenshtein(String a, String b) {
        int[][] map = new int[a.length()][b.length()];

        for(int x = 0; x < a.length(); x++) {
            map[x][0] = x;
        }
        for(int y = 0; y < b.length(); y++) {
            map[0][y] = y;
        }

        for(int x = 1; x < a.length(); x++) {
            for(int y = 1; y < b.length(); y++) {
                int cost = a.charAt(x) == b.charAt(y) ? 0 : 1;
                int va = map[x - 1][y] + 1;
                int vb = map[x][y - 1] + 1;
                int vc = map[x - 1][y - 1] + cost;
                int min = Math.min(va, Math.min(vb, vc));
                map[x][y] = min;
            }
        }

        return map[a.length() - 1][b.length() - 1] + 1;
    }

}
