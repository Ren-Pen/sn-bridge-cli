package com.slimenano.framework.commands;

import com.slimenano.sdk.commands.Command;
import lombok.extern.slf4j.Slf4j;
import com.slimenano.framework.plugin.PluginManager;
import com.slimenano.sdk.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.InstanceAlias;
import com.slimenano.sdk.framework.annotations.Mount;
import com.slimenano.sdk.logger.Marker;

import java.util.HashMap;

@SystemInstance
@InstanceAlias(alias = "command:plugin")
@Slf4j
@Marker("指令:插件")
public class PluginCommand implements Command {

    @Mount
    private PluginManager manager;

    @Override
    public boolean exec(HashMap<String, String> args) throws Exception {

        if (args.containsKey("load")){
            manager.load(args.get("load"));
            return true;
        }
        else if (args.containsKey("unload")){
            manager.unload(args.get("unload"));
            return true;
        }


        return false;
    }
}
