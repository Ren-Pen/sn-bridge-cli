package com.slimenano.framework.commands;

import com.slimenano.framework.RobotApplication;
import com.slimenano.sdk.commands.Command;
import com.slimenano.nscan.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.InstanceAlias;
import com.slimenano.sdk.logger.Marker;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

/**
 * 退出命令
 */
@SystemInstance
@InstanceAlias(alias = "command:exit")
public class ExitCommand implements Command {


    @Override
    public boolean exec(HashMap<String, String> args) throws Exception {
        RobotApplication.stop();
        return true;
    }
}
