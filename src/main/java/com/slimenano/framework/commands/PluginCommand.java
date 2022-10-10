package com.slimenano.framework.commands;

import com.slimenano.framework.plugin.PluginManager;
import com.slimenano.framework.plugin.PluginMeta;
import com.slimenano.sdk.access.Permission;
import com.slimenano.sdk.commands.Command;
import com.slimenano.sdk.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.InstanceAlias;
import com.slimenano.sdk.framework.annotations.Mount;
import com.slimenano.sdk.framework.ui.GUI_CONST;
import com.slimenano.sdk.framework.ui.IGUIBridge;
import com.slimenano.sdk.logger.Marker;
import com.slimenano.sdk.plugin.PluginInformation;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

import static org.fusesource.jansi.Ansi.ansi;

@SystemInstance
@InstanceAlias(alias = "command:plugin")
@Slf4j
@Marker("指令:插件")
public class PluginCommand implements Command {

    @Mount
    private PluginManager manager;

    @Mount
    private IGUIBridge bridge;

    @Override
    public boolean exec(HashMap<String, String> args) throws Exception {

        if (args.containsKey("load")) {
            manager.load(args.get("load"));
        } else if (args.containsKey("unload")) {
            manager.unload(args.get("unload"));
        } else {
            System.out.println("=======================================");
            System.out.println("=               插件列表               =");
            System.out.println("=======================================");
            System.out.println();
            if (manager.getPluginMap().size() == 0){
                System.out.println("（空列表）");
            }
            for (PluginMeta meta : manager.getPluginMap().values()) {
                PluginInformation information = meta.getInformation();
                bridge.alert("[" + ansi().fgBrightCyan().a(information.getName()).reset() + "]",
                        "  |- 插件类名：" + ansi().fgBrightMagenta().a(information.getPath()).reset() + "\n" +
                                "  |- 插件作者：" + information.getAuthor() + "\n" +
                                "  |- 插件版本：" + information.getVersion() + "\n" +
                                "  |- 插件详情：" + information.getDescription() + "\n" +
                                "  |- 插件权限：\n" +
                                Permission.toString("  |    |- [%s] %s%n", information.getPermissions())
                        , GUI_CONST.INFO);
            }
            System.out.println();
            System.out.println("=======================================");
        }


        return true;
    }
}
