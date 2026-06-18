package com.studyroom.util;

import com.studyroom.enums.ConsumptionMode;
import com.studyroom.enums.OrderStatus;
import com.studyroom.enums.PaymentMethod;

public final class ExportLabelHelper {

    private ExportLabelHelper() {
    }

    public static String paymentMethod(PaymentMethod method) {
        if (method == null) {
            return "";
        }
        return switch (method) {
            case UNION_PAY -> "银联";
            case WECHAT -> "微信";
            case ALIPAY -> "支付宝";
            case CASH -> "现金";
            case BANK_CARD -> "银行卡";
        };
    }

    public static String orderStatus(OrderStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case ACTIVE -> "正常";
            case PARTIAL_REFUND -> "部分退款";
            case REFUNDED -> "已退款";
        };
    }

    public static String consumptionMode(ConsumptionMode mode) {
        if (mode == null) {
            return "";
        }
        return switch (mode) {
            case AMOUNT -> "按金额消课";
            case HOURS -> "按课时消课";
        };
    }

    public static String consumptionStatus(String status) {
        if (status == null || status.isBlank()) {
            return "";
        }
        return switch (status) {
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            default -> status;
        };
    }
}
