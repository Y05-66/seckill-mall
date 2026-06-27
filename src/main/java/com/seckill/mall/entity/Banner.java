package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图实体类
 */
@Data
@TableName("t_banner")
public class Banner {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 图片URL */
    private String imageUrl;

    /** 跳转链接 */
    private String linkUrl;

    /** 排序（越小越靠前） */
    private Integer sortOrder;

    /** 状态（0-禁用，1-启用） */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
