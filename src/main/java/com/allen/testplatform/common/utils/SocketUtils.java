package com.allen.testplatform.common.utils;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author Fan QingChuan
 * @since 2022/6/27 10:29
 */

public class SocketUtils {

    public static boolean isPortBeUsed(int port) {
        boolean isRunning = false;
        ServerSocket serverSocket;

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.close();
        } catch (IOException e) {

            isRunning = true;
        } finally {
            serverSocket = null;
        }

        return isRunning;
    }
}
