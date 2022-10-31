package com.pjx.vser.server;

import com.pjx.vser.common.exception.LifecycleException;

public interface Lifecycle {

    /**
     * 启动，整个生命周期只调用一次
     */
    void start() throws LifecycleException;

    /**
     * 结束，整个生命周期只调用一次
     */
    void stop() throws LifecycleException;
}
