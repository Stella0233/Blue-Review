package com.hmdp.service.impl;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        String key = RedisConstants.CACHE_TYPE_LIST;
        //1.查询缓存
        String typeJson = stringRedisTemplate.opsForValue().get(key);
        //2.查到，返回
        if(StrUtil.isNotBlank(typeJson)){
            List<ShopType> list = JSONUtil.toList(typeJson,ShopType.class);
            return Result.ok(list);
        }
        //3.没查到，查数据库
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        //4.数据库没查到，抛异常
        if(CollectionUtil.isEmpty(shopTypeList)){
            return Result.fail("No Shop Type Data Existing.");
        }
        //5.查到数据，写回缓存，返回
        stringRedisTemplate.opsForValue().set("shop_type", JSONUtil.toJsonStr(shopTypeList));
        return Result.ok(shopTypeList);
    }
}

