package com.bioelectronic.framework;

import lombok.extern.slf4j.Slf4j;
import com.bioelectronic.framework.core.BaseRobot;
import com.bioelectronic.framework.plugin.PluginManager;
import com.bioelectronic.sdk.framework.DefaultIGUIBridge;
import com.bioelectronic.sdk.framework.SystemInstance;
import com.bioelectronic.sdk.framework.annotations.Mount;
import com.bioelectronic.sdk.logger.Marker;

@Slf4j
@SystemInstance
@Marker("控制台")
public class SNRobotCLIBridge extends DefaultIGUIBridge {

    @Mount
    private PluginManager manager;

    @Mount
    private BaseRobot robot;

    public SNRobotCLIBridge() {
        super("alphe-1.0.0", "SNRobot-CLI-Bridge");
    }

    @Override
    public boolean confirm(String title, String content, int type) {
        return true;
    }

    @Override
    public void main(String[] args) {
        robot.login();
        manager.load("DemoPlugin-1.0.jar");
        log.info("环境已部署，正在运行");
    }

}
