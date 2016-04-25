/*
 * Copyright 1999-2024 Colotnet.com All right reserved. This software is the confidential and proprietary information of
 * Colotnet.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Colotnet.com.
 */
package com.sunney.modules.sequence.impl;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.sunney.modules.sequence.Sequence;
import com.sunney.modules.sequence.SequenceDao;
import com.sunney.modules.sequence.SequenceRange;
import com.sunney.modules.sequence.exception.SequenceException;

/**
 * 类DefaultSequence.java的实现描述：TODO 类实现描述
 * 
 * @author Sunney 2016年4月15日 下午9:29:17
 */
public class DefaultSequence implements Sequence {

    private final Lock             lock = new ReentrantLock();

    private SequenceDao            sequenceDao;

    /**
     * 默认的序列名称
     */
    private String                 name;

    private volatile SequenceRange currentRange;

    @Override
    public long nextValue(String SequenceName) throws SequenceException {

        if (currentRange == null) {
            lock.lock();
            try {
                if (currentRange == null) {
                    currentRange = sequenceDao.nextRange(SequenceName);
                }
            } finally {
                lock.unlock();
            }
        }

        long value = currentRange.getAndIncrement();
        if (value == -1) {
            lock.lock();
            try {
                for (;;) {
                    if (currentRange.isOver()) {
                        currentRange = sequenceDao.nextRange(SequenceName);
                    }

                    value = currentRange.getAndIncrement();
                    if (value == -1) {
                        continue;
                    }

                    break;
                }
            } finally {
                lock.unlock();
            }
        }

        if (value < 0) {
            throw new SequenceException("Sequence value overflow, value = " + value);
        }

        return value;
    }

    public long nextValue() throws SequenceException {
        return this.nextValue(name);
    }

    @Override
    public long nextValue(int size) throws SequenceException {
        if (size > this.getSequenceDao().getStep()) {
            throw new SequenceException("batch size > sequence step step, please change batch size or sequence inner step");
        }

        if (currentRange == null) {
            lock.lock();
            try {
                if (currentRange == null) {
                    currentRange = sequenceDao.nextRange(name);
                }
            } finally {
                lock.unlock();
            }
        }

        long value = currentRange.getBatch(size);
        if (value == -1) {
            lock.lock();
            try {
                for (;;) {
                    if (currentRange.isOver()) {
                        currentRange = sequenceDao.nextRange(name);
                    }

                    value = currentRange.getBatch(size);
                    if (value == -1) {
                        continue;
                    }

                    break;
                }
            } finally {
                lock.unlock();
            }
        }

        if (value < 0) {
            throw new SequenceException("Sequence value overflow, value = " + value);
        }

        return value;
    }

    @Override
    public boolean exhaustValue() throws SequenceException {
        lock.lock();
        try {
            if (currentRange != null) {
                // 强制设置为已用完
                currentRange.setOver(true);
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    public SequenceDao getSequenceDao() {
        return sequenceDao;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
