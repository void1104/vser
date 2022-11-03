package com.pjx.vser.server.http.processor;

import com.pjx.vser.common.exception.LifecycleException;
import com.pjx.vser.server.Lifecycle;

import java.net.Socket;

public class HttpProcessor implements Lifecycle, Runnable {


    /**
     * 核心方法,将available变量(类似信号量)置为有效，告诉核心run()方法可以执行
     *
     * @param socket TCP socket, 从HttpConnector模块获得
     */
    public synchronized void assign(Socket socket) {

    }

    /**
     * 核心方法，阻塞当前线程，直到当前processor可以进行下一次处理时，返回TCP socket
     *
     * @return 可用的TCP socket
     */
    private synchronized Socket await() {
        return null;
    }


    @Override
    public void start() throws LifecycleException {

    }

    @Override
    public void stop() throws LifecycleException {

    }

    @Override
    public void run() {

    }
}
