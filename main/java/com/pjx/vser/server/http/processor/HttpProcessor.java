package com.pjx.vser.server.http.processor;

import com.pjx.vser.common.constant.Globals;
import com.pjx.vser.common.exception.LifecycleException;
import com.pjx.vser.server.Lifecycle;
import com.pjx.vser.server.http.HttpRequest;
import com.pjx.vser.server.http.HttpRequestImpl;
import com.pjx.vser.server.http.HttpResponse;
import com.pjx.vser.server.http.HttpResponseImpl;
import com.pjx.vser.server.http.connector.HttpConnector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class HttpProcessor implements Lifecycle, Runnable {

    /**
     * 关联的连接器
     */
    private HttpConnector connector = null;

    /**
     * 关联的Socket
     */
    private Socket socket = null;

    /**
     * HTTP请求体，后续传给对应的container
     */
    private HttpRequestImpl request = null;

    /**
     * HTTP响应体，后续传给对应的container
     */
    private HttpResponseImpl response = null;

    /**
     * 后台线程
     */
    private Thread thread = null;

    /**
     * 后台线程的名称
     */
    private String threadName = null;

    /**
     * 后台线程启动标志
     */
    private boolean started = false;

    /**
     * 后台线程停止标志
     */
    private boolean stopped = false;

    /**
     * 类似信号量的作用,用于判断当前process是否空闲
     */
    private boolean available = false;

    /**
     * 同步锁
     */
    private Object threadSync;

    /**
     * 代理服务器的接口 TODO 还不知道有什么用
     */
    private int proxyPort = 0;

    /**
     * 连接通常使用的端口
     */
    private int serverPort = 0;

    private static final String match = ";" + Globals.SESSION_PARAMETER_NAME + "=";


    public HttpProcessor(HttpConnector connector) {

        super();
        this.connector = connector;
    }

    /**
     * 核心函数：进行http解析并调用对应的容器处理业务逻辑
     * <p>
     * Tomcat的设计将Request的解析和Response的填充放到processor中，保证只有在需要的时候才解析
     *
     * @param socket The socket on which we are connected to the client.
     */
    public void process(Socket socket) {
        boolean ok = true;
        InputStream input = null;
        OutputStream output = null;

        try {
            input = new BufferedInputStream(socket.getInputStream(), connector.getBufferSize());
            output = socket.getOutputStream();

            request.setStream(input);
            request.setResponse(response);
            response.setStream(output);
            response.setRequest(request);
            ((HttpServletResponse) response).setHeader("Server", "vser server");
        } catch (Exception e) {
            System.out.println("HttpProcessor.process:" + e);
            ok = false;
        }

        // 1.解析Http请求.
        try {
            if (ok) {
                parseConnection(socket);
                parseRequest(input);
                if (!request.getProtocol().startsWith("HTTP/0"))
                    parseHeaders(input);
            }
        } catch (Exception e) {
            try {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (Exception f) {

            }
        }

        // 2.调用对应的Container去处理请求.

        // 3.填充Http响应体并执行response相应的资源回收.

        // 4.执行request的资源回收.

        // 5.执行socket的资源回收.
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("FIXME-Exception closing socket" + e);
        }
        socket = null;
    }

    /**
     * Parse and record the connection parameters related to this request.
     *
     * @param socket The socket on which we are connected
     */
    private void parseConnection(Socket socket) {
        request.setInet(socket.getInetAddress());
        if (proxyPort != 0)
            request.setServerPort(proxyPort);
        else
            request.setServerPort(serverPort);
        request.setSocket(socket);
    }

    /**
     * 解析请求体
     */
    private void parseRequest(InputStream input) throws IOException, ServletException {

        // 解析HTTP请求行的第一行
        String line = read(input);
        if (line == null)
            throw new ServletException("httpProcessor.parseRequest.read.exception!!!");
        StringTokenizer st = new StringTokenizer(line);
        // 获得http method
        String method = null;
        try {
            method = st.nextToken();
        } catch (NoSuchElementException e) {
            method = null;
        }

        // 1.获得http uri
        String uri = null;
        try {
            uri = st.nextToken();
        } catch (NoSuchElementException e) {
            uri = null;
        }

        // 2.解析获得http协议
        String protocol = null;
        try {
            protocol = st.nextToken();
        } catch (NoSuchElementException e) {
            protocol = "HTTP/0.9";
        }

        // 3.检验请求行
        if (method == null) {
            throw new ServletException("httpProcessor.parseRequest.method.exception!!!");
        }
        if (uri == null) {
            throw new ServletException("httpProcessor.parseRequest.uri.exception!!!");
        }

        // 解析get方式下的参数
        int question = uri.indexOf('?');
        if (question >= 0) {
            request.setQueryString(uri.substring(question + 1));
            uri = uri.substring(0, question);
        } else {
            request.setQueryString(null);
        }

        // 解析获得session ID
        int semicolon = uri.indexOf(match);
        if (semicolon >= 0) {
            String rest = uri.substring(semicolon + match.length());
            int semicolon2 = rest.indexOf(';');
            if (semicolon2 >= 0) {
                request.setRequestedSessionId(rest.substring(0, semicolon2));
                rest = rest.substring(semicolon2);
            } else {
                request.setRequestedSessionId(rest);
                rest = "";
            }
            request.setRequestedSessionURL(true);
            uri = uri.substring(0, semicolon) + rest;
        } else {
            request.setRequestedSessionId(null);
            request.setRequestedSessionURL(false);
        }

        // Set the corresponding request properties
        request.setMethod(method);
        request.setProtocol(protocol);
        request.setRequestURI(uri);
        request.setSecure(false);       // No SSL support
        request.setScheme("http");      // No SSL support
    }

    /**
     * 按行读取输入流
     */
    private String read(InputStream input) throws IOException {

        StringBuilder sb = new StringBuilder();
        while (true) {
            int ch = input.read();
            if (ch < 0) {
                if (sb.length() == 0) {
                    return (null);
                } else {
                    break;
                }
            } else if (ch == '\r') {
                continue;
            } else if (ch == '\n') {
                break;
            }
            sb.append((char) ch);
        }
        return sb.toString();
    }


    /**
     * 核心方法,将available变量(类似信号量)置为有效，告诉核心run()方法可以执行
     *
     * @param socket TCP socket, 从HttpConnector模块获得
     */
    public synchronized void assign(Socket socket) {

        while (available) {
            try {
                wait();
            } catch (Exception e) {

            }
        }

        this.socket = socket;
        available = true;
        notifyAll();
    }

    /**
     * 核心方法，阻塞当前线程，直到当前processor可以进行下一次处理时，返回TCP socket
     *
     * @return 可用的TCP socket
     */
    private synchronized Socket await() {

        while (!available) {
            try {
                wait();
            } catch (Exception e) {

            }
        }

        // 使用一个本地变量保存，避免notifyAll()之后，成员变量的socket已被重新赋值.
        Socket socket = this.socket;
        available = false;
        notifyAll();

        return socket;
    }

    private void threadStart() {
        System.out.println("HttpProcessor.thread." + threadName + ".start");
        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();
    }

    private void threadStop() {
        stopped = true;
        assign(null);
        // 睡眠5s，等于processor处理完所有请求
        synchronized (threadSync) {
            try {
                threadSync.wait(5000);
            } catch (InterruptedException e) {
                ;
            }
        }
        thread = null;
    }


    @Override
    public void start() throws LifecycleException {
        if (started) {
            throw new LifecycleException("httpProcess.alreadyStart!");
        }
        // TODO lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        threadStart();
    }

    @Override
    public void stop() throws LifecycleException {
        if (!started) {
            throw new LifecycleException("httpProcess.notStarted!");
        }
        // TODO lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        threadStop();
    }

    @Override
    public void run() {

        while (!stopped) {
            // 阻塞等待httpConnector调用assign传来一个新的请求
            Socket socket = await();
            if (socket == null) {
                continue;
            }

            process(socket);

            // TODO 完成生命周期模块
            // request.recycle();
            // response.recycle();
            // connector.recycle(this);
        }

        synchronized (threadSync) {
            threadSync.notifyAll();
        }
    }
}
