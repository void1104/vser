package com.pjx.vser.server;

import com.pjx.vser.common.exception.LifecycleException;

import java.io.IOException;

/**
 * 连接器interface
 */
public interface Connector {

    /**
     * 初始化函数
     *
     * @throws LifecycleException -if this server was already initailized.
     */
    void initialize() throws LifecycleException, IOException;
}
