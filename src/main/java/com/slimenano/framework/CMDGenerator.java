package com.slimenano.framework;

import org.w3c.dom.Document;
import com.slimenano.framework.commons.ClassUtils;
import com.slimenano.framework.commons.XMLReader;
import com.slimenano.sdk.commands.BeanCommand;
import com.slimenano.sdk.commands.XMLBean;
import com.slimenano.sdk.framework.Context;
import com.slimenano.sdk.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.Mount;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * 读入xml生成BeanCommand
 */
@SystemInstance
public class CMDGenerator {

    @Mount
    private Context context;

    public List<BeanCommand> generateInner() throws Exception {
        Document commands = XMLReader.parse(SNRobotCLIBridge.class.getClassLoader().getResourceAsStream("console.xml"));

        Object o = XMLReader.deepE2M(commands.getDocumentElement());
        if (!(o instanceof HashMap)) {
            throw new ClassCastException("Invalid XML Format");
        }
        return generate((HashMap<String, Object>) o, context);

    }

    public List<BeanCommand> generate(HashMap<String, Object> o, Context context) throws Exception {

        XMLBean xmlBean = XMLReader.EMO2Bean(o, XMLBean.class);

        LinkedList<BeanCommand> result = new LinkedList<>();

        for (XMLBean.CommandBean commandBean : xmlBean.getCommand()) {
            Object bean;
            Method method;
            if (commandBean.getBean().startsWith("name:")) {
                bean = context.getBean(commandBean.getBean().substring("name:".length()));
            } else {
                bean = context.getBean(context.getBeanClassLoader().loadClass(commandBean.getBean().substring("classpath:".length())));
            }
            method = bean.getClass().getDeclaredMethod(commandBean.getMethod(), HashMap.class);
            BeanCommand command = new BeanCommand(bean, method, commandBean.getName(), xmlBean.getPrefix());
            if (commandBean.getArguments() != null) {

                for (XMLBean.ArgumentBean argument : commandBean.getArguments()) {

                    String arg_name = argument.getName();
                    command.getArguments().put(arg_name, argument.getSimplify());
                    if (argument.getSimplify() != null){
                        command.getSimple_arguments().put(argument.getSimplify(), arg_name);
                    }
                    command.getArguments_description().put(arg_name, argument.getDescription());
                    if (argument.getExcludes() != null) {
                        for (String exclude : argument.getExcludes()) {
                            if (!command.getExclude().containsKey(arg_name)) {
                                command.getExclude().put(arg_name, new HashSet<>());
                            }
                            command.getExclude().get(arg_name).add(exclude);
                        }
                    }

                }

            }
            result.add(command);
        }


        return result;
    }

    /*public List<BeanCommand> generate(Context context, HashMap<String, Object> map) throws Exception {

        Object command = map.get("command");
        LinkedList<BeanCommand> result = new LinkedList<>();
        if ((command instanceof HashMap)) {
            command = map2List((HashMap<String, Object>) command);
        }
        if (command instanceof List) {
            LinkedList<HashMap<String, Object>> list = (LinkedList<HashMap<String, Object>>) command;
            for (HashMap<String, Object> cmap : list) {
                String bean = (String) cmap.get("bean");
                String method = (String) cmap.get("method");
                String description = (String) cmap.get("description");
                String name = (String) cmap.get("name");
                Object bean1;
                Method method1;
                if (bean.startsWith("name:")) {
                    bean1 = context.getBean(bean.substring("name:".length()));
                } else {
                    bean1 = context.getBean(context.getBeanClassLoader().loadClass(bean.substring("classpath:".length())));
                }
                method1 = bean1.getClass().getDeclaredMethod(method, HashMap.class);

                BeanCommand e = new BeanCommand(bean1, method1, name, (String) map.get("prefix"));
                e.setDescription(description);

                Object argObject = cmap.get("arguments");
                if (argObject instanceof HashMap) {
                    argObject = map2List((HashMap<String, Object>) argObject);
                }
                if (argObject instanceof String) {
                    argObject = o2List(argObject, String.class);
                }
                LinkedList<Object> args = (LinkedList<Object>) argObject;
                if (args != null) {
                    for (Object oarg : args) {
                        if (oarg instanceof HashMap) {
                            HashMap<String, Object> arg = (HashMap<String, Object>) oarg;
                            String name1 = (String) arg.get("name");
                            String simplify = (String) arg.get("simplify");
                            e.getArguments().put(name1, simplify);
                            if (simplify != null)
                                e.getSimple_arguments().put(simplify, name1);
                            e.getArguments_description().put((String) arg.get("name"), (String) arg.get("description"));
                            LinkedList<String> excludes = o2List(arg.get("excludes"), String.class);
                            for (String exclude : excludes) {
                                if (e.getExclude().containsKey(name1)) {
                                    e.getExclude().put(name1, new HashSet<>());
                                }
                                e.getExclude().get(name1).add(exclude);
                            }
                        }else{
                            e.getArguments().put((String) oarg, null);
                        }
                    }
                }

                result.add(e);
            }
        }

        return result;
    }

    private LinkedList<HashMap<String, Object>> map2List(HashMap<String, Object> command) {
        LinkedList<HashMap<String, Object>> list = new LinkedList<>();
        list.add(command);
        return list;
    }

    private <T> LinkedList<T> o2List(Object o, Class<T> clazz) {
        if (o instanceof HashMap) {
            return (LinkedList<T>) map2List((HashMap<String, Object>) o);
        } else if (o instanceof String) {
            LinkedList<T> list = new LinkedList<>();
            list.add((T) o);
            return list;
        }
        return (LinkedList<T>) o;
    }*/




}
