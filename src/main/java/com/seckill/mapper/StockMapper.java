package com.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.entity.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface StockMapper extends BaseMapper<Stock> {

    @Select("SELECT * FROM t_stock WHERE product_id = #{productId}")
    Stock selectByProductId(@Param("productId") Long productId);

    /** 乐观锁原子扣减;返回受影响行数,0表示版本冲突 */
    @Update("UPDATE t_stock SET available = available - #{cnt}, version = version + 1 " +
            "WHERE product_id = #{productId} AND version = #{version} AND available >= #{cnt}")
    int deductByOptimisticLock(@Param("productId") Long productId,
                               @Param("cnt") Integer cnt,
                               @Param("version") Integer version);
}
