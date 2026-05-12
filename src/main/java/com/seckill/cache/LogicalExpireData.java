package com.seckill.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 逻辑过期包装: 物理上 Redis/Caffeine 不过期, 由 expireAt 判定是否需异步刷新.
 * 防止热点 Key 过期瞬间击穿到 DB.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogicalExpireData implements Serializable {
    private LocalDateTime expireAt;
    private Object data;

    public boolean isExpired() {
        return expireAt != null && LocalDateTime.now().isAfter(expireAt);
    }
}
