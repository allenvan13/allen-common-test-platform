package com.allen.testplatform.common.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DownloadUtils {
    public static void downloadFileToDirectory(String fileUrl, String filePath) {
        try {
            URL url = new URL(fileUrl);
            File file = new File(filePath);

            if (!file.getParentFile().exists()) {
                file.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }
            FileUtils.copyURLToFile(url, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
