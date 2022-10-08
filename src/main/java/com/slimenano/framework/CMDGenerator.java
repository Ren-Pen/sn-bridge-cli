package com.slimenano.framework;

import com.slimenano.framework.commons.XMLReader;
import com.slimenano.sdk.commands.BeanCommand;
import com.slimenano.sdk.commands.XMLBean;
import com.slimenano.sdk.framework.Context;
import com.slimenano.sdk.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.Mount;
import com.slimenano.sdk.logger.Marker;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * 读入xml生成BeanCommand
 */
@SystemInstance
@Slf4j
@Marker("指令生成器")
public class CMDGenerator {

    @Mount
    private Context context;

    public List<BeanCommand> generateInner() throws Exception {
        log.debug("准备加载内置指令...");
        Document commands = XMLReader.parse(SNRobotCLIBridge.class.getClassLoader().getResourceAsStream("console.xml"));
        log.debug("内置指令文档已加载");
        Object o = XMLReader.deepE2M(commands.getDocumentElement());
        if (!(o instanceof HashMap)) {
            throw new ClassCastException("Invalid XML Format");
        }
        return generate((HashMap<String, Object>) o, context);

    }

    public List<BeanCommand> generate(HashMap<String, Object> o, Context context) throws Exception {
        log.debug("开始解析指令文档");
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
            log.debug("加载到指令对象！对象：{} 方法：{}", bean.getClass(), method);
            BeanCommand command = new BeanCommand(bean, method, commandBean.getName().toLowerCase(), xmlBean.getPrefix().toLowerCase(), commandBean.getArguments());
            command.setEmpty(commandBean.getEmpty());
            command.setDescription(commandBean.getDescription());
            result.add(command);
        }


        return result;
    }


}
