package com.pjx.vser.server;

/**
 * @author pengjiaxin3
 * @description
 * @date 11/2/22 4:50 PM
 *
 * JVM常用的三种类加载器, 从上到下是父子继承关系, 使用双亲委派机制实现类的加载
 * - 引导类载入器(bootstrap class loader)
 * - 拓展类载入器(extension class loader)
 * - 系统类载入器(system class loader)
 * 双亲委派机制的目的是为了避免用户自定义的类影响关键的核心区域.
 */
public interface Loader {
}
