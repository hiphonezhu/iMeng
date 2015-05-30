package com.android.imeng.framework.logic.parser;

import com.alibaba.fastjson.JSONObject;
import com.android.imeng.framework.logic.InfoResult;
/**
 * 通用操作解析器
 * @author hiphonezhu@gmail.com
 * @version [OApp, 2014-11-10]
 */
public class SimpleJsonParser extends JsonParser
{
    /** 需要回传的值*/
    private Object extraObj;
    public SimpleJsonParser(Object extraObj)
    {
        this.extraObj = extraObj;
    }
    
    public SimpleJsonParser()
    {
    }
    
    @Override
    public void parseResponse(InfoResult infoResult, JSONObject jsonObject)
    {
        infoResult.setExtraObj(extraObj);
    }
}
