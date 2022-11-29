package com.allen.testplatform.common.utils;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.JschRuntimeException;
import com.jcraft.jsch.*;
import org.springframework.scheduling.annotation.Async;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 执行Shell工具类
 */
public class ExecuteShellUtil {

    /** 未调用初始化方法 错误提示信息 */
    private static final String DONOT_INIT_ERROR_MSG = "please invoke init(...) first!";

    private static Session session;

    private ExecuteShellUtil() {
    }

    /**
     * 获取ExecuteShellUtil类实例对象
     *
     * @return 实例
     * @date 2019/4/29 16:58
     */
    public static ExecuteShellUtil getInstance() {
        return new ExecuteShellUtil();
    }

    /**
     * 初始化
     *
     * @param ip
     *         远程Linux地址
     * @param port
     *         端口
     * @param username
     *         用户名
     * @param password
     *         密码
     * @throws JSchException
     *         JSch异常
     * @date 2019/3/15 12:41
     */
    public void init(String ip, Integer port, String username, String password) throws JSchException {
        JSch jsch = new JSch();
        jsch.getSession(username, ip, port);
        session = jsch.getSession(username, ip, port);
        session.setPassword(password);
        Properties sshConfig = new Properties();
        sshConfig.put("StrictHostKeyChecking", "no");
        session.setConfig(sshConfig);
        session.connect(1200 * 1000);
    }

    /**
     * 执行一条命令
     */
    public String execCmd(String command) throws Exception {
//        if (session == null || channel == null || channelExec == null) {
//            throw new Exception(DONOT_INIT_ERROR_MSG);
//        }
        // 打开执行shell指令的通道
        Channel channel = session.openChannel("exec");
        ChannelExec channelExec = (ChannelExec) channel;
        channelExec.setCommand("source /etc/profile && source ~/.bash_profile && source ~/.bashrc &&  adb devices");
        channelExec.setCommand(command);
        channel.setInputStream(null);
        channelExec.setErrStream(System.err);
        // channel.setXForwarding();
        channel.connect();
        StringBuilder sb = new StringBuilder(16);
        try (InputStream in = channelExec.getInputStream();
             InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                sb.append("\n").append(buffer);
            }
            return sb.toString();
        }finally {
            if (channelExec != null && channelExec.isConnected()) {
                channelExec.disconnect();
            }
            if ( channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    /**
     * 执行一条命令 获取错误流中的内容
     */
    @Async
    public String execCmdErrContent(String command) throws Exception {
//        if (session == null || channel == null || channelExec == null) {
//            throw new Exception(DONOT_INIT_ERROR_MSG);
//        }
        // 打开执行shell指令的通道
        Channel channel = session.openChannel("exec");
        ChannelExec channelExec = (ChannelExec) channel;
        channelExec.setCommand(command);
        channel.setInputStream(null);
        ByteArrayOutputStream err  = new ByteArrayOutputStream();
        channelExec.setErrStream(err);
        channel.connect();
        StringBuilder sb = new StringBuilder(16);
        try (InputStream in = channelExec.getErrStream();
             InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                sb.append("\n").append(buffer);
            }
            if(StrUtil.contains(sb.toString(), "没有那个文件或目录")){
                return "";
            }else{
                return sb.toString();
            }

        }finally {
            if (channelExec != null && channelExec.isConnected()) {
                channelExec.disconnect();
            }
            if ( channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }


    public void closeConnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}