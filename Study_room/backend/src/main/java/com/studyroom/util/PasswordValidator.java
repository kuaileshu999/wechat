package com.studyroom.util;

import com.studyroom.common.BusinessException;

import java.util.regex.Pattern;

public final class PasswordValidator {

    private static final Pattern ALL_DIGITS = Pattern.compile("^\\d+$");

    private PasswordValidator() {
    }

    public static void validate(String password) {
        if (password == null || password.length() < 6) {
            throw new BusinessException("密码至少6位");
        }
        if (ALL_DIGITS.matcher(password).matches()) {
            throw new BusinessException("密码不能全是数字");
        }
    }
}
