package com.slimenano.framework.console;

import com.slimenano.framework.CMDManager;
import com.slimenano.sdk.commands.BeanCommand;
import com.slimenano.sdk.commands.Command;
import com.slimenano.sdk.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.Mount;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jline.utils.AttributedStyle.*;

@SystemInstance
public class InputHighlighter implements Highlighter {


    @Mount
    private CMDManager manager;

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        AttributedStringBuilder asb = new AttributedStringBuilder();
        Matcher matcher = Command.cmdMatcher.matcher(buffer);
        // 先匹配命令
        if (!matcher.matches()) {
            asb.style(DEFAULT).append(buffer);
        } else {
            String system = matcher.group("system");
            String prefix = matcher.group("prefix");
            String plugin = matcher.group("plugin");
            BeanCommand beanCommand = null;
            if (system != null) {
                beanCommand = manager.innerCommand.get(system);
            } else if (prefix != null && plugin != null) {
                String name = prefix + "@" + plugin;
                beanCommand = manager.pluginCommand.get(name);
            }

            if (beanCommand == null) {
                asb.style(new AttributedStyle().foreground(BRIGHT | RED)).append(buffer);
            } else {
                String[] cmds = buffer.split(" ");
                asb.style(new AttributedStyle().foreground(BRIGHT | GREEN)).append(cmds[0]);
                asb.style(DEFAULT).append(buffer.substring(cmds[0].length()));

            }

        }

        return asb.toAttributedString();
    }

    @Override
    public void setErrorPattern(Pattern errorPattern) {

    }

    @Override
    public void setErrorIndex(int errorIndex) {

    }
}
