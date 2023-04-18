package com.dksirius.mplustools;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Objects;

/**
 * @Author wuxiaofeng
 * <p>
 * mybatis-plus 逻辑删除拦截器
 */

@Slf4j
@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class IbatisPlusLogicDeletedInterceptor implements LogicDeleteInterceptor {

    private final static String MapStatementKey = "delegate.mappedStatement";

    private final static String BoundSqlKey = "sql";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler stat = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(stat);
        MappedStatement mappedstatement = (MappedStatement) metaObject.getValue(MapStatementKey);
        long start = System.currentTimeMillis();
        if (Objects.requireNonNull(mappedstatement.getSqlCommandType()) == SqlCommandType.UPDATE) {
            BoundSql boundSql = stat.getBoundSql();
            String sql = switcherLogicDeleteValue(boundSql.getSql());
            if (sql != null) {
                Field field = boundSql.getClass().getDeclaredField(BoundSqlKey);
                field.setAccessible(true);
                field.set(boundSql, sql);
            }
        }
        log.info(">>>>>>>>>>>>>>>>cost time = {} ms", (System.currentTimeMillis() - start));
        return invocation.proceed();
    }


}
