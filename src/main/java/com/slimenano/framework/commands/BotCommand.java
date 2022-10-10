package com.slimenano.framework.commands;

import com.slimenano.framework.config.RobotConfiguration;
import com.slimenano.framework.core.BaseRobot;
import com.slimenano.sdk.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.InstanceAlias;
import com.slimenano.sdk.framework.annotations.Mount;
import com.slimenano.sdk.logger.Marker;
import com.slimenano.sdk.robot.contact.SNContact;
import com.slimenano.sdk.robot.contact.user.SNStranger;
import com.slimenano.sdk.robot.messages.content.SNText;
import com.slimenano.sdk.robot.messages.meta.SNMessageSource;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;

import static org.fusesource.jansi.Ansi.ansi;

@SystemInstance
@InstanceAlias(alias = "command:bot")
@Slf4j
@Marker("指令:机器人控制")
public class BotCommand {

    @Mount
    private BaseRobot robot;

    @Mount
    private RobotConfiguration configuration;


    public boolean login(HashMap<String, String> args) {

        if (!robot.isClose()) {
            log.warn("当前程序已登录，请使用 logout 退出登录后再重新登录！");
            return false;
        }

        if (args.containsKey("account")) {
            configuration.setAccount(Long.parseLong(args.get("account")));
        }
        if (args.containsKey("password")) {
            configuration.setPassword(args.get("password"));
        }
        if (args.containsKey("protocol")) {
            configuration.setProtocol(args.get("protocol"));
        }
        if (args.containsKey("save")) {
            configuration.save();
        }
        robot.login();
        return true;
    }

    public boolean logout(HashMap<String, String> args) throws Exception {
        if (robot.isClose()) {
            log.warn("没有已登录的机器人");
            return false;
        }
        robot.close();
        return true;
    }

    public boolean status(HashMap<String, String> args) throws Exception {

        if (robot.isClose()) {
            System.out.println(ansi()
                    .a("机器人状态：")
                    .fgBrightRed()
                    .a("未登录")
                    .reset());
        } else {
            long botId = robot.getBotId();
            if (botId == 0L) {
                System.out.println(ansi()
                        .a("机器人状态：")
                        .fgBrightRed()
                        .a("离线")
                        .reset());
            } else {
                System.out.println(ansi()
                        .a("机器人状态：")
                        .fgBrightGreen()
                        .a("在线")
                        .reset()
                        .newline()
                        .a("当前登录：")
                        .fgBrightCyan()
                        .a(botId)
                        .reset());
            }
        }

        return true;
    }

    public boolean send(HashMap<String, String> args) throws Exception {

        if (robot.isClose()){
            log.warn("机器人尚未登录");
            return false;
        }

        SNContact contact;
        if (args.containsKey("friend")){
            contact = robot.getFriend(Long.parseLong(args.get("friend")));
        }else if(args.containsKey("group")){
            contact = robot.getGroup(Long.parseLong(args.get("group")));
        }else if(args.containsKey("stranger")){
             contact = robot.getStranger(Long.parseLong(args.get("stranger")));
        }else{
            log.warn("发送的消息没有目标");
            return false;
        }
        if (contact == null){
            log.warn("没有获取到目标，请检查输入是否正确！");
            return false;
        }
        SNMessageSource source = contact.sendMessage(robot, new SNText(args.get("msg")).toChain());
        if (source == null){
            log.warn("发送消息失败，请检查日志！");
            return false;
        }else{
            log.info("发送成功！\n源：{} \n内部：{}\n时间戳：{}", Arrays.toString(source.getIds()), Arrays.toString(source.getInternalIds()), source.getTime());
        }
        return true;
    }

}
