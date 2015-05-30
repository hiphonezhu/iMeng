package com.android.imeng.framework.volley;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.android.imeng.framework.log.Logger;
import com.android.imeng.framework.logic.ILogic;
import com.android.imeng.framework.logic.InfoResult;
import com.android.imeng.framework.volley.InfoResultRequest.ResponseParserListener;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.plus.multipart.MultiPartRequest;
import com.android.volley.toolbox.HttpHeaderParser;
/**
 * 重写MultiPartRequest实现InfoResult结果类型的网络请求, 做了以下扩展
 * [
 *   1、提供设置请求头header
 * ]
 * @author hiphonezhu@gmail.com
 * @version [Android-BaseLine, 2014-9-18]
 */
public class InfoResultMultiPartRequest extends MultiPartRequest<InfoResult> implements Listener<InfoResult>
{
    /** request identifier*/
    private int requestId;
    /** request headers*/
    private Map<String, String> headers;
    /** 回调业务层做解析Bean封装*/
    private ResponseParserListener parserListener;
    /** 分发解析好的数据到业务层*/
    private ILogic logic;
    
    public InfoResultMultiPartRequest(final int requestId, String url, int method, ResponseParserListener parseListener, final ILogic logic)
    {
        super(method, url, null, new ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Logger.w("InfoResultMultiPartRequest", error);
                logic.onResult(requestId, error);
            }
        });
        this.parserListener = parseListener;
        this.requestId = requestId;
        this.logic = logic;
        RetryPolicy retryPolicy = new DefaultRetryPolicy(20000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        setRetryPolicy(retryPolicy);
    }
    
    @Override
    public void onResponse(InfoResult response)
    {
        logic.onResult(requestId, response);
    }
    
    /**
     * 设置头信息
     * @param headers
     */
    public void setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
    }
    
    /**
     * 添加头信息
     * @param headers
     */
    public void addHeaders(Map<String, String> headers)
    {
        if (this.headers == null || headers == null)
        {
            setHeaders(headers);
        }
        else
        {
            this.headers.putAll(headers);
        }
    }
    
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError
    {
        if (headers != null)
        {
            return headers;
        }
        else
        {
            return super.getHeaders();
        }
    }
    @Override
    protected Response<InfoResult> parseNetworkResponse(NetworkResponse response)
    {
        try
        {
            String str = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            InfoResult infoResult = parserListener.doParse(str);
            if (infoResult == null) // 解析失败
            {
                return Response.error(new VolleyError("parse response error >>> " + str));
            }
            else // 解析成功
            {
                return Response.success(infoResult, HttpHeaderParser.parseCacheHeaders(response));
            }
        }
        catch (UnsupportedEncodingException e)
        {
            return Response.error(new VolleyError("UnsupportedEncodingException"));
        }
        catch (Exception e)
        {
            return Response.error(new VolleyError("Exception is >>> " + e.getMessage()));
        }  
    }

    @Override
    protected void deliverResponse(InfoResult response)
    {
        this.onResponse(response);
    }
}
