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
public class BeanCommand implements Command, Comparable<BeanCommand> {

    @Getter
    private final Object bean;
    @Getter
    private final Method method;
    @Getter
    private final String name;

    @Getter
    private final String prefix;

    @Getter
    private final HashMap<String, XMLBean.ArgumentBean> arguments = new HashMap<>();

    @Getter
    @Setter
    private String description = null;

    @Getter
    @Setter
    private XMLBean.Empty empty;

    public BeanCommand(Object bean, Method method, String name, String prefix, XMLBean.ArgumentBean[] arguments) {
        this.bean = bean;
        this.method = method;
        this.name = name;
        this.prefix = prefix;
        this.method.setAccessible(true);
        if (arguments != null){

            for (XMLBean.ArgumentBean argument : arguments) {
                this.arguments.put(argument.getName(), argument);
            }

        }
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

    @Override
    public int compareTo(BeanCommand o) {
        return this.name.compareTo(o.name);
    }
}
