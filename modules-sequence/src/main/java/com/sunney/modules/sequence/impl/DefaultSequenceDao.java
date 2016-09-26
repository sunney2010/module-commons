/*
 * Copyright 1999-2024 Colotnet.com All right reserved. This software is the confidential and proprietary information of
 * Colotnet.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Colotnet.com.
 */
package com.sunney.modules.sequence.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunney.modules.sequence.SequenceDao;
import com.sunney.modules.sequence.SequenceRange;
import com.sunney.modules.sequence.exception.SequenceException;
import com.sunney.modules.sequence.lifecycle.AbstractLifecycle;

/**
 * 序列DAO默认实现，JDBC方式
 * 
 * @author Sunney 2016年4月15日 下午9:38:49
 */

public class DefaultSequenceDao extends AbstractLifecycle implements SequenceDao {

    private static final Logger logger                           = LoggerFactory.getLogger(DefaultSequenceDao.class);
    private static final int    MIN_STEP                         = 1;
    private static final int    MAX_STEP                         = 100000;
    private static final int    DEFAULT_STEP                     = 1000;
    private static final int    DEFAULT_RETRY_TIMES              = 150;

    private static final String DEFAULT_TABLE_NAME               = "sequence";
    private static final String DEFAULT_NAME_COLUMN_NAME         = "name";
    private static final String DEFAULT_VALUE_COLUMN_NAME        = "value";
    private static final String DEFAULT_STEP_COLUMN_NAME         = "step";
    private static final String DEFAULT_RETRY_COLUMN_NAME        = "retryTimes";
    private static final String DEFAULT_GMT_MODIFIED_COLUMN_NAME = "gmt_modified";

    private static final long   DELTA                            = 100000000L;

    private DataSource          dataSource;

    /**
     * 重试次数
     */
    private int                 retryTimes                       = DEFAULT_RETRY_TIMES;

    /**
     * 步长
     */
    private int                 step                             = DEFAULT_STEP;

    /**
     * 序列所在的表名
     */
    private String              tableName                        = DEFAULT_TABLE_NAME;

    /**
     * 存储序列名称的列名
     */
    private String              nameColumnName                   = DEFAULT_NAME_COLUMN_NAME;

    /**
     * 存储序列值的列名
     */
    private String              valueColumnName                  = DEFAULT_VALUE_COLUMN_NAME;

    /**
     * 存储序列值的列名
     */
    private String              stepColumnName                   = DEFAULT_STEP_COLUMN_NAME;

    /**
     * 存储序列值的列名
     */
    private String              retryTimesColumnName             = DEFAULT_RETRY_COLUMN_NAME;

    /**
     * 存储序列最后更新时间的列名
     */
    private String              gmtModifiedColumnName            = DEFAULT_GMT_MODIFIED_COLUMN_NAME;
    // 配置信息，存成字符串
    protected String            configStr                        = "";

    private volatile String     selectSql;
    private volatile String     updateSql;
    private volatile String     insertSql;

    public SequenceRange nextRange(String name) throws SequenceException {
        if (name == null) {
            throw new IllegalArgumentException("序列名称不能为空");
        }

        long oldValue;
        long newValue;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        for (int i = 0; i < retryTimes + 1; ++i) {
            try {
                conn = dataSource.getConnection();
                stmt = conn.prepareStatement(getSelectSql());
                stmt.setString(1, name);
                rs = stmt.executeQuery();
                if (rs == null || !rs.next()) {
                    boolean val = insertSql(conn, stmt, name);
                    if (val) {
                        continue;
                    }
                }
                oldValue = rs.getLong(1);
                step=rs.getInt(2);
                if (oldValue < 0) {
                    StringBuilder message = new StringBuilder();
                    message.append("Sequence value cannot be less than zero, value = ").append(oldValue);
                    message.append(", please check table ").append(getTableName());

                    throw new SequenceException(message.toString());
                }

                if (oldValue > Long.MAX_VALUE - DELTA) {
                    StringBuilder message = new StringBuilder();
                    message.append("Sequence value overflow, value = ").append(oldValue);
                    message.append(", please check table ").append(getTableName());

                    throw new SequenceException(message.toString());
                }

                newValue = oldValue + getStep();
            } catch (SQLException e) {
                throw new SequenceException(e);
            } finally {
                closeResultSet(rs);
                rs = null;
                closeStatement(stmt);
                stmt = null;
                closeConnection(conn);
                conn = null;
            }

            try {
                conn = dataSource.getConnection();
                stmt = conn.prepareStatement(getUpdateSql());
                stmt.setLong(1, newValue);
                stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                stmt.setString(3, name);
                stmt.setLong(4, oldValue);
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    // retry
                    continue;
                }

                return new SequenceRange(oldValue + 1, newValue);
            } catch (SQLException e) {
                throw new SequenceException(e);
            } finally {
                closeStatement(stmt);
                stmt = null;
                closeConnection(conn);
                conn = null;
            }
        }

