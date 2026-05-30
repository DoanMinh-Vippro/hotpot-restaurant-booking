package com.example.hotpotrestaurantbooking_backend.Validation;

public class ValidateUtil {

    //Kiểm tra khoảng trắng đầu cuối.
    public static boolean hasLeadingOrTrailingSpace(String value) {
        return value != null && !value.equals(value.trim());
    }

    //Kiểm tra có 2 dấu cách liên tiếp hay không.
    public static boolean hasMultipleSpaces(String value) {
        return value != null && value.contains("  ");
    }
    //Kiểm tra null.
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
