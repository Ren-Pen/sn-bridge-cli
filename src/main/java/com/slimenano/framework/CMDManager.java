package com.slimenano.framework;

import com.slimenano.framework.event.impl.plugin.PluginLoadedEvent;
import com.slimenano.framework.event.impl.plugin.PluginUnloadedEvent;
import com.slimenano.sdk.commands.BeanCommand;
import com.slimenano.sdk.commands.Command;
import com.slimenano.sdk.commands.XMLBean;
import com.slimenano.sdk.event.annotations.EventListener;
import com.slimenano.sdk.event.annotations.Subscribe;
import com.slimenano.sdk.framework.InitializationBean;
import com.slimenano.sdk.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.Mount;
import com.slimenano.sdk.framework.exception.BeanRepeatNameException;
import com.slimenano.sdk.logger.Marker;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import static com.slimenano.sdk.framework.ui.GUI_CONST.YES_NO;


@SystemInstance
@Slf4j
@Marker("命令管理器")
@EventListener
public class CMDManager implements InitializationBean, Completer {

    /**
     * 内置命令
     */
    public final HashMap<String, BeanCommand> innerCommand = new HashMap<>(32);
    /**
     * 插件命令，使用prefix@command输入，如果出现冲突，则将发出警告并使用插件path作为前缀
     * :prefix.xxxxxx
     */
    public final ConcurrentHashMap<String, BeanCommand> pluginCommand = new ConcurrentHashMap<>(32);
    /**
     * 插件映射
     * 插件命令的载入不会影响插件的载入情况，如果载入过程中出现异常则可能导致插件的部分功能失效
     * 插件类名 &lt;-&gt; 命令
     */
    public final ConcurrentHashMap<String, List<String>> pluginMapping = new ConcurrentHashMap<>(32);
    private final StringsCompleter yes_no = new StringsCompleter("yes", "no");
    private final StringsCompleter ok_cancel = new StringsCompleter("ok", "cancel");
    public volatile int confirmMode = 0;
    private StringsCompleter stringsCompleter = new StringsCompleter();
    @Mount
    private CMDGenerator generator;

