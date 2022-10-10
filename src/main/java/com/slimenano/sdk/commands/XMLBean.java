package com.slimenano.sdk.commands;

import lombok.Data;
import lombok.Getter;

import static org.fusesource.jansi.Ansi.ansi;

@Data
public class XMLBean {

    private String prefix;
    private CommandBean[] command;

    public enum Empty {
        FORCE(ansi().a("[")
                .fgBrightBlue().a("无参")
                .reset().a("]")
                .fgBrightGreen().a(" %s ")
                .reset().a("%s").toString()
                , ansi().a("[-")
                .fgBrightMagenta()
                .a("%s").reset()
                .a("][--").fgBrightCyan()
                .a("%s").reset()
                .a("] %s")
                .toString()),
        //"[-%s][--%s=[...]] %s"
        DEFAULT(ansi().a("[")
                .fgBrightBlue().a("默认")
                .reset().a("]")
                .fgBrightGreen().a(" %s ")
                .reset().a("%s")
                .toString()
                , ansi().a("[-")
                .fgBrightMagenta()
                .a("%s").reset()
                .a("][--").fgBrightCyan()
                .a("%s").reset()
                .a("=[").fgYellow()
                .a("...").reset()
                .a("]] %s")
                .toString()
        ),
        FALSE(ansi().a("[")
                .fgBrightBlue().a("供需")
                .reset().a("]")
                .fgBrightGreen().a(" %s ")
                .reset().a("%s").toString()
                , ansi().a("[-")
                .fgBrightMagenta()
                .a("%s").reset()
                .a("][--").fgBrightCyan()
                .a("%s").reset()
                .a("=<").fgBrightRed()
                .a("...").reset()
                .a(">] %s")
                .toString()
        );

        @Getter
        private final String commandFormat;

        @Getter
        private final String argumentFormat;

        Empty(String commandFormat, String argumentFormat) {
            this.commandFormat = commandFormat;
            this.argumentFormat = argumentFormat;
        }
    }

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
    public static class ArgumentBean {
        private String name;
        private String simplify;
        private String description;
        private boolean required;
        private String[] excludes;
        private String[] includes;
        private Empty empty;
    }

}
