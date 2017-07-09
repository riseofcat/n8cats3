package com.n8cats.lib_gwt.data;

import com.n8cats.lib_gwt.Const;
import com.n8cats.lib_gwt.JsonBasic;

import java.util.Date;

public class SmallTask extends JsonBasic {

    public Const.Id.SmallTask id;
    public Const.SmallTaskStatus status;
    public Const.Id.BigTask bigTaskId;
    public Const.Id.Account accountId;
    public Rate rate;
    public Date startTime;

}
