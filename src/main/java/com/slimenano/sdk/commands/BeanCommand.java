package com.slimenano.sdk.commands;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

/**
 * XML的指令将会实例化成插件指令类
 */
public class BeanCommand implements Command {

    @Getter
    private final Object bean;
    @Getter
    private final Method method;
    @Getter
    private final String name;

    @Getter
    @Setter
    private final String prefix;

    @Getter
    @Setter
    private String description = null;

    @Getter
    private final HashMap<String, HashSet<String>> exclude = new HashMap<>();

    @Getter
    private final HashMap<String, String> simple_arguments = new HashMap<>();

    @Getter
    private final HashMap<String, String> arguments = new HashMap<>();

    @Getter
    private final HashMap<String, String> arguments_description = new HashMap<>();

    public BeanCommand(Object bean, Method method, String name, String prefix) {
        this.bean = bean;
        this.method = method;
        this.name = name;
        this.prefix = prefix;
        this.method.setAccessible(true);
    }

    @Override
    public boolean exec(HashMap<String, String> args) throws Exception {

        try {
            return (boolean) method.invoke(bean, args);
        } catch (IllegalAccessException ignore) {
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception)
                throw (Exception) e.getCause();
            else if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
        }

        return false;
    }

}
