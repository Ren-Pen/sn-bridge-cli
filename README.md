# SlimeNano 控制台界面桥

该界面桥负责为 SlimeNano 提供控制台操作环境

**注意：SlimeNano 仅支持同时引入一种界面桥**

- 命令补全
- 输入高亮
- 插件可扩展

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!-- 示例 -->
<plugin xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.slimenano.com/plugin http://www.slimenano.com/schema/plugin.xsd
                            http://www.slimenano.com/cli http://www.slimenano.com/schema/bridge-cli.xsd"
        xmlns="http://www.slimenano.com/plugin">
    <name>demo plugin</name>
    <path>com.demo.DemoPlugin</path>
    <version>1.0.1</version>
    <author>xzy5487</author>
    <description><![CDATA[This is a demo plugin!]]></description>
    <permissions>
        <permission use="ROOT"/>
        <permission use="BEHAVIOR_IMG_UPLOAD"/>
    </permissions>

    <extension>
        <console xmlns="http://www.slimenano.com/cli" prefix="demo">
            <command bean="classpath:com.demo.DemoPlugin" method="demo" name="demo">
                <arguments>
                    <argument name="demo" simplify="r" description="运行demo" empty="FORCE" required="true"/>
                    <argument name="debug" simplify="d" description="插件调试指令" empty="FORCE"/>
                </arguments>
            </command>
            <command bean="classpath:com.demo.DemoPlugin" method="friend" name="friend" empty="FALSE">
                <arguments>
                    <argument name="target" simplify="t" description="目标" empty="FALSE" required="true"/>
                    <argument name="msg" simplify="m" description="消息" empty="FALSE" required="true"/>
                </arguments>
            </command>
        </console>
    </extension>
</plugin>
```
