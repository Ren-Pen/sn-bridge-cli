package top.bioelectronic.framework;

import lombok.extern.slf4j.Slf4j;
import top.bioelectronic.framework.core.BaseRobot;
import top.bioelectronic.framework.plugin.PluginManager;
import top.bioelectronic.sdk.framework.DefaultIGUIBridge;
import top.bioelectronic.sdk.framework.SystemInstance;
import top.bioelectronic.sdk.framework.annotations.Mount;
import top.bioelectronic.sdk.logger.Marker;

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
        log.info("环境已部署，正在运行");
    }

}
