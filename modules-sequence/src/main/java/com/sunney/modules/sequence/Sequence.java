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
 * 类Sequence.java的实现描述：TODO 类实现描述 
 * @author Sunney 2016年4月15日 下午9:28:25
 */
public interface Sequence {

    /**
     * 取得序列下一个值
     * 
     * @return 返回序列下一个值
     * @throws SequenceException
     */
    long nextValue() throws SequenceException;
    
    /**
     * 取得序列下一个值
     * @param SequenceName 名字
     * @return 返回序列下一个值
     * @throws SequenceException
     */
    long nextValue(String SequenceName) throws SequenceException;

    /**
     * 返回size大小后的值，比如针对batch拿到size大小的值，自己内存中顺序分配
     * 
     * @param size
     * @return
     * @throws SequenceException
     */
    long nextValue(int size) throws SequenceException;

    /**
     * 消耗掉当前内存中已分片的sequence
     */
    public boolean exhaustValue() throws SequenceException;
}

