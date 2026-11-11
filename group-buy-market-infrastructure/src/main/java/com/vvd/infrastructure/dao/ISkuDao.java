package com.vvd.infrastructure.dao;

import com.vvd.infrastructure.dao.po.Sku;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/11 14:33
 */
@Mapper
public interface ISkuDao {

    Sku querySkuByGoodsId(String goodsId);

}
