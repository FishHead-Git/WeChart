package com.yujutg.upload.utils;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class Base64TypeHandle<T> extends BaseTypeHandler<T> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, T t, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, PasswordEncoderUtils.encoder((String) t));
    }

    @Override
    public T getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String res = resultSet.getString(s);
        return res!=null&&!"".equals(res)?(T) PasswordEncoderUtils.decoderStr(res):(T) res;
    }

    @Override
    public T getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String res = resultSet.getString(i);
        return res!=null&&!"".equals(res)?(T) PasswordEncoderUtils.decoderStr(res):(T) res;
    }

    @Override
    public T getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String res = callableStatement.getString(i);
        return res!=null&&!"".equals(res)?(T) PasswordEncoderUtils.decoderStr(res):(T) res;
    }
}
