package com.slimenano.sdk.commands;

import lombok.Data;

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
        private ArgumentBean[] arguments;
    }

    @Data
    public static class ArgumentBean{
        private String name;
        private String simplify;
        private String description;
        private String[] excludes;


    }

}
