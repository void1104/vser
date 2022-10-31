package com.pjx.vser.server.connector;

import com.pjx.vser.common.constant.ServerConst;
import com.pjx.vser.common.exception.LifecycleException;
import com.pjx.vser.server.Connector;
import com.pjx.vser.server.Lifecycle;

import java.net.ServerSocket;

public class HttpConnector implements Connector, Lifecycle, Runnable {

    /**
     * 服务端监听socket
     */
    private ServerSocket serverSocket;

    /**
     * 监听http服务的端口
     */
    private int port = 8080;

    /**
     * 最小process数
     */
    protected int minProcessors = 5;

    /**
     * 最大process数
     */
    protected int maxProcessors = 20;

    /**
     * 启动标志
     */
    private boolean started = false;

    /**
     * 停止标志
     */
    private boolean stopped = false;

    /**
     * 后台线程名
     */
    private String threadName;

    /**
     * 后台线程
     */
    private Thread thread;


    public void run() {

    }

    public void start() throws LifecycleException {

        // validate and update our current state
        if (started)
            throw new LifecycleException(ServerConst.get("httpConnector.alreadyStarted"));
        this.threadName = "HttpConnector[" + port + "]";
        // todo lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

    }

    public void stop() throws LifecycleException {

    }
}
