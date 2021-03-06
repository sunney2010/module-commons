/*
 * Copyright 1999-2024 Colotnet.com All right reserved. This software is the
 * confidential and proprietary information of Colotnet.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Colotnet.com.
 */
package com.sunney.modules.sequence.lifecycle;


/**
 * 类Lifecycle.java的实现描述：TODO 类实现描述 
 * @author Sunney 2016年4月15日 下午9:45:35
 */
public interface Lifecycle {
    /**
     * 正常启动
     */
    void init() throws Exception;

    /**
     * 正常停止
     */
    void destroy() throws Exception;

    /**
     * 是否存储运行运行状态
     * 
     * @return
     */
    boolean isInited() throws Exception;
}
