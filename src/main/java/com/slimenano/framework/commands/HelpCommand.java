package com.slimenano.framework.commands;

import com.slimenano.sdk.commands.Command;
import com.slimenano.sdk.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.Instance;
import com.slimenano.sdk.framework.annotations.InstanceAlias;

import java.util.HashMap;

/**
 * 帮助命令
 */
@SystemInstance
@InstanceAlias(alias = "command:help")
public class HelpCommand implements Command {

    @Override
    public boolean exec(HashMap<String, String> args) throws Exception {
        boolean deal = false;
        if (args.containsKey("debug")){
            deal = true;
            System.out.println("调试参数：" + args.get("debug"));
        }
        if (args.containsKey("page")){
            throw new Exception("没有这一页！" + args.get("page"));
        }
        return deal;
    }

}
