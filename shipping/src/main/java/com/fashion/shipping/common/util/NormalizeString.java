package com.fashion.shipping.common.util;

import java.text.Normalizer;

public class NormalizeString {
    public static String toNormalize(String name){
        if (name == null) return null;
        
        // Remove Vietnamese accents
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
        
        // Convert to lowercase and replace spaces with hyphens
        return normalized.toLowerCase()
            .trim()
            .replaceAll("\\s+", "-")
            .replaceAll("[^a-z0-9-]", "");
    }
}
