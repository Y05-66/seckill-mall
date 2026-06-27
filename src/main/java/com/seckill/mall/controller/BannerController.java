package com.seckill.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.common.Result;
import com.seckill.mall.entity.Banner;
import com.seckill.mall.mapper.BannerMapper;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.BannerKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Api(tags = "轮播图管理")
@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerMapper bannerMapper;
    private final RedisService redisService;

    @ApiOperation("获取启用的轮播图列表（前台）")
    @GetMapping("/list")
    public Result<List<Banner>> getActiveBanners() {
        // 优先从Redis缓存获取
        List<Banner> cached = redisService.get(BannerKey.BANNER_LIST, "active");
        if (cached != null) {
            return Result.success(cached);
        }

        // 缓存未命中，查询数据库
        List<Banner> list = bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1)
                        .orderByAsc(Banner::getSortOrder));

        // 写入Redis缓存
        redisService.set(BannerKey.BANNER_LIST, "active", list);
        return Result.success(list);
    }

    @ApiOperation("获取所有轮播图（管理端）")
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Banner>> getAllBanners() {
        return Result.success(bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>().orderByAsc(Banner::getSortOrder)));
    }

    @ApiOperation("添加轮播图")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> addBanner(@RequestBody Banner banner) {
        banner.setCreateTime(LocalDateTime.now());
        banner.setUpdateTime(LocalDateTime.now());
        if (banner.getSortOrder() == null) banner.setSortOrder(0);
        if (banner.getStatus() == null) banner.setStatus(1);
        bannerMapper.insert(banner);

        // 清除轮播图缓存
        redisService.delete(BannerKey.BANNER_LIST, "active");
        return Result.success();
    }

    @ApiOperation("更新轮播图")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        banner.setUpdateTime(LocalDateTime.now());
        bannerMapper.updateById(banner);

        // 清除轮播图缓存
        redisService.delete(BannerKey.BANNER_LIST, "active");
        return Result.success();
    }

    @ApiOperation("删除轮播图")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> deleteBanner(@PathVariable Long id) {
        bannerMapper.deleteById(id);

        // 清除轮播图缓存
        redisService.delete(BannerKey.BANNER_LIST, "active");
        return Result.success();
    }
}
