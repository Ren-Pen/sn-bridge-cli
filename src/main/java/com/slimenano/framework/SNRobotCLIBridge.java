package com.slimenano.framework;

import com.slimenano.framework.config.RobotConfiguration;
import com.slimenano.framework.core.BaseRobot;
import com.slimenano.sdk.framework.DefaultIGUIBridge;
import com.slimenano.sdk.framework.SystemInstance;
import com.slimenano.sdk.framework.annotations.Mount;
import com.slimenano.sdk.logger.Marker;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

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

    private LineReader reader = null;

    public SNRobotCLIBridge() {
        super("alphe-1.0.0", "SNRobot-CLI-Bridge");
    }

    @Override
    public boolean confirm(String title, String content, int type) {
        if (reader == null) return false;
        System.out.println();
        System.out.println(title);
        System.out.println();
        System.out.println(content);
        if (type == OK_CANCEL) {
            System.out.println("(ok) or (cancel)?");
            while (true) {
                String s = reader.readLine(">").trim().toLowerCase();
                if ("ok".equals(s)) {
                    return true;
                } else if ("cancel".equals(s)) {
                    return false;
                }
                System.out.println("please input (ok) or (cancel)！");
            }
        }
        System.out.println("(yes) or (no)?");
        while (true) {
            String s = reader.readLine(">").trim().toLowerCase();
            if ("yes".equals(s)) {
                return true;
            } else if ("no".equals(s)) {
                return false;
            }
            System.out.println("please input (yes) or (no)！");
        }
    }

    @Override
    public void alert(String title, String content, int type) {
        String out = "\n" + title + "\n" + content;
        switch (type) {
            case ERROR:
                log.error(out);
                break;
            case WARN:
                log.warn(out);
                break;
            default:
                System.out.println(out);
                break;
        }
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
        reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .history(new DefaultHistory())
                .appName("SN Console Bridge")
                .highlighter(new DefaultHighlighter())
                .build();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                String preString = "no login";
                if (!robot.isClose()) {
                    preString = String.valueOf(robot.getBotId());
                }
                String s = reader.readLine(preString + " > ").trim();
                if (!s.isEmpty())
                    manager.exec(s);


            } catch (EndOfFileException | UserInterruptException ignore) {
                RobotApplication.stop();
            }
        }

    }

}
