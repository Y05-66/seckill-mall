package com.seckill.mall.service;

import com.seckill.mall.dto.OrderDTO;
import com.seckill.mall.common.BusinessException;
import com.seckill.mall.dto.OrderStatisticsDTO;

import java.util.List;

/**
 * 订单业务服务接口
 * <p>
 * 定义订单查询、支付、取消等业务方法
 * 管理端方法（全部订单列表，订单统计）合并到该接口
 * 通过Controller路径（。admin/order）和@PreAuthorize注解实现权限隔离
 * </p>
 */
public interface OrderService {

    /**
     * 获取用户的订单列表（按创建时间倒序）
     *
     * @param userId 用户ID
     * @return 订单DTO列表
     */
    List<OrderDTO> listUserOrders(Long userId);

    /**
     * 获取订单详情（校验订单归属）
     *
     * @param orderNo 订单编号
     * @param userId  当前用户ID（校验订单归属，防止越权）
     * @return 订单详情DTO
     * @throws BusinessException 订单不存在或不属于该用户时抛出异常
     */
    OrderDTO getOrderDetail(String orderNo, Long userId);

    /**
     * 模拟支付订单
     *
     * @param orderNo 订单编号
     * @param userId  用户ID（校验订单归属）
     * @throws BusinessException 订单不存在、已支付或不属于该用户时抛出异常
     */
    void payOrder(String orderNo, Long userId);

    /**
     * 取消订单（释放秒杀库存）
     *
     * @param orderNo 订单编号
     * @param userId  用户ID（校验订单归属）
     * @throws BusinessException 订单不存在或非未支付状态时抛出异常
     */
    void cancelOrder(String orderNo, Long userId);

    /**
     * 删除订单（仅限已取消、已超时、已退款的订单）
     *
     * @param orderNo 订单编号
     * @param userId  用户ID（校验订单归属）
     * @throws BusinessException 订单不存在、不属于该用户或状态不允许删除时抛出异常
     */
    void deleteOrder(String orderNo, Long userId);

    /**
     * 获取全部订单列表（管理端使用，支持按状态和订单号筛选
     * <p>
     * 管理端需要查看全量订单数据，并可按订单状态或订单号进行筛选
     * 当status为null是返回所有状态的订单：当orderNo非null是按订单号模糊匹配
     * </p>
     *
     * @param status 订单状态筛选条件（null=全部，0=未支付，1=已支付，2=已取消，3=已超时）
     * @param orderNo 订单号筛选条件（null=全部，非null时按订单号精确匹配）
     * @return 订单DTO列表（按创建时间倒序）
     */
    List<OrderDTO> listAllOrders(Integer status, String orderNo);

    /**
     * 获取订单统计数据（管理端使用）
     * <p>
     * 统计各状态的订单数量，已支付订单的总营收金额，今日新增订单数
     * 管理端通过此数据了解秒杀活动的整体运营情况
     * </p>
     *
     * @return 订单统计DTO
     */
    OrderStatisticsDTO getOrderStatistics();
}
