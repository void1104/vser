package com.pjx.vser.server;


/**
 * @author pengjiaxin3
 * @description
 * @date 11/1/22 11:23 AM
 *
 * Tomcat的Servlet分级
 * - Engine:  表示整个Catalina servlet引擎
 * - Host:    表示包含有一个或多个Context容器的虚拟主机
 * - Context: 表示一个Web应用程序,一个Context可以有多个Wrapper
 * - Wrapper: 表示一个独立的servlet
 */
public interface Contained {
}
