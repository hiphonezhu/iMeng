package com.android.imeng.framework.logic.parser;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.android.imeng.framework.logic.InfoResult;
import com.android.imeng.framework.volley.ResponseParserListenerAdpater;
/**
 * A abstract class that parse logic status with type json
 * @author hiphonezhu@gmail.com
 * @version [Android-BaseLine, 2014-9-28]
 */
public abstract class JsonParser extends ResponseParserListenerAdpater
{
    /** 附加数据*/
    private Map<String, Object> extras = new HashMap<String, Object>();
    /**
     * 解析服务器结果的状态信息(业务成功与失败, 对应错误码和描述信息等)
     * @param response
     * @return InfoResult
     * @throws JSONException, Exception
     */
    public InfoResult doParse(final String response) throws JSONException
    {
        JSONObject jsonObject = JSON.parseObject(response);
        InfoResult infoResult = new InfoResult.Builder()
                   .success(jsonObject.getBooleanValue("success"))
                   .errorCode(jsonObject.getString("errorCode"))
                   .desc(jsonObject.getString("msg"))
                   .build();
        parseResponse(infoResult, jsonObject);
        return infoResult;
    }
    
    public JsonParser putExtra(String key, Object value)
    {
        extras.put(key, value);
        return this;
    }
    
    protected Object getExtra(String key)
    {
        return extras.get(key);
    }
    
    /**
     * 解析业务数据
     * @param infoResult
     * @param jsonObject
     */
    protected abstract void parseResponse(final InfoResult infoResult, final JSONObject jsonObject);
}
