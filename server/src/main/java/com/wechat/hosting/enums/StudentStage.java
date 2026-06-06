package com.wechat.hosting.enums;

public final class StudentStage {
    public static final int CONVERSION = 1;
    public static final int UNDERTAKING = 2;
    public static final int COMPLETED = 3;

    private StudentStage() {
    }

    public static String label(int stage) {
        return switch (stage) {
            case CONVERSION -> "转化期";
            case UNDERTAKING -> "承接期";
            case COMPLETED -> "已结课";
            default -> "未知";
        };
    }
}
