package com.dksirius.mplustools;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * @author wuxiaofeng
 * @date 2022/8/30 13:21
 */
@Component
@Slf4j
public class MyBatisPlusBaseHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("insert time");
        this.setFieldValByName("version", 1, metaObject);
        //兼容迁移数据的时间
        if (ObjectUtil.isEmpty(metaObject.getValue("createdAt"))) {
            this.setFieldValByName("createdAt", OffsetDateTime.now(), metaObject);
        }
        if (ObjectUtil.isEmpty(metaObject.getValue("updatedAt"))) {
            this.setFieldValByName("updatedAt", OffsetDateTime.now(), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updatedAt", OffsetDateTime.now(), metaObject);
    }
}
