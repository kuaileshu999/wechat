package com.studyroom.util;

import com.studyroom.common.BusinessException;

import java.util.regex.Pattern;

public final class PhoneValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");

    private PhoneValidator() {
    }

    public static void validate(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new BusinessException("手机号必须是11位数字");
        }
    }
}
