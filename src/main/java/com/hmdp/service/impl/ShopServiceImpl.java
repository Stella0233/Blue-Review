package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        //1. 从Redis查缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2. 存在，返回
        if(StrUtil.isNotBlank(shopJson)){
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
        //存在，但是是空值
        //StrUtil.isNotBlank==false && !=null，则代表是空值
        if(shopJson != null){
            return Result.fail("不存在该店铺！");
        }

        //3. 不存在，查数据库
        Shop shop = getById(id);
        //4. 数据库不存在，缓存空值
        if (shop == null) {
            stringRedisTemplate.opsForValue().set(key,"",RedisConstants.CACHE_NULL_TTL,TimeUnit.MINUTES);
        }
        //5. 数据库存在，写入缓存
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //6.返回
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result upadate(Shop shop) {
        String key = RedisConstants.CACHE_SHOP_KEY + shop.getId();
        //1.检查shop是否存在
        if(shop.getId()==null){
            return Result.fail("需要更新的shop idb不存在");
        }
        //2.更新数据库
        updateById(shop);
        //3.删除缓存
        stringRedisTemplate.delete(key);
        return Result.ok(shop);
    }
}
