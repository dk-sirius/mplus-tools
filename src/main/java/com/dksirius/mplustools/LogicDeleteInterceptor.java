package com.dksirius.mplustools;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.plugin.Interceptor;

import java.util.List;

/**
 * @Author wuxiaofeng
 * <p>
 * 配置优先级为: 字面量>定义对象>Sql解析
 * <p>
 * 1) 字面量处理低于2ms；
 * 2）定义对象处理大于3ms；
 * 3）Sql解析处理低于33ms；
 * <p>
 * 改拦截器处理为Sql 的Prepare下的 statement。
 */
public interface LogicDeleteInterceptor extends Interceptor {

    String LogicDelCondition = "%s=%s";

    /**
     * 包含逻辑删除定义的对象,可以为null
     *
     * @return
     */
    default Class<?> logicDeleteClass() {
        return null;
    }

    /**
     * 逻辑删除更新表达式，可以返回null
     *
     * @return
     */
    default String logicDeletedValueExpr() {
        return "f_deleted=1";
    }

    /**
     * 逻辑删除更新目标表达，可以返回null
     *
     * @return
     */
    default String logicDeleteReplaceValueExpr() {
        return String.format("f_deleted=%s", System.currentTimeMillis());
    }

    default TableFieldInfo loadTableFieldInfo(String sql) throws JSQLParserException {
        if (StrUtil.isNotEmpty(sql)) {
            TableInfo info;
            if (logicDeleteClass() != null) {
                // 还在数据表基类的方式会比较节省时间
                info = TableInfoHelper.getTableInfo(logicDeleteClass());
                if (info != null && info.isWithLogicDelete()) {
                    return info.getLogicDeleteFieldInfo();
                }
            } else {
                TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
                List<String> tableList = tablesNamesFinder.getTableList(CCJSqlParserUtil.parse(sql));
                if (tableList != null && tableList.size() > 0) {
                    info = TableInfoHelper.getTableInfo(tableList.get(0));
                    if (info != null && info.isWithLogicDelete()) {
                        return info.getLogicDeleteFieldInfo();
                    }
                }
            }
        }
        return null;
    }

    default String switcherLogicDeleteValue(String sql) throws JSQLParserException {
        if (StrUtil.isNotEmpty(logicDeletedValueExpr())) {
            // 存在默认配置正则
            if (sql.contains(logicDeletedValueExpr())) {
                return sql.replace(logicDeletedValueExpr(), logicDeleteReplaceValueExpr());
            }
        } else {
            TableFieldInfo fieldInfo = loadTableFieldInfo(sql);
            if (fieldInfo != null) {
                String sourceSegment = String.format(LogicDelCondition, fieldInfo.getColumn(), fieldInfo.getLogicDeleteValue());
                if (sql.contains(sourceSegment)) {
                    String targetSegment = String.format(LogicDelCondition, fieldInfo.getColumn(), System.currentTimeMillis());
                    sql = sql.replace(sourceSegment, targetSegment);
                    return sql;
                }
            }
        }
        return sql;
    }

}
