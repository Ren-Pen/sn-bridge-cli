package com.slimenano.framework.commands;

import com.slimenano.framework.CMDManager;
import com.slimenano.sdk.commands.BeanCommand;
import com.slimenano.sdk.commands.Command;
import com.slimenano.sdk.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.InstanceAlias;
import com.slimenano.sdk.framework.annotations.Mount;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * 帮助命令
 */
@SystemInstance
@InstanceAlias(alias = "command:help")
public class HelpCommand implements Command {

    @Mount
    private CMDManager manager;

    @Override
    public boolean exec(HashMap<String, String> args) throws Exception {

        // 打印内部指令
        BeanCommand[] innerCommands = manager.innerCommand.values().toArray(new BeanCommand[0]);
        System.out.println(ansi().a("[").fgBrightCyan().a("内置指令").reset().a("]"));
        print(innerCommands);
        manager.pluginMapping.forEach(((s, list) -> {
            BeanCommand[] commands = manager.pluginCommand.entrySet().stream()
                    .filter(entry -> list.contains(entry.getKey()))
                    .map(Map.Entry::getValue).distinct()
                    .toArray(BeanCommand[]::new);
            if (commands.length != 0) {
                System.out.println(ansi()
                                .a("[").fgBrightCyan().a("插件指令").reset().a("][")
                                .fgBrightBlue().a(commands[0].getPrefix()).reset().a("][")
                                .fgBrightMagenta().a(s).reset().a("]"));
                print(commands);
            }
        }));
        return true;
    }

    private void print(BeanCommand[] commands) {
        // 按照名字排序
        Arrays.sort(commands);
        for (BeanCommand c : commands) {
            printSingle(c);
        }
        System.out.println();
    }

    private void printSingle(BeanCommand command) {
        System.out.printf("  |- " + command.getEmpty().getCommandFormat() + "%n", command.getName(), command.getDescription() == null ? "" : command.getDescription());
        command.getArguments().values().forEach((argument) -> {
            System.out.printf("  |     |- " +
                            (argument.isRequired() ? "*" : " ") +
                            argument.getEmpty().getArgumentFormat() + "%n",
                    argument.getSimplify() == null ? "*" : argument.getSimplify(),
                    argument.getName(),
                    "",
                    argument.getDescription() == null ? "" : argument.getDescription()
            );

            if (argument.getIncludes() != null) {
                for (String include : argument.getIncludes()) {
                    System.out.printf(ansi().a("  |     |     |- [").fgBrightGreen().a("依赖").reset().a("][--").fgBrightGreen().a("%s").reset().a("]%n").toString(), include);
                }
            }

            if (argument.getExcludes() != null) {
                for (String exclude : argument.getExcludes()) {
                    System.out.printf(ansi().a("  |     |     |- [").fgBrightRed().a("冲突").reset().a("][--").fgBrightRed().a("%s").reset().a("]%n").toString(), exclude);
                }
            }

            if (argument.getExcludes() != null || argument.getIncludes() != null) {
                System.out.println("  |     |");
            }

        });
    }

}
