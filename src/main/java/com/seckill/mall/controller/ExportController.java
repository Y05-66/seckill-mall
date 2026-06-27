package com.seckill.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.entity.SeckillOrder;
import com.seckill.mall.entity.User;
import com.seckill.mall.mapper.SeckillOrderMapper;
import com.seckill.mall.mapper.UserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.List;

@Api(tags = "数据导出")
@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final SeckillOrderMapper seckillOrderMapper;
    private final UserMapper userMapper;

    @ApiOperation("导出订单Excel")
    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportOrders(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=orders.xlsx");

        List<SeckillOrder> orders = seckillOrderMapper.selectList(
                new LambdaQueryWrapper<SeckillOrder>().orderByDesc(SeckillOrder::getCreateTime));

        try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("订单列表");

        // 表头
        String[] headers = {"订单号", "用户ID", "秒杀商品ID", "状态", "创建时间", "支付时间"};
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 数据（与Constants.ORDER_*状态码对应：0-未支付，1-已支付，2-已取消，3-已超时，4-已退款）
        String[] statusDesc = {"待支付", "已支付", "已取消", "已超时", "已退款"};
        for (int i = 0; i < orders.size(); i++) {
            SeckillOrder order = orders.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(order.getOrderNo());
            row.createCell(1).setCellValue(order.getUserId());
            row.createCell(2).setCellValue(order.getSeckillGoodsId());
            row.createCell(3).setCellValue(order.getStatus() >= 0 && order.getStatus() < statusDesc.length ? statusDesc[order.getStatus()] : "未知");
            row.createCell(4).setCellValue(order.getCreateTime() != null ? order.getCreateTime().toString() : "");
            row.createCell(5).setCellValue(order.getPayTime() != null ? order.getPayTime().toString() : "");
        }

        // 自动列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        OutputStream out = response.getOutputStream();
        workbook.write(out);
        out.flush();
        }
    }

    @ApiOperation("导出用户Excel")
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportUsers(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=users.xlsx");

        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime));

        try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("用户列表");

        // 表头
        String[] headers = {"ID", "用户名", "昵称", "手机号", "邮箱", "角色", "状态", "注册时间"};
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 数据
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getUsername());
            row.createCell(2).setCellValue(user.getNickname() != null ? user.getNickname() : "");
            row.createCell(3).setCellValue(user.getPhone() != null ? user.getPhone() : "");
            row.createCell(4).setCellValue(user.getEmail() != null ? user.getEmail() : "");
            row.createCell(5).setCellValue(user.getRole() == 1 ? "管理员" : "普通用户");
            row.createCell(6).setCellValue(user.getStatus() == 0 ? "正常" : "禁用");
            row.createCell(7).setCellValue(user.getCreateTime() != null ? user.getCreateTime().toString() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        OutputStream out = response.getOutputStream();
        workbook.write(out);
        out.flush();
        }
    }
}
