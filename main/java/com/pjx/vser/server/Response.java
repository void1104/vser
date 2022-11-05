package com.pjx.vser.server;

import java.io.OutputStream;

public interface Response {

    /**
     * 设置相应的请求体
     */
    void setRequest(Request request);

    /**
     * 设置输出流
     */
    void setStream(OutputStream outputStream);
}
