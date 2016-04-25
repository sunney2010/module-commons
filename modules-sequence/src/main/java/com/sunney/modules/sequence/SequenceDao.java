/*
 * Copyright 1999-2024 Colotnet.com All right reserved. This software is the
 * confidential and proprietary information of Colotnet.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Colotnet.com.
 */
package com.sunney.modules.sequence;

import com.sunney.modules.sequence.exception.SequenceException;

/**
 * 类SequenceDao.java的实现描述：TODO 类实现描述 
 * @author Sunney 2016年4月15日 下午9:32:28
 */
public interface SequenceDao {
    /**
     * 取得下一个可用的序列区间
     * 
     * @param name 序列名称
     * @return 返回下一个可用的序列区间
     * @throws SequenceException
     */
    SequenceRange nextRange(String name) throws SequenceException;

    /**
     * 获取当前的步长
     */
    int getStep();
}
