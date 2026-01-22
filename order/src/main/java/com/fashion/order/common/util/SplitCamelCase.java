package com.fashion.order.common.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SplitCamelCase {
    public static String convertCamelCase(String input) {
        if (input == null || input.isEmpty()) return "";

        // Thêm khoảng trắng trước các chữ in hoa
        String spaced = input.replaceAll("([a-z])([A-Z])", "$1 $2");

        // Viết hoa chữ cái đầu mỗi từ
        return Arrays.stream(spaced.split(" "))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
