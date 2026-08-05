package com.example.hotpotrestaurantbooking_backend.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class VNCharacterUtils {

    public static String removeAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("")
                .replace('Đ', 'D').replace('đ', 'd');
    }
}