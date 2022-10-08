package com.slimenano.framework.commands;

import com.slimenano.framework.config.RobotConfiguration;
import com.slimenano.framework.core.BaseRobot;
import com.slimenano.sdk.core.Robot;
import com.slimenano.sdk.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.InstanceAlias;
import com.slimenano.sdk.framework.annotations.Mount;
import com.slimenano.sdk.logger.Marker;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@SystemInstance
@InstanceAlias(alias = "command:bot")
@Slf4j
@Marker("指令:机器人控制")
public class BotCommand {

    @Mount
    private BaseRobot robot;

    @Mount
    private RobotConfiguration configuration;

    public boolean login(HashMap<String, String> args){

        if (!robot.isClose()){
            log.warn("当前程序已登录，请使用 logout 退出登录后再重新登录！");
            return false;
        }

        if (args.containsKey("account")){
            configuration.setAccount(Long.parseLong(args.get("account")));
        }
        if (args.containsKey("password")){
            configuration.setPassword(args.get("password"));
        }
        if (args.containsKey("protocol")){
            configuration.setProtocol(args.get("protocol"));
        }
        if (args.containsKey("save")) {
            configuration.save();
        }
        robot.login();
        return true;
    }

    public boolean logout(HashMap<String, String> args) throws Exception {
        if (robot.isClose()){
            log.warn("没有已登录的机器人");
            return false;
        }
        robot.close();
        return true;
    }

}
