package com.allen.testplatform.testscripts.scripts;

import cn.hutool.core.text.StrSplitter;
import cn.hutool.core.util.StrUtil;
import com.allen.testplatform.common.utils.EncryptUtils;
import com.allen.testplatform.common.utils.ExecuteShellUtil;
import com.allen.testplatform.common.utils.MyRuntimeUtil;
import com.allen.testplatform.testscripts.config.ReportLog;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.util.List;

public class RemoteServerShell {

    private static final ReportLog reportLog = new ReportLog(RemoteServerShell.class);

    @Test(description = "临时shell调用python开启远程服务器 http.server服务 用于静态资源展示")
    public void openPythonHttpServer() {

        ExecuteShellUtil instance = ExecuteShellUtil.getInstance();

        try {
            instance.init(
                    EncryptUtils.decrypt("dc8bda365b05d6d26aa7787d80f0b18a"),
                    22,
                    EncryptUtils.decrypt("f3d525f9824390f76a749ebf8fd47a90"),
                    EncryptUtils.decrypt("7bd8f3d33dad0fa4b190dbcebb676c8d"));

            String ls = instance.execCmd("ps -ef|grep python39 |grep http.server |grep /home/pages");
            reportLog.info("ps -ef ===============>  {}",ls);
            List<String> lineFreedList = StrSplitter.splitByRegex(StrUtil.trimToEmpty(ls), "\n", -1, true, true);
            lineFreedList.forEach(s -> {
                List<String> stringList = StrSplitter.split(StrUtil.trimToEmpty(s), "=", -1, true, true);
                stringList.forEach(str -> {
                    List<String> codeList = StrSplitter.split(str, " ", -1, true, true);
                    int pid = Integer.parseInt(codeList.get(1));
                    try {
                        instance.execCmd("kill "+pid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });

            ls = instance.execCmd("/home/sh/pythonHttpServer.sh");
            reportLog.info("python server ===============>  {}",ls);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test(description = "windows系统 关闭目标端口占用程序 kill PID")
    public void exeCmd(@Optional("4723")int port) {
        String command = String.format("cmd.exe /c netstat -ano | findstr \"%s\"",port);
        String rs = MyRuntimeUtil.execForStr(command).replaceAll(" ", "").replaceAll("\n","").replaceAll("\r","");
        String PID = rs.split("LISTENING")[1];
        command = String.format("cmd.exe /c taskkill /pid %s /F", PID);
        MyRuntimeUtil.execForStr(command);
    }


}
