package com.slimenano.sdk.commands;

import lombok.Data;
import lombok.Getter;

@Data
public class XMLBean {

    private String prefix;
    private CommandBean[] command;

    @Data
    public static class CommandBean {
        private String bean;
        private String description;
        private String name;
        private String method;
        private Empty empty;
        private ArgumentBean[] arguments;
    }

    @Data
    public static class ArgumentBean{
        private String name;
        private String simplify;
        private String description;
        private boolean required;
        private String[] excludes;
        private String[] includes;
        private Empty empty;
    }

    public enum Empty{
        FORCE("[无参] %s %s", "[-%s][--%s] %s"),
        DEFAULT("[默认] %s %s", "[-%s][--%s=[...]] %s"),
        FALSE("[供需] %s %s", "[-%s][--%s=<...>] %s");

        @Getter
        private final String commandFormat;

        @Getter
        private final String argumentFormat;

        Empty(String commandFormat, String argumentFormat) {
            this.commandFormat = commandFormat;
            this.argumentFormat = argumentFormat;
        }
    }

}
