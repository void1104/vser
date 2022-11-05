package com.pjx.vser.server.http;

import java.net.InetAddress;
import java.net.Socket;

public class HttpRequestImpl extends HttpRequest {

    /**
     * The InetAddress of the remote client of ths request.
     */
    protected InetAddress inet = null;

    /**
     * The server port associated with this Request.
     */
    protected int serverPort = -1;

    /**
     * The query string for this request.
     */
    protected String queryString = null;

    /**
     * 请求对应socket
     */
    protected Socket socket = null;

    /**
     * The requested session ID (if any) for this request.
     */
    protected String requestedSessionId = null;

    /**
     * Was the requested session ID received in a URL?
     */
    protected boolean requestedSessionURL = false;

    /**
     * Was this request received on a secure channel?
     */
    protected boolean secure = false;

    /**
     * The scheme associated with this Request.
     */
    protected String scheme = null;

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setInet(InetAddress inet) {
        this.inet = inet;
    }

    public void setServerPort(int port) {
        this.serverPort = port;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setQueryString(String query) {
        this.queryString = query;
    }

    public void setRequestedSessionId(String id) {
        this.requestedSessionId = id;
    }

    public void setRequestedSessionURL(boolean flag) {
        this.requestedSessionURL = flag;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }


}
