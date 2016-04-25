/*
 * Copyright 1999-2024 Colotnet.com All right reserved. This software is the
 * confidential and proprietary information of Colotnet.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Colotnet.com.
 */
package com.sunney.modules.sequence.exception;

import java.sql.SQLException;


/**
 * 类SequenceException.java的实现描述：TODO 类实现描述 
 * @author Sunney 2016年4月15日 下午9:24:51
 */
public class SequenceException extends SQLException{

    /**
     * 
     */
    private static final long serialVersionUID = -1680891881363614530L;

    public SequenceException(String params){
        super( params);
    }


    public SequenceException(Throwable cause){
        super(cause);
    }
}