        throw new SequenceException("Retried too many times, retryTimes = " + retryTimes);
    }

    private String getSelectSql() {
        if (selectSql == null) {
            synchronized (this) {
                if (selectSql == null) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("select ").append(getValueColumnName()).append(",").append(getStepColumnName());
                    buffer.append(" from ").append(getTableName());
                    buffer.append(" where ").append(getNameColumnName()).append(" = ?");

                    selectSql = buffer.toString();
                }
            }
        }

        return selectSql;
    }

    private String getUpdateSql() {
        if (updateSql == null) {
            synchronized (this) {
                if (updateSql == null) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("update ").append(getTableName());
                    buffer.append(" set ").append(getValueColumnName()).append(" = ?, ");
                    buffer.append(getGmtModifiedColumnName()).append(" = ? where ");
                    buffer.append(getNameColumnName()).append(" = ? and ");
                    buffer.append(getValueColumnName()).append(" = ?");

                    updateSql = buffer.toString();
                }
            }
        }

        return updateSql;
    }

    private boolean insertSql(Connection conn, PreparedStatement stmt, String name) throws SequenceException {
        boolean val = false;
        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(getInsertSql());
            stmt.setString(1, name);
            stmt.setLong(2, 1);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setLong(4, step);
            stmt.setLong(5, retryTimes);
            stmt.executeUpdate();
            val = true;
        } catch (SQLException e) {
            val = false;
            throw new SequenceException(e);
        } finally {
            closeStatement(stmt);
            stmt = null;
            closeConnection(conn);
            conn = null;
        }
        return val;
    }

    /**
     * 当没有记录集时可以执行插入操作
     * 
     * @return
     */
    private String getInsertSql() {
        if (insertSql == null) {
            synchronized (this) {
                if (insertSql == null) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("insert into ").append(getTableName());
                    buffer.append("( ");
                    buffer.append(getNameColumnName()).append(",");
                    buffer.append(getValueColumnName()).append(",");
                    buffer.append(getGmtModifiedColumnName()).append(",");
                    buffer.append(getStepColumnName()).append(",");
                    buffer.append(getRetryTimesColumnName());
                    buffer.append(")");
                    buffer.append(" VALUES(?, ? ,?,?,?)");
                    insertSql = buffer.toString();
                }
            }
        }

        return insertSql;
    }

    private static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.debug("Could not close JDBC ResultSet", e);
            } catch (Throwable e) {
                logger.debug("Unexpected exception on closing JDBC ResultSet", e);
            }
        }
    }

    private static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.debug("Could not close JDBC Statement", e);
            } catch (Throwable e) {
                logger.debug("Unexpected exception on closing JDBC Statement", e);
            }
        }
    }

    private static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.debug("Could not close JDBC Connection", e);
            } catch (Throwable e) {
                logger.debug("Unexpected exception on closing JDBC Connection", e);
            }
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        if (retryTimes < 0) {
            throw new IllegalArgumentException("Property retryTimes cannot be less than zero, retryTimes = "
                                               + retryTimes);
        }

        this.retryTimes = retryTimes;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        if (step < MIN_STEP || step > MAX_STEP) {
            StringBuilder message = new StringBuilder();
            message.append("Property step out of range [").append(MIN_STEP);
            message.append(",").append(MAX_STEP).append("], step = ").append(step);

            throw new IllegalArgumentException(message.toString());
        }

        this.step = step;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getNameColumnName() {
        return nameColumnName;
    }

    public void setNameColumnName(String nameColumnName) {
        this.nameColumnName = nameColumnName;
    }

    public String getValueColumnName() {
        return valueColumnName;
    }

    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    public String getGmtModifiedColumnName() {
        return gmtModifiedColumnName;
    }

    public void setGmtModifiedColumnName(String gmtModifiedColumnName) {
        this.gmtModifiedColumnName = gmtModifiedColumnName;
    }

    /**
     * @return the stepColumnName
     */
    public String getStepColumnName() {
        return stepColumnName;
    }

    /**
     * @param stepColumnName the stepColumnName to set
     */
    public void setStepColumnName(String stepColumnName) {
        this.stepColumnName = stepColumnName;
    }

    /**
     * @return the retryTimesColumnName
     */
    public String getRetryTimesColumnName() {
        return retryTimesColumnName;
    }

    /**
     * @param retryTimesColumnName the retryTimesColumnName to set
     */
    public void setRetryTimesColumnName(String retryTimesColumnName) {
        this.retryTimesColumnName = retryTimesColumnName;
    }

    public String getConfigStr() {
        if (StringUtils.isEmpty(configStr)) {
            String format = "[type:simple] [step:{0}] [retryTimes:{1}] [tableInfo:{2}({3},{4},{5})]";
            this.configStr = MessageFormat.format(format, String.valueOf(step), String.valueOf(retryTimes), tableName,
                                                  nameColumnName, valueColumnName, gmtModifiedColumnName);
        }
        return configStr;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }
}
