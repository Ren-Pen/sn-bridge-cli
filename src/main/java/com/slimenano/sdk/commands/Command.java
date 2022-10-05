package com.slimenano.sdk.commands;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * 命令接口
 */
public interface Command {

    Pattern argMatcher = Pattern.compile("--(?<name>[A-Za-z0-9_$-]+)(?:=(?<arg>\".*?\"|[^\\s\"-]*)(?:\\s+|$)|\\s+|$)");
    Pattern simplifyArgMatcher = Pattern.compile("-(?<name>[A-Za-z0-9])(?:\\s+(?<arg>\".*?\"|[^\\s\"-]*)(?:\\s+|$)|\\s+|$)");
    Pattern cmdMatcher = Pattern.compile("^(?:(?<prefix>[A-Za-z0-9_$.]+)@(?<plugin>[A-Za-z0-9_$.]+)|(?<system>[A-Za-z0-9_$.]+))(?:\\s+|$).*$");



    boolean exec(HashMap<String, String> args) throws Exception;

}
