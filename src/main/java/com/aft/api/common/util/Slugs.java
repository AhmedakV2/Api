package com.aft.api.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Slugs {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_DASH = Pattern.compile("(^-|-$)");

    private Slugs() {
    }

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("slug icin bos deger verilemez");
        }
        String ascii = Normalizer.normalize(turkishToAscii(input), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String slug = NON_ALNUM.matcher(ascii.toLowerCase(Locale.ROOT)).replaceAll("-");
        return EDGE_DASH.matcher(slug).replaceAll("");
    }

    private static String turkishToAscii(String value) {
        return value.replace('ı', 'i').replace('İ', 'I')
                .replace('ş', 's').replace('Ş', 'S')
                .replace('ğ', 'g').replace('Ğ', 'G')
                .replace('ü', 'u').replace('Ü', 'U')
                .replace('ö', 'o').replace('Ö', 'O')
                .replace('ç', 'c').replace('Ç', 'C');
    }
}
