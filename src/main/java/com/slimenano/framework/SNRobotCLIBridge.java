package com.slimenano.framework;

import lombok.extern.slf4j.Slf4j;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import com.slimenano.framework.commons.XMLReader;
import com.slimenano.framework.config.RobotConfiguration;
import com.slimenano.framework.core.BaseRobot;
import com.slimenano.framework.plugin.PluginManager;
import com.slimenano.sdk.framework.DefaultIGUIBridge;
import com.slimenano.sdk.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.Mount;
import com.slimenano.sdk.logger.Marker;

import java.nio.charset.StandardCharsets;

@Slf4j
@SystemInstance
@Marker("控制台")
public class SNRobotCLIBridge extends DefaultIGUIBridge {

    @Mount
    private CMDManager manager;

    @Mount
    private BaseRobot robot;

    @Mount
    private RobotConfiguration rc;

    public SNRobotCLIBridge() {
        super("alphe-1.0.0", "SNRobot-CLI-Bridge");
    }

    @Override
    public boolean confirm(String title, String content, int type) {
        return true;
    }

    @Override
    public void main(String[] args) throws Exception {
        log.info("环境已部署，正在运行");
        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .jansi(true)
                .jna(true)
                .encoding(StandardCharsets.UTF_8)
                .build();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .history(new DefaultHistory())
                .appName("SN Console Bridge")
                .highlighter(new DefaultHighlighter())
                .build();

        while(!Thread.currentThread().isInterrupted()){
            try {
                String s = reader.readLine("no login > ");
                manager.exec(s);


            }catch (EndOfFileException | UserInterruptException ignore){
                RobotApplication.stop();
            }
        }

    }

}
