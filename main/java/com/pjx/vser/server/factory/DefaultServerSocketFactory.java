package com.pjx.vser.server.factory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class DefaultServerSocketFactory implements ServerSocketFactory {

    @Override
    public ServerSocket createSocket(int port) throws IOException {
        return new ServerSocket(port);
    }

    @Override
    public ServerSocket createSocket(int port, int backlog) throws IOException {
        return new ServerSocket(port, backlog);
    }

    @Override
    public ServerSocket createSocket(int port, int backlog, InetAddress inetAddress) throws IOException {
        return new ServerSocket(port, backlog, inetAddress);
    }
}
