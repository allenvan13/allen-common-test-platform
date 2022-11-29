package com.allen.testplatform.common.handler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

/**
 *  自动转换 按 , 分割的数据  SQL取出时自动转换成List<T>
 * @since 2022/3/14 10:46
 */
public class MySplitHandler implements TypeHandler<Object> {
    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        // 不能作为参数进行查询
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException {
        String string = rs.getString(columnName);
        if(string==null){
            return Collections.emptyList();
        }
        return Arrays.asList(string.split(","));
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
        String string = rs.getString(columnIndex);
        if(string==null){
            return Collections.emptyList();
        }
        return Arrays.asList(string.split(","));
    }

    @Override
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String string = cs.getString(columnIndex);
        if(string==null){
            return Collections.emptyList();
        }
        return Arrays.asList(string.split(","));
    }
}
