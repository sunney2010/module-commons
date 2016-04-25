/*
 * Copyright 1999-2024 Colotnet.com All right reserved. This software is the confidential and proprietary information of
 * Colotnet.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Colotnet.com.
 */
package com.sunney.modules.sequence.lifecycle;

/**
 * 类AbstractLifecycle.java的实现描述：TODO 类实现描述
 * 
 * @author Sunney 2016年4月15日 下午9:46:02
 */
public class AbstractLifecycle implements Lifecycle {

    protected final Object     lock     = new Object();
    protected volatile boolean isInited = false;

    public void init() throws Exception {
        synchronized (lock) {
            if (isInited()) {
                return;
            }

            try {
                doInit();
                isInited = true;
            } catch (Exception e) {
                // 出现异常调用destroy方法，释放
                try {
                    doDestroy();
                } catch (Exception e1) {
                    // ignore
                }
                throw new Exception(e);
            }
        }
    }

    public void destroy() throws Exception {
        synchronized (lock) {
            if (!isInited()) {
                return;
            }

            doDestroy();
            isInited = false;
        }
    }

    public boolean isInited() {
        return isInited;
    }

    protected void doInit() throws Exception {
    }

    protected void doDestroy() throws Exception {
    }
}
