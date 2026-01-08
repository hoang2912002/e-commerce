package com.fashion.notification.common.util;

import java.text.Normalizer;
import java.util.Locale;

public class SlugUtil {
    public static String toSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        input = input.replace('Đ', 'D').replace('đ', 'd');

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        String slug = normalized.replaceAll("\\p{M}", "");

        slug = slug.toLowerCase(Locale.ROOT);

        slug = slug.replaceAll("[^a-z0-9\\s-]", "");

        slug = slug.trim().replaceAll("\\s+", "-");

        slug = slug.replaceAll("-+", "-");
        return slug;
    }
}
