package com.allen.testplatform.common.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.enums.TestTerminalEnum;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import cn.nhdc.common.exception.BusinessException;
import cn.nhdc.common.util.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Fan QingChuan
 * @since 2022/6/1 16:17
 */
@Slf4j
public class CommonUtils {

    public static String SEPARATOR = System.getProperty("file.separator");

    /**
     * open网关 则返true
     */
    public static boolean isOpenGateWay(String terminalCode){
        List<String> list = Arrays.asList(TestTerminalEnum.JX_APP_ANDROID.getTerminalCode(),
                TestTerminalEnum.JX_APP_IOS.getTerminalCode(),
                TestTerminalEnum.JX_H5.getTerminalCode(),
                TestTerminalEnum.JX_XCX.getTerminalCode());
        return list.contains(terminalCode);
    }

    /**
     * 查找匹配用户,未指定姓名或未匹配到则随机取1名用户
     * @param searchRealName
     * @param userList
     * @return
     */
    public static UcUser findTargetUser(String searchRealName, List<UcUser> userList) {

        if (CollectionUtils.isEmpty(userList)) {
            throw new BusinessException("目标List为空!");
        }
        Collections.shuffle(userList);
        UcUser target;
        if (ObjectUtil.isNotEmpty(searchRealName)){
            target = userList.stream().filter(x -> x.getRealName().contains(searchRealName)).findFirst().orElse(userList.get(RandomUtil.randomInt(userList.size())));
            log.info("匹配到姓名为[{}]的目标人员 名称[{}]",searchRealName, ObjectUtil.isNotEmpty(target.getRealName()) ? target.getRealName() : "未知");
        }else {
            //如果未指定 则随机选取1名人员
            target = userList.get(RandomUtil.randomInt(userList.size()));
            log.info("未指定目标人员,随机选取1名人员 名称[{}]",target.getRealName());
        }

        return target;
    }

    public static String getOutPutRootPath() {
        String root = System.getProperty("user.dir");
        return root + SEPARATOR + "test-output" + SEPARATOR;
    }

    public static String getResourceRootPath() {
        String root = System.getProperty("user.dir");
        return root + SEPARATOR + "src" + SEPARATOR + "main" +SEPARATOR +"resources" +SEPARATOR;
    }

    public static String stopWindowsAppiumService(int port) {
        String command = String.format("cmd.exe /c netstat -ano | findstr \"%s\"",port);
//        String rs = MyRuntimeUtil.execForStr(command).replaceAll(" ", "").replaceAll("\n","").replaceAll("\r","");
        String rs = cn.nhdc.common.util.StringUtils.cleanBlank(MyRuntimeUtil.execForStr(command));

        if (rs.contains("LISTENING")) {
            rs = StringUtils.substringAfter(rs,"LISTENING");
        }

        if (rs.contains("TCP")) {
            rs = StringUtils.substringBefore(rs,"TCP");
        }

        command =  String.format("cmd.exe /c taskkill /pid %s /F ", rs);
        return MyRuntimeUtil.execForStr(command);
    }

    public static String stopLinuxAppiumService(int port) {
        String command = String.format("netstat -tunlp|grep %s",port);
        String rs = MyRuntimeUtil.execForStr(command).replaceAll(" ", "").replaceAll("\n","").replaceAll("\r","");
        String PID = StringUtils.subString(rs, "LISTEN", "/");
        command = String.format("kill -9  %s", PID);
        return MyRuntimeUtil.execForStr(command);
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static boolean isJSONString(String content) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }
        if (!content.startsWith("{") || !content.endsWith("}")) {
            return false;
        }
        try {
            JSONObject.parse(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据包名获取包下面所有的类名
     *
     * @param pack
     * @return
     * @throws Exception
     */
    public static Set<Class<?>> getClasses(String pack) throws Exception {
        // 第一个class类的集合
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        findClassesInPackageByJar(packageName, entries, packageDirName, recursive, classes);
                    } catch (IOException e) {
                        // log.error("在扫描用户定义视图时从jar包获取文件出错");
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    private static void findClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            @Override
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    // classes.add(Class.forName(packageName + '.' +
                    // className));
                    // 经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    // log.error("添加用户自定义视图类错误 找不到此类的.class文件");
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 以jar的形式来获取包下的所有Class
     *
     * @param packageName
     * @param entries
     * @param packageDirName
     * @param recursive
     * @param classes
     */
    private static void findClassesInPackageByJar(String packageName, Enumeration<JarEntry> entries, String packageDirName, final boolean recursive, Set<Class<?>> classes) {
        // 同样的进行循环迭代
        while (entries.hasMoreElements()) {
            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            // 如果是以/开头的
            if (name.charAt(0) == '/') {
                // 获取后面的字符串
                name = name.substring(1);
            }
            // 如果前半部分和定义的包名相同
            if (name.startsWith(packageDirName)) {
                int idx = name.lastIndexOf('/');
                // 如果以"/"结尾 是一个包
                if (idx != -1) {
                    // 获取包名 把"/"替换成"."
                    packageName = name.substring(0, idx).replace('/', '.');
                }
                // 如果可以迭代下去 并且是一个包
                if ((idx != -1) || recursive) {
                    // 如果是一个.class文件 而且不是目录
                    if (name.endsWith(".class") && !entry.isDirectory()) {
                        // 去掉后面的".class" 获取真正的类名
                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                        try {
                            // 添加到classes
                            classes.add(Class.forName(packageName + '.' + className));
                        } catch (ClassNotFoundException e) {
                            // .error("添加用户自定义视图类错误 找不到此类的.class文件");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    //TODO 获取目标目录 传入路径上文件夹名称
//    public static String getTargetPath(String var1,...) {
//
//    }

    public static void main(String[] args) {
//        System.out.println(stopWindowsAppiumService(4723));
        System.out.println(AesUtils.decrypt("78c1bb8758b94f772ea7185e2d78347e"));
    }

    public static List<String> getAdbDevices() {
        String rs = MyRuntimeUtil.execForStr("cmd.exe /c adb devices");
        if (rs.startsWith("List")) {
            String sss = cn.nhdc.common.util.StringUtils.cleanBlank(rs).substring(21);
            String[] devices = sss.split("device");
            return Arrays.asList(devices);
        }else {
            return null;
        }
    }
}
