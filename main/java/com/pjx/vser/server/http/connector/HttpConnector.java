package com.pjx.vser.server.http.connector;

import com.pjx.vser.common.constant.NetConst;
import com.pjx.vser.common.exception.LifecycleException;
import com.pjx.vser.server.Connector;
import com.pjx.vser.server.Lifecycle;
import com.pjx.vser.server.factory.DefaultServerSocketFactory;
import com.pjx.vser.server.factory.ServerSocketFactory;
import com.pjx.vser.server.http.processor.HttpProcessor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class HttpConnector implements Connector, Lifecycle, Runnable {

    /**
     * 服务端监听socket
     */
    private ServerSocket serverSocket;

    /**
     * 服务端绑定ip地址
     */
    private String address;

    /**
     * Connector可承受的连接数量
     */
    private int acceptCount = 10;

    /**
     * 监听http服务的端口
     */
    private int port = 8080;

    /**
     * 连接超时时间, 为0表示不设值超时
     */
    private int connectionTimeout = NetConst.DEFAULT_CONNECTION_TIMEOUT;

    /**
     * 输入缓冲区的大小/(byte)
     */
    private int bufferSize = 2048;

    /**
     * processors池,用于存放空闲的processor
     */
    private final Deque<HttpProcessor> processors = new ArrayDeque<>();

    /**
     * 存换已经创建的processor
     */
    private List<HttpProcessor> created = new ArrayList<>();

    /**
     * ServerSocket工厂
     */
    private ServerSocketFactory socketFactory;

    /**
     * 当前process数量
     */
    protected int curProcessors = 0;

    /**
     * 最小process数
     */
    protected int minProcessors = 5;

    /**
     * 最大process数
     */
    protected int maxProcessors = 20;

    /**
     * 初始化标志
     */
    private boolean initialized = false;

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

    public void setAddress(String address) {
        this.address = address;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * 同步锁object
     */
    private final Object threadSync = new Object();

    public void initialize() throws LifecycleException, IOException {
        if (initialized)
            throw new LifecycleException("httpConnector.alreadyInitialized");
        this.initialized = true;

        serverSocket = open();
    }

    private ServerSocket open() throws IOException {

        ServerSocketFactory factory = getFactory();

        if (address == null) {
            return factory.createSocket(port, acceptCount);
        }

        // 如果从本地能找到相应的地址，则绑定该地址为服务地址
        InetAddress[] addresses = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
        for (InetAddress inetAddress : addresses) {
            if (inetAddress.getHostAddress().equals(address)) {
                return factory.createSocket(port, acceptCount, inetAddress);
            }
        }
        return factory.createSocket(port, acceptCount);
    }


    private ServerSocketFactory getFactory() {
        if (this.socketFactory == null) {
            synchronized (this) {
                this.socketFactory = new DefaultServerSocketFactory();
            }
        }
        return (this.socketFactory);
    }

    /**
     * 启动后台核心连接处理线程
     */
    private void threadStart() {
        System.out.println("httpConnector.starting");

        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 停止后台核心连接处理线程
     */
    private void threadStop() {
        System.out.println("httpConnector.ending");

        stopped = true;
        // 等待后台线程把正在执行的请求处理完毕
        try {
            threadSync.wait(5000);
        } catch (InterruptedException e) {

        }
        thread = null;
    }

    /**
     * TODO 生成一个新的processor
     */
    private HttpProcessor newProcessor() {

        HttpProcessor processor = new HttpProcessor(this);
        try {
            processor.start();
        } catch (Exception e) {
            System.out.println("createProcessor" + e);
        }
        created.add(processor);
        return processor;
    }

    /**
     * 从缓存池获取一个processors,如果池子没有则创建一个(除非数量已达上限)
     */
    private HttpProcessor createProcessor() {
        synchronized (processors) {
            if (processors.size() > 0) {
                return processors.pop();
            }
            if ((maxProcessors > 0) && (curProcessors < maxProcessors)) {
                return newProcessor();
            }
            if (maxProcessors <= 0) {
                return newProcessor();
            }
            return null;
        }
    }

    /**
     * 回收processor至缓冲池
     */
    private void recycle(HttpProcessor processor) {
        processors.push(processor);
    }


    public void run() {
        // 循环直到收到shutdown命令
        while (!stopped) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                if (connectionTimeout > 0) {
                    socket.setSoTimeout(connectionTimeout);
                }
            } catch (Exception e) {

            }

            HttpProcessor processor = createProcessor();
            // 拿不到processor，说明没有空闲的processor了，比较粗暴的忽略当前请求
            if (processor == null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    ;
                }
                continue;
            }
            processor.assign(socket);
        }

        // 唤醒在threadStop()方法中因wait()而睡眠的线程
        synchronized (threadSync) {
            threadSync.notifyAll();
        }
    }

    public void start() throws LifecycleException {

        // 检验启动标志并更新
        if (started)
            throw new LifecycleException("httpConnector.alreadyStarted");
        this.threadName = "HttpConnector[" + port + "]";
        // TODO lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        // 启动后台线程
        threadStart();

        // 初始化processors缓存池
        while (curProcessors < minProcessors) {
            if ((maxProcessors > 0) && (curProcessors >= maxProcessors))
                break;
            HttpProcessor processor = newProcessor();
            recycle(processor);
        }
    }

    public void stop() throws LifecycleException {

        if (!started)
            throw new LifecycleException("httpConnector.alreadyEnd");
        // TODO lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // 优雅关闭所有已创建的processors
        for (int i = 0; i < created.size(); i++) {
            HttpProcessor processor = created.get(i);
            if (processor != null) {
                try {
                    processor.stop();
                } catch (LifecycleException e) {
                    System.out.println("HttpConnector.stop" + e);
                }
            }
        }

        synchronized (threadSync) {
            // 关闭正在使用的serverSocket
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {

                }
                // 停止正在运行的后台线程
                threadStop();
            }
        }
        serverSocket = null;
    }
}