    /**
     * 执行指令
     *
     * @param line
     */
    public void exec(String line) {
        log.debug("指令输入：{}", line);
        Matcher matcher = Command.cmdMatcher.matcher(line);
        if (!matcher.matches()) {
            log.warn("错误的指令格式，请检查您的输入！ 输入：{}", line);
            return;
        }
        String system = matcher.group("system");
        String prefix = matcher.group("prefix");
        String plugin = matcher.group("plugin");
        BeanCommand command;
        // 判断指令正确性
        if (system != null) {
            system = system.toLowerCase();
            if (!innerCommand.containsKey(system)) {
                log.warn("没有找到指令 {} 请检查您的输入是否正确！", system);
                return;
            }
            command = innerCommand.get(system);

        } else if (plugin != null && prefix != null) {
            plugin = plugin.toLowerCase();
            prefix = prefix.toLowerCase();
            String key = prefix + "@" + plugin;
            if (!pluginCommand.containsKey(key)) {
                log.warn("没有找到插件 {} 的指令 {} 请检查您的输入是否正确！", prefix, plugin);
                return;
            }
            command = pluginCommand.get(key);
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
            if (!command.getArguments().containsKey(name)) {
                log.warn("遇到了未知的参数：--{}", name);
            }
            args.put(name, arg);
        }

        Matcher argSimpleM = Command.simplifyArgMatcher.matcher(line);
        while (argSimpleM.find()) {
            String name = argSimpleM.group("name");
            boolean find = false;
            for (XMLBean.ArgumentBean argument : command.getArguments().values()) {
                if (name.equals(argument.getSimplify())) {
                    name = argument.getName();
                    find = true;
                    break;
                }
            }
            if (!find) {
                log.warn("遇到了未知的简化参数：-{}", name);
                return;
            }

            String arg = argSimpleM.group("arg");
            if (arg != null && arg.startsWith("\"") && arg.endsWith("\"")) {
                arg = arg.substring(1, arg.length() - 1);
            }

            args.put(name, arg);
        }

        if (command.getEmpty() == XMLBean.Empty.FORCE && !args.isEmpty()) {
            log.warn("命令不接受任何参数！输入：{}", line);
            return;
        }

        if (command.getEmpty() == XMLBean.Empty.FALSE && args.isEmpty()) {
            log.warn("命令至少需要一个参数！输入：{}", line);
            return;
        }


        // 必要参数
        if (command.getArguments().values().stream().anyMatch(arg -> arg.isRequired() && !args.containsKey(arg.getName()))) {
            log.warn("命令缺少必要参数！请使用帮助命令查询用法！ 输入：{}", line);
            return;
        }

        // 依赖
        if (!args.keySet().stream()
                .allMatch(arg -> {
                            String[] includes = command.getArguments().get(arg).getIncludes();
                            if (includes == null) return true;
                            return Arrays.stream(includes)
                                    .allMatch(s -> {
                                                if (!args.containsKey(s)) {
                                                    log.warn("参数 {} 缺少依赖参数！ 依赖于：{} 输入：{}", arg, s, line);
                                                    return false;
                                                }
                                                return true;
                                            }
                                    );
                        }
                )
        ) {
            return;
        }

        // 排斥
        if (!args.keySet().stream()
                .allMatch(arg -> {
                            String[] excludes = command.getArguments().get(arg).getExcludes();
                            if (excludes == null) return true;
                            return Arrays.stream(excludes)
                                    .noneMatch(s -> {
                                                if (!args.containsKey(s)) {
                                                    return false;
                                                }
                                                if (s.equals(arg)) {
                                                    log.warn("无效的排除参数！参数：{} 排除：{} 输入：{}", arg, s, line);
                                                    return false;
                                                }
                                                log.warn("参数 {} 不允许与参数 {} 同时使用！输入：{}", arg, s, line);
                                                return true;
                                            }
                                    );
                        }
                )
        ) {
            return;
        }


        // 是否允许空
        if (!args.entrySet().stream().allMatch(entry -> {

            XMLBean.Empty empty = command.getArguments().get(entry.getKey()).getEmpty();
            if (empty == XMLBean.Empty.FORCE && (entry.getValue() != null && !entry.getValue().isEmpty())) {
                log.warn("参数 {} 不接受任何值！输入：{}", entry.getKey(), line);
                return false;
            }
            if (empty == XMLBean.Empty.FALSE && (entry.getValue() == null || entry.getValue().isEmpty())) {
                log.warn("参数 {} 必须拥有一个值！输入：{}", entry.getKey(), line);
                return false;
            }
            return true;

        })) {
            return;
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

    @Subscribe
    public void onPluginUnLoad(PluginUnloadedEvent event) {
        String path = event.getPayload().getInformation().getPath();
        if (pluginMapping.containsKey(path)) {
            log.debug("即将卸载插件命令");
            for (String s : pluginMapping.get(path)) {
                if (pluginCommand.containsKey(s)) {

                    if (!pluginCommand.get(s).getBean().getClass().getClassLoader().equals(event.getPayload().getPluginLoader())) {
                        log.warn("命令注册在插件中但不属于该插件，命令不会移除！命令：{}", s);
                    } else {
                        log.debug("移除命令！命令：{}", s);
                        pluginCommand.remove(s);
                    }
                }
            }
            pluginMapping.remove(path);
            update();
        }
    }

    @Subscribe
    public void onPluginLoaded(PluginLoadedEvent event) {
        String path = event.getPayload().getInformation().getPath();
        HashMap<String, Object> extension = event.getPayload().getInformation().getExtension();
        if (extension.containsKey("console")) {
            pluginMapping.put(path, new LinkedList<>());
            log.debug("插件拥有命令扩展，即将加载插件命令");
            Object o = extension.get("console");
            if (!(o instanceof HashMap)) {
                throw new ClassCastException("Invalid XML Format");
            }
            try {
                List<BeanCommand> commands = generator.generate((HashMap<String, Object>) o, event.getPayload().getContext());
                for (BeanCommand command : commands) {
                    String name = command.getPrefix() + "@" + command.getName();
                    pluginMapping.get(path).add(name);
                    if (pluginCommand.containsKey(name)) {
                        log.warn("{} 同前缀命令已被注册，原始命令被覆盖！ 源：{} 命令：{}", command.getBean(), pluginCommand.get(name).getBean(), name);
                    }
                    pluginCommand.put(name, command);
                }

            } catch (Exception e) {
                log.warn("加载插件命令扩展时出现错误，插件功能可能无法完全正常运行！", e);
            }
            update();
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
        update();

    }

    private void update() {

        ArrayList<String> list = new ArrayList<>(innerCommand.size() + pluginCommand.size());
        list.addAll(innerCommand.keySet());
        list.addAll(pluginCommand.keySet());
        stringsCompleter = new StringsCompleter(list);
    }

    @Override
    public void onDestroy() throws Exception {
    }

    @Override
    public void complete(LineReader reader, ParsedLine parsedLine, List<Candidate> candidates) {
        if (confirmMode == 0) {
            StringsCompleter argCompleter;

            String line = parsedLine.line();
            Matcher matcher = Command.cmdMatcher.matcher(line);
            if (!matcher.matches()) {
                argCompleter = new StringsCompleter();
                stringsCompleter.complete(reader, parsedLine, candidates);
            } else {
                String system = matcher.group("system");
                String prefix = matcher.group("prefix");
                String plugin = matcher.group("plugin");
                if (system != null) {
                    argCompleter = parseCMDParameters(innerCommand.containsKey(system), innerCommand.get(system), system);
                } else if (prefix != null && plugin != null) {
                    String name = prefix + "@" + plugin;
                    argCompleter = parseCMDParameters(pluginCommand.containsKey(name), pluginCommand.get(name), name);
                } else {
                    argCompleter = new StringsCompleter();
                }
            }


            argCompleter.complete(reader, parsedLine, candidates);
        } else {
            if (confirmMode == YES_NO) {
                yes_no.complete(reader, parsedLine, candidates);
            } else {
                ok_cancel.complete(reader, parsedLine, candidates);
            }
        }
    }

    private StringsCompleter parseCMDParameters(boolean b, BeanCommand command, String system) {
        StringsCompleter argCompleter;
        if (!b) {
            argCompleter = new StringsCompleter();
        } else {
            HashMap<String, XMLBean.ArgumentBean> arguments = command.getArguments();
            ArrayList<String> list = new ArrayList<>(arguments.size());
            for (XMLBean.ArgumentBean bean : arguments.values()) {
                String s = bean.getEmpty() == XMLBean.Empty.FALSE ? "=" : "";
                list.add("--" + bean.getName() + s);
                if (bean.getSimplify() != null) {
                    list.add("-" + bean.getSimplify() + s);
                }
            }
            argCompleter = new StringsCompleter(list);
        }
        return argCompleter;
    }
}
