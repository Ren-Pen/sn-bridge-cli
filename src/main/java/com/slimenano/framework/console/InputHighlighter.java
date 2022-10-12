package com.slimenano.framework.console;

import com.slimenano.framework.CMDManager;
import com.slimenano.sdk.commands.BeanCommand;
import com.slimenano.sdk.commands.Command;
import com.slimenano.sdk.commands.XMLBean;
import com.slimenano.nscan.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.Mount;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.slimenano.sdk.framework.ui.GUI_CONST.YES_NO;
import static org.jline.utils.AttributedStyle.*;

@SystemInstance
public class InputHighlighter implements Highlighter {


    @Mount
    private CMDManager manager;

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        AttributedStringBuilder asb = new AttributedStringBuilder();
        if (manager.disableH_C) {
            asb.append(buffer);
            return asb.toAttributedString();
        }
        // 判断是否为对话框模式
        AttributedStyle red = new AttributedStyle().foreground(BRIGHT | RED);
        AttributedStyle green = new AttributedStyle().foreground(BRIGHT | GREEN);
        AttributedStyle yellow = new AttributedStyle().foreground(YELLOW);
        if (manager.confirmMode == 0) {
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
                    asb.style(red).append(buffer);
                } else {
                    String[] cmds = buffer.split(" ");
                    asb.style(new AttributedStyle().foreground(BRIGHT | GREEN)).append(cmds[0]);
                    String args = buffer.substring(cmds[0].length());
                    int last = 0;
                    boolean q = false;
                    for (int i = 0; i < args.length(); i++) {
                        if (args.charAt(i) == '\"'){
                           if (q){
                               asb.style(yellow).append(args.substring(last, i+1));
                               last = i+1;
                               q = false;
                           }else{
                               asb.style(DEFAULT).append(args.substring(last, i));
                               last = i;
                               q = true;
                           }
                        }
                    }


                    if (last < args.length()) {
                        if (q){
                            asb.style(red).append(args.substring(last));
                        }else{
                            asb.style(DEFAULT).append(args.substring(last));
                        }
                    }

                }

            }
        } else {
            String trim = buffer.trim();
            if (manager.confirmMode == YES_NO) {
                if ("yes".equalsIgnoreCase(trim) || "no".equalsIgnoreCase(trim)) {
                    asb.style(green).append(buffer);
                } else {
                    asb.style(red).append(buffer);
                }
            } else {
                if ("ok".equalsIgnoreCase(trim) || "cancel".equalsIgnoreCase(trim)) {
                    asb.style(green).append(buffer);
                } else {
                    asb.style(red).append(buffer);
                }
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
