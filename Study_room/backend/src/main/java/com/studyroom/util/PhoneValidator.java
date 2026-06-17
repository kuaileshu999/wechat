package com.studyroom.util;

import com.studyroom.common.BusinessException;

import java.util.regex.Pattern;

public final class PhoneValidator {

    private static final Pattern DIGITS_PATTERN = Pattern.compile("^\\d+$");

    private PhoneValidator() {
    }

    public static void validate(String phone) {
        validateLength(phone, 11, "手机号必须是11位数字");
    }

    public static void validateEmployee(String phone) {
        validate(phone);
    }

    private static void validateLength(String phone, int length, String message) {
        if (phone == null) {
            throw new BusinessException(message);
        }
        String trimmed = phone.trim();
        if (!DIGITS_PATTERN.matcher(trimmed).matches() || trimmed.length() != length) {
            throw new BusinessException(message);
        }
    }
}
