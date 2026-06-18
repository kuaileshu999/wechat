package com.studyroom.service;

import com.studyroom.dto.FinanceReportVO;
import com.studyroom.dto.OrderListVO;
import com.studyroom.security.SecurityUtils;
import com.studyroom.util.ExcelExportHelper;
import com.studyroom.util.ExportLabelHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final OrderService orderService;
    private final FinanceService financeService;
    private final ConsumptionService consumptionService;

    public void exportOrders(HttpServletResponse response, Long campusId, LocalDate startDate, LocalDate endDate,
                             String keyword, String unionPayOrderNo, String orderNo) throws IOException {
        SecurityUtils.checkPermission("order:export");
        List<OrderListVO> orders = orderService.listAllForExport(campusId, startDate, endDate, keyword, unionPayOrderNo, orderNo);
        Workbook workbook = ExcelExportHelper.createWorkbook();
        Sheet sheet = workbook.createSheet("订单");
        CellStyle headerStyle = ExcelExportHelper.headerStyle(workbook);
        Row header = sheet.createRow(0);
        ExcelExportHelper.writeHeader(header, headerStyle,
                "订单号", "校区", "学员姓名", "学员手机号", "课程名称", "课时数", "收款金额", "收款方式",
                "银联单号", "收款日期", "销售人", "主讲老师", "备注", "已消金额", "已消课时", "已退金额", "状态");
        int rowNum = 1;
        for (OrderListVO order : orders) {
            Row row = sheet.createRow(rowNum++);
            ExcelExportHelper.setCell(row, 0, order.getOrderNo());
            ExcelExportHelper.setCell(row, 1, order.getCampusName());
            ExcelExportHelper.setCell(row, 2, order.getStudentName());
            ExcelExportHelper.setCell(row, 3, order.getStudentPhone());
            ExcelExportHelper.setCell(row, 4, order.getCourseName());
            ExcelExportHelper.setCell(row, 5, order.getTotalHours());
            ExcelExportHelper.setCell(row, 6, order.getPaidAmount());
            ExcelExportHelper.setCell(row, 7, ExportLabelHelper.paymentMethod(order.getPaymentMethod()));
            ExcelExportHelper.setCell(row, 8, order.getUnionPayOrderNo());
            ExcelExportHelper.setCell(row, 9, order.getPaymentDate());
            ExcelExportHelper.setCell(row, 10, order.getSalespersonNames());
            ExcelExportHelper.setCell(row, 11, order.getTeacherNames());
            ExcelExportHelper.setCell(row, 12, order.getRemark());
            ExcelExportHelper.setCell(row, 13, order.getConsumedAmount());
            ExcelExportHelper.setCell(row, 14, order.getConsumedHours());
            ExcelExportHelper.setCell(row, 15, order.getRefundedAmount());
            ExcelExportHelper.setCell(row, 16, ExportLabelHelper.orderStatus(order.getStatus()));
        }
        autoSizeColumns(sheet, 17);
        ExcelExportHelper.writeResponse(response, workbook, "订单导出");
    }

    public void exportFinance(HttpServletResponse response, String type, LocalDate startDate, LocalDate endDate,
                              String month, Long campusId) throws IOException {
        SecurityUtils.checkPermission("finance:export");
        List<FinanceReportVO> data;
        String sheetName;
        if ("month".equals(type)) {
            data = financeService.reportByMonth(month, campusId);
            sheetName = "财务按月";
        } else if ("campus".equals(type)) {
            data = financeService.reportByCampus(startDate, endDate);
            sheetName = "财务按校区";
        } else {
            data = financeService.reportByDay(startDate, endDate, campusId);
            sheetName = "财务按天";
        }

        Workbook workbook = ExcelExportHelper.createWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        CellStyle headerStyle = ExcelExportHelper.headerStyle(workbook);
        Row header = sheet.createRow(0);
        if ("campus".equals(type)) {
            ExcelExportHelper.writeHeader(header, headerStyle,
                    "校区", "收款金额", "已消课金额", "待消课金额");
        } else if ("month".equals(type)) {
            ExcelExportHelper.writeHeader(header, headerStyle,
                    "月份", "校区", "收款金额", "已消课金额", "待消课金额");
        } else {
            ExcelExportHelper.writeHeader(header, headerStyle,
                    "日期", "校区", "收款金额", "已消课金额", "待消课金额");
        }

        int rowNum = 1;
        for (FinanceReportVO item : data) {
            Row row = sheet.createRow(rowNum++);
            if ("campus".equals(type)) {
                ExcelExportHelper.setCell(row, 0, item.getCampusName());
                ExcelExportHelper.setCell(row, 1, item.getTotalPaidAmount());
                ExcelExportHelper.setCell(row, 2, item.getTotalConsumedAmount());
                ExcelExportHelper.setCell(row, 3, item.getTotalPendingAmount());
            } else if ("month".equals(type)) {
                ExcelExportHelper.setCell(row, 0, item.getMonth());
                ExcelExportHelper.setCell(row, 1, item.getCampusName());
                ExcelExportHelper.setCell(row, 2, item.getTotalPaidAmount());
                ExcelExportHelper.setCell(row, 3, item.getTotalConsumedAmount());
                ExcelExportHelper.setCell(row, 4, item.getTotalPendingAmount());
            } else {
                ExcelExportHelper.setCell(row, 0, item.getDate());
                ExcelExportHelper.setCell(row, 1, item.getCampusName());
                ExcelExportHelper.setCell(row, 2, item.getTotalPaidAmount());
                ExcelExportHelper.setCell(row, 3, item.getTotalConsumedAmount());
                ExcelExportHelper.setCell(row, 4, item.getTotalPendingAmount());
            }
        }
        autoSizeColumns(sheet, "campus".equals(type) ? 4 : 5);
        ExcelExportHelper.writeResponse(response, workbook, "财务导出");
    }

    public void exportConsumptions(HttpServletResponse response, String keyword) throws IOException {
        SecurityUtils.checkPermission("consumption:export");
        var records = consumptionService.listAllCompletedForExport(keyword);
        Workbook workbook = ExcelExportHelper.createWorkbook();
        Sheet sheet = workbook.createSheet("消课记录");
        CellStyle headerStyle = ExcelExportHelper.headerStyle(workbook);
        Row header = sheet.createRow(0);
        ExcelExportHelper.writeHeader(header, headerStyle,
                "订单号", "校区", "学员姓名", "手机号", "课程名称", "学科", "上课老师", "消课方式",
                "消课金额", "消课课时", "开始时间", "结束时间", "备注", "状态", "取消原因", "创建时间");
        int rowNum = 1;
        for (var record : records) {
            Row row = sheet.createRow(rowNum++);
            ExcelExportHelper.setCell(row, 0, record.getOrderNo());
            ExcelExportHelper.setCell(row, 1, record.getCampusName());
            ExcelExportHelper.setCell(row, 2, record.getStudentName());
            ExcelExportHelper.setCell(row, 3, record.getStudentPhone());
            ExcelExportHelper.setCell(row, 4, record.getCourseName());
            ExcelExportHelper.setCell(row, 5, record.getSubjectName());
            ExcelExportHelper.setCell(row, 6, record.getTeacherName());
            ExcelExportHelper.setCell(row, 7, ExportLabelHelper.consumptionMode(record.getConsumptionMode()));
            ExcelExportHelper.setCell(row, 8, record.getConsumedAmount());
            ExcelExportHelper.setCell(row, 9, record.getConsumedHours());
            ExcelExportHelper.setCell(row, 10, record.getClassTime());
            ExcelExportHelper.setCell(row, 11, record.getClassEndTime());
            ExcelExportHelper.setCell(row, 12, record.getRemark());
            ExcelExportHelper.setCell(row, 13, ExportLabelHelper.consumptionStatus(record.getStatus()));
            ExcelExportHelper.setCell(row, 14, record.getCancelReason());
            ExcelExportHelper.setCell(row, 15, record.getCreatedAt());
        }
        autoSizeColumns(sheet, 16);
        ExcelExportHelper.writeResponse(response, workbook, "消课导出");
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            try {
                sheet.autoSizeColumn(i);
            } catch (Exception ignored) {
                sheet.setColumnWidth(i, 4000);
            }
        }
    }
}
