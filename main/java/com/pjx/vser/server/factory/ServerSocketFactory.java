package com.pjx.vser.server.factory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * ServerSocket工厂，根据不同的协议生成相应不同的ServerSocket工厂
 */
public interface ServerSocketFactory {

    ServerSocket createSocket(int port) throws IOException;

    ServerSocket createSocket(int port, int backlog) throws IOException;

    ServerSocket createSocket(int port, int backlog, InetAddress inetAddress) throws IOException;
}
