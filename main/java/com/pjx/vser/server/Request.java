package com.pjx.vser.server;

import java.io.InputStream;

public interface Request {

    /**
     * 设置关联的response
     */
    void setResponse(Response response);

    /**
     * 设置输入流
     */
    void setStream(InputStream stream);

    /**
     * 设置http method
     */
    void setMethod(String method);

    /**
     * get method
     */
    String getMethod(String method);

    /**
     * 设置协议版本
     */
    void setProtocol(String protocol);

    /**
     * get protocol
     */
    String getProtocol(String protocol);

    /**
     * 设置 uri
     */
    void setRequestURI(String uri);

    /**
     * get uri
     */
    String getRequestURI(String uri);
}
