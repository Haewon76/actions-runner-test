package com.cashmallow.api.infrastructure.security;

import com.cashmallow.api.application.SecurityService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.*;

@RequiredArgsConstructor
public class EncryptTypeHandler implements TypeHandler<String> {

    private SecurityService securityService;

    public EncryptTypeHandler(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {

        try {
            if (StringUtils.isNotEmpty(parameter)) {
                parameter = securityService.encryptAES256(parameter);
            }
            ps.setString(i, parameter);
        } catch (Exception e) {
            throw new SQLException("Failed to decrypt result");
        }
    }

    @Override
    public String getResult(ResultSet rs, String columnName) throws SQLException {

        String value = "";
        try {
            value = rs.getString(columnName);
            if (StringUtils.isNotEmpty(value)) {
                return securityService.decryptAES256(value);
            }
        } catch (Exception e) {
            throw new SQLException("Failed to decrypt result");
        }
        return value;
    }

    @Override
    public String getResult(ResultSet rs, int columnIndex) throws SQLException {

        String value = "";
        try {
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            String columnName = resultSetMetaData.getCatalogName(columnIndex);
            value = rs.getString(columnName);

            if (StringUtils.isNotEmpty(value)) {
                return securityService.decryptAES256(value);
            }
        } catch (Exception e) {
            throw new SQLException("Failed to decrypt result");
        }
        return value;
    }

    @Override
    public String getResult(CallableStatement cs, int columnIndex) throws SQLException {

        String value = "";
        try {
            ResultSetMetaData resultSetMetaData = cs.getMetaData();
            String columnName = resultSetMetaData.getCatalogName(columnIndex);
            value = cs.getString(columnName);

            if (StringUtils.isNotEmpty(value)) {
                return securityService.decryptAES256(value);
            }
        } catch (Exception e) {
            throw new SQLException("Failed to decrypt result");
        }
        return value;
    }


}
