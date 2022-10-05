package com.slimenano.framework;

import com.slimenano.sdk.commands.BeanCommand;
import com.slimenano.sdk.commands.Command;
import lombok.extern.slf4j.Slf4j;
import com.slimenano.sdk.framework.InitializationBean;
import com.slimenano.sdk.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.Mount;
import com.slimenano.sdk.framework.exception.BeanRepeatNameException;
import com.slimenano.sdk.logger.Marker;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;


@SystemInstance
@Slf4j
@Marker("命令管理器")
public class CMDManager implements InitializationBean {

    /**
     * 内置命令
     */
    public final HashMap<String, BeanCommand> innerCommand = new HashMap<>(32);
    /**
     * 插件命令，使用prefix@command输入，如果出现冲突，则将发出警告并使用插件path作为前缀
     * :prefix.xxxxxx
     */
    public final HashMap<String, BeanCommand> pluginCommand = new HashMap<>(32);

    /**
     * 插件映射
     * 插件类名 &lt;-&gt; 命令
     */
    public final HashMap<String, List<String>> pluginMapping = new HashMap<>(32);
    @Mount
    private CMDGenerator generator;

    /**
     * 执行指令
     *
     * @param line
     */
    public void exec(String line) {
        Matcher matcher = Command.cmdMatcher.matcher(line);
        if (!matcher.matches()) {
            log.warn("错误的指令格式，请检查您的输入！ 输入：{}", line);
            return;
        }
        String system = matcher.group("system");
        String prefix = matcher.group("prefix");
        String plugin = matcher.group("plugin");
        BeanCommand command;
        if (system != null) {
            if (!innerCommand.containsKey(system)) {
                log.warn("没有找到指令 {} 请检查您的输入是否正确！", system);
                return;
            }
            command = innerCommand.get(system);

        } else if (plugin != null && prefix != null) {
            String key = prefix + "." + plugin;
            if (!pluginCommand.containsKey(key)) {
                log.warn("没有找到插件指令 {} 请检查您的输入是否正确！", plugin);
                return;
            }
            command = innerCommand.get(key);
        } else {
            log.warn("错误的命令格式，请检查您的输入！ 输入：{}", line);
            return;
        }

        HashMap<String, String> args = new HashMap<>();

        // 解析输入参数
        Matcher argM = Command.argMatcher.matcher(line);
        while (argM.find()) {
            String name = argM.group("name");
            String arg = argM.group("arg");
            if (arg != null && arg.startsWith("\"") && arg.endsWith("\"")) {
                arg = arg.substring(1, arg.length() - 1);
            }
            args.put(name, arg);
        }

        Matcher argSimpleM = Command.simplifyArgMatcher.matcher(line);
        while (argSimpleM.find()) {
            String name = argSimpleM.group("name");
            if (!command.getSimple_arguments().containsKey(name)) {
                log.warn("遇到了未知的简化参数：-{}", name);
                continue;
            }
            name = command.getSimple_arguments().get(name);

            String arg = argSimpleM.group("arg");
            if (arg != null && arg.startsWith("\"") && arg.endsWith("\"")) {
                arg = arg.substring(1, arg.length() - 1);
            }

            args.put(name, arg);
        }

        for (String an : args.keySet()) {
            if(command.getExclude().containsKey(an)) {
                for (String ean : args.keySet()) {
                    if (command.getExclude().get(an).contains(ean)) {
                        if (an.equals(ean)) {
                            log.warn("无效的排除参数！参数：{} 排除：{} 输入：{}", an, ean, line);
                        } else {
                            log.warn("参数 {} 不允许与参数 {} 同时出现！输入：{}", an, ean, line);
                            return;
                        }
                    }

                }
            }
        }

        try {
            boolean result = command.exec(args);
            if (!result) {
                log.warn("命令执行失败，请检查日志！ 输入：{}", line);
            }
        } catch (Exception e) {
            log.error("命令执行过程中出现了异常！输入：{}", line, e);
        }


    }

    @Override
    public void onLoad() throws Exception {
        List<BeanCommand> beanCommands = generator.generateInner();
        for (BeanCommand beanCommand : beanCommands) {
            String name = beanCommand.getName();
            if (innerCommand.containsKey(name)) {
                throw new BeanRepeatNameException("重复的内置指令名，请检查您的配置");
            }
            innerCommand.put(name, beanCommand);
        }

    }

    @Override
    public void onDestroy() throws Exception {

    }
}
