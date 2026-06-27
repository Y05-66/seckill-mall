package com.seckill.mall.service;

import com.seckill.mall.entity.Refund;

import java.util.List;

public interface RefundService {
    void applyRefund(Long userId, Refund refund);
    List<Refund> getUserRefunds(Long userId);
    List<Refund> getAllRefunds();
    void approveRefund(Long refundId, String adminNote);
    void rejectRefund(Long refundId, String adminNote);
}
