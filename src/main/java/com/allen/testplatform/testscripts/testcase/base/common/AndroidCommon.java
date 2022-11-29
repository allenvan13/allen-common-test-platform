package com.allen.testplatform.testscripts.testcase.base.common;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.utils.CommonUtils;
import com.allen.testplatform.testscripts.config.AppiumServiceEntity;
import com.allen.testplatform.testscripts.config.Assertion;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.appium.java_client.MultiTouchAction;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidTouchAction;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServerHasNotBeenStartedLocallyException;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;

public interface AndroidCommon extends AppCommon{

    ReportLog reportLog = new ReportLog(AndroidCommon.class);

    /**
     * 设置隐式等待时间
     * @param driver
     * @param second
     */
    default void setTimeOut(AndroidDriver driver, int second) {
        int nanos = second * 1000;
        Duration duration = Duration.ofNanos(nanos);
        driver.manage().timeouts().implicitlyWait(duration);
    }

    /**
     * 目标toast信息是否出现 (检查频率0.1秒)
     * @param driver T extends AppiumDriver
     * @param outTimeSeconds 超时秒值
     * @param message toast信息
     * @return
     */
    default boolean isToastHasAppeared(AndroidDriver driver, int outTimeSeconds, String message) {
        boolean isAppeared = false;
        try {
            reportLog.info("toast消息是否出现 ======== >> 等待时间上限:[{}]秒,预期消息:[{}]",outTimeSeconds,message);
            new WebDriverWait(driver,Duration.ofSeconds(outTimeSeconds),Duration.ofMillis(100))
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='" + message + "']")));
            isAppeared = true;
        } catch (Exception e) {
            reportLog.error("错误 ======== >> {}",e.getMessage());
        } finally {
            reportLog.info("结果 ======== >> toast消息是否出现:[{}]",isAppeared);
        }

        return isAppeared;
    }

    /**
     * 断言目标toast信息是否出现 (检查频率0.1秒)
     * @param driver T extends AppiumDriver
     * @param outTimeSeconds 超时秒值
     * @param message toast信息
     * @return
     */
    default void assertToastHasAppeared(AndroidDriver driver, int outTimeSeconds, String message) {
        String toastMessage = null;
        try {
            WebElement element = new WebDriverWait(driver, Duration.ofSeconds(outTimeSeconds),Duration.ofMillis(100))
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='" + message + "']")));
            toastMessage = element.getText();
        } catch (Exception e) {
            reportLog.error("错误 ======== >> {}",e.getMessage());
        }finally {
            Assertion.verifyNotNulls(toastMessage,"测试不通过! 元素不存在");
            if (toastMessage != null) {
                Assertion.verifyEquals(toastMessage,message);
            }
        }
    }

    /**
     * 在屏幕上画图标记(滑动操作)
     * @param driver AndroidDriver
     * @param type 1-勾 2-叉 3-圆形
     */
    default void drawMarkInScreen(AndroidDriver driver,int type) {
        AndroidTouchAction touchAction = new AndroidTouchAction(driver);
        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;
        if (type < 1 || type > 3) type = RandomUtil.randomInt(1,4);
        switch (type) {
            case 1:
                //打勾
                touchAction.longPress(PointOption.point(width/4, height/2)).moveTo(PointOption.point(width/2,height*3/4)).moveTo(PointOption.point(width*3/4,height/4)).release().perform();
                reportLog.info(" ======== >> 屏幕标记->[打勾]");
                break;
            case 2:
                //画叉
                touchAction.longPress(PointOption.point(width/4, height/3)).moveTo(PointOption.point(width*3/4,height*2/3)).release().perform();
                touchAction.longPress(PointOption.point(width*3/4, height/3)).moveTo(PointOption.point(width/4,height*2/3)).release().perform();
                reportLog.info(" ======== >> 屏幕标记->[画叉]");
                break;
            case 3:
                //画圆
                drawCircle(driver,new Point(width/2, height/2),200,30);
                reportLog.info(" ======== >> 屏幕标记->[画圆]");
                break;
            default:
                throw new IllegalArgumentException("标记类型不合法! ");
        }

    }

    /**
     * 绘制圆形
     * @param driver
     * @param origin 中心点位置
     * @param radius 半径长度
     * @param steps
     */
    default void drawCircle (AndroidDriver driver, Point origin, double radius, int steps) {
        Point firstPoint = getPointOnCircle(0, steps, origin, radius);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence circle = new Sequence(finger, 0);
        circle.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), firstPoint.x, firstPoint.y));
        circle.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));

        for (int i = 1; i < steps + 1; i++) {
            Point point = getPointOnCircle(i, steps, origin, radius);
            circle.addAction(finger.createPointerMove(Duration.ofMillis(100), PointerInput.Origin.viewport(), point.x, point.y));
        }

        circle.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(circle));
    }

    default Point getPointOnCircle (int step, int totalSteps, Point origin, double radius) {
        double theta = 2 * Math.PI * ((double)step / totalSteps);
        int x = (int)Math.floor(Math.cos(theta) * radius);
        int y = (int)Math.floor(Math.sin(theta) * radius);
        return new Point(origin.x + x, origin.y + y);
    }

    default int mulToInt(Double multiplier ,int multiplicand) {
        return Double.valueOf(multiplier * multiplicand).intValue();
    }

    /**
     * 向上滑动 (手指向上) 页面向下 单次滑动比例 20%
     *
     * @param driver AndroidDriver对象
     * @param second 持续时间
     * @param num 滚动次数
     */
    default void swipeToDown(AndroidDriver driver, double second, int num) {
        int nanos = (int) (second * 1000);
        Duration duration = Duration.ofNanos(nanos);
        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;
        TouchAction action = new TouchAction(driver);

        for (int i = 0; i <= num; i++) {
            action.press(PointOption.point(width / 2, height * 4 / 5)).waitAction(WaitOptions.waitOptions(duration))
                    .moveTo(PointOption.point(width / 2, height * 3 / 5)).release().perform();
        }

    }

    /**
     * 向下滑动 (手指向下) 页面向上 单次滑动比例 20%
     *
     * @param driver AndroidDriver对象
     * @param second 持续时间
     * @param num 滚动次数
     */
    default void swipeToUp(AndroidDriver driver, double second, int num) {
        int nanos = (int) (second * 1000);
        Duration duration = Duration.ofNanos(nanos);
        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;
        TouchAction action = new TouchAction(driver);

        for (int i = 0; i <= num; i++) {
            action.press(PointOption.point(width / 2, height * 2 / 5)).waitAction(WaitOptions.waitOptions(duration))
                    .moveTo(PointOption.point(width / 2, height * 3 / 5)).release().perform();
        }

    }

    /**
     * 向左滑动 (手指向左) 页面向右 单次滑动比例 20%
     *
     * @param driver AndroidDriver对象
     * @param second 持续时间
     * @param num 滑动次数
     */
    default void swipeToRight(AndroidDriver driver, double second, int num) {
        int nanos = (int) (second * 1000);
        Duration duration = Duration.ofNanos(nanos);
        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;
        TouchAction action = new TouchAction(driver);

        for (int i = 0; i <= num; i++) {
            action.press(PointOption.point(width * 4/5, height / 2)).waitAction(WaitOptions.waitOptions(duration))
                    .moveTo(PointOption.point(width *3/5, height / 2)).release().perform();
        }
    }

    /**
     * 向右滑动 (手指向右) 页面向左 单次滑动比例 20%
     *
     * @param driver AndroidDriver对象
     * @param second 持续时间
     * @param num 滑动次数
     */
    default void swipeToLeft(AndroidDriver driver, double second, int num) {
        int nanos = (int) (second * 1000);
        Duration duration = Duration.ofNanos(nanos);
        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;
        TouchAction action = new TouchAction(driver);
        for (int i = 0; i <= num; i++) {
            action.press(PointOption.point(width /5, height / 2)).waitAction(WaitOptions.waitOptions(duration))
                    .moveTo(PointOption.point(width * 2/5, height / 2)).release().perform();
        }

    }

    /**
     * 根据某点 进行扩放整体屏幕(双指扩放)
     * @param driver
     * @param pointX
     * @param pointY
     */
    default void enlargePoint(AndroidDriver driver,double pointX,double pointY) {
        int point_XX = Double.valueOf(pointX).intValue();
        int point_YY = Double.valueOf(pointY).intValue();
        MultiTouchAction multiAction = new MultiTouchAction(driver);
        AndroidTouchAction actionA = new AndroidTouchAction(driver);
        AndroidTouchAction actionB = new AndroidTouchAction(driver);
        int width = driver.manage().window().getSize().width / 10;
        int height = driver.manage().window().getSize().height / 10;
        actionA.press(PointOption.point(point_XX-width,point_YY-height)).waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000))).moveTo(PointOption.point(point_XX-(width*2),point_YY-(height*2))).release();
        actionB.press(PointOption.point(point_YY+width,point_YY+height)).waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000))).moveTo(PointOption.point(point_YY+(width*2),point_YY+(height*2))).release();
        multiAction.add(actionA).add(actionB).perform();
    }

    /**
     * 按方向、屏幕比例 屏幕滑动
     * @param driver AndroidDriver对象
     * @param ratioX 横向滑动比例
     * @param ratioY 纵向滑动比例
     * @param direction 滑动方向 UP 自下向上 DOWN 自上向下 LEFT 从右往左  RIGHT 从左往右
     */
    default void swipeByDirect(AndroidDriver driver, double ratioX, double ratioY, String direction){

        if (!(ratioX >= 0 && ratioY >= 0 && ratioX <= 1 && ratioY <= 1)) {
            throw new RuntimeException("滑动幅度比例ratioX ratioY 需>= 0 且 <= 1 ");
        }

        Dimension dimension = driver.manage().window().getSize();
        //留足屏幕上下边界共20% 左右 边界共10%,防止部分刘海屏手机遮挡不兼容问题
        int widMax = mulToInt(0.9,dimension.width);
        int heightMax = mulToInt(0.8,dimension.height);

        switch (direction) {
            case "UP" :
                swipeToPoint(driver,mulToInt(0.5,widMax),mulToInt(0.8,heightMax),mulToInt(0.5,widMax),mulToInt((1-ratioY),heightMax));
                break;
            case "DOWN" :
                swipeToPoint(driver,mulToInt(0.5,widMax),mulToInt(0.2,heightMax),mulToInt(0.5,widMax),mulToInt(ratioY,heightMax));
                break;
            case "LEFT" :
                swipeToPoint(driver,mulToInt(0.9,widMax),mulToInt(0.5,heightMax),mulToInt((1-ratioX),widMax),mulToInt(0.5,heightMax));
                break;
            case "RIGHT" :
                swipeToPoint(driver,mulToInt(0.1,widMax),mulToInt(0.5,heightMax),mulToInt(ratioX,widMax),mulToInt(0.5,heightMax));
                break;
            default:
                throw new RuntimeException("不支持的方向滑动!");
        }
    }

    /**
     * 按X、Y坐标点值 屏幕滑动某点至某点
     * @param driver AndroidDriver对象
     * @param startX 原点X坐标
     * @param startY 原点Y坐标
     * @param moveToX 目标点X坐标
     * @param moveToY 目标点Y坐标
     */
    default void swipeToPoint(AndroidDriver driver, double startX, double startY, double moveToX, double moveToY){

        TouchAction touchAction = new TouchAction(driver);
        touchAction.press(PointOption.point(Double.valueOf(startX).intValue(),Double.valueOf(startY).intValue()))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(500)))
                .moveTo(PointOption.point(Double.valueOf(moveToX).intValue(),Double.valueOf(moveToY).intValue()))
                .release()
                .perform();
    }

    /**
     * 给定区域(左上角点坐标 & 右下角坐标 即 x坐标最大最小,y坐标最大最小值),在该区域内随机点击某点一次
     *
     * @param driver AndroidDriver对象
     * @param minX x坐标最小值
     * @param minY y坐标最小值
     * @param maxX x坐标最大值
     * @param maxY y坐标最大值
     */
    default PointOption clickRandomPointInCustomArea(AndroidDriver driver, double minX, double minY, double maxX, double maxY) {

        Dimension dimension = driver.manage().window().getSize();
        int checkX = dimension.width;
        int checkY = dimension.height;

        if ((minX < 0 && maxX > checkX) || (minY < 0 && maxY > checkY)) {
            throw new IllegalArgumentException("坐标最大值不能超过屏幕宽度 最小值不能小于0");
        }

        AndroidTouchAction touchAction = new AndroidTouchAction(driver);
        int x = RandomUtil.randomInt(Double.valueOf(minX).intValue(),Double.valueOf(maxX).intValue());
        int y = RandomUtil.randomInt(Double.valueOf(minY).intValue(),Double.valueOf(maxY).intValue());

        reportLog.info(" 随机点击屏幕某点  ======== >> [{},{}]",x,y);
        PointOption option = PointOption.point(x, y);
        touchAction.tap(option).release().perform();

        return PointOption.point(x,y);
    }


    //以下为Excel自动化测试可调用

    default AndroidDriver initBaseDriverAndStartService(AppiumServiceEntity serviceEntity, String androidInitInfos) {

        if (ObjectUtil.isEmpty(serviceEntity)) {
            throw new IllegalArgumentException("serviceEntity 不能为空!");
        }

        if (ObjectUtil.isEmpty(androidInitInfos)) {
            throw new IllegalArgumentException("androidInitInfos 配置信息不能为空!");
        }

        if (!CommonUtils.isJSONString(androidInitInfos)) {
            throw new IllegalArgumentException("androidInitInfos JSON 格式不合法! 请检查!");
        }

        DesiredCapabilities capabilities = new DesiredCapabilities();
        JSONObject initJson = JSON.parseObject(androidInitInfos);
        initJson.keySet().forEach(key -> capabilities.setCapability(key,initJson.get(key)));

        URL url = null;
        if (ObjectUtil.isNotEmpty(serviceEntity.getService())) {
            AppiumDriverLocalService service = serviceEntity.getService();
            try {
                checkPortAndKillTask(APP_COMMON_DEFAULT_PORT);
                service.start();
                reportLog.info(" ========== >> 开启Appium本地服务 URL: [{}]",service.getUrl());
            } catch (AppiumServerHasNotBeenStartedLocallyException e) {
                e.printStackTrace();
            }finally {
                reportLog.info(" ========== >> Appium本地服务是否正常开启 [{}]",service.isRunning());
            }
            url = service.getUrl();
        }else {
            try {
                url = new URL(serviceEntity.getUrl());
                reportLog.info(" ========== >> 连接远程服务地址 [{}]",url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                reportLog.info(" ========== >>  远程地址解析失败![{}]",serviceEntity);
            }
        }
        return new AndroidDriver(url,capabilities);
    }

    //type-0
    /**
     * 缩放整体屏幕(双指缩放)
     * @param driver AndroidDriver
     */
    default void scaleScreen(AndroidDriver driver) {
        MultiTouchAction multiTouchAction = new MultiTouchAction(driver);
        TouchAction actionA = new TouchAction(driver);
        TouchAction actionB = new TouchAction(driver);
        int width = driver.manage().window().getSize().width / 10;
        int height = driver.manage().window().getSize().height / 10;
        actionA.press(PointOption.point(width *2,height *2)).waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000))).moveTo(PointOption.point(width *4,height *4)).release();
        actionB.press(PointOption.point(width *8,height *8)).waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000))).moveTo(PointOption.point(width *6,height *6)).release();
        multiTouchAction.add(actionA).add(actionB).perform();
    }

    /**
     * 扩放整体屏幕(双指扩放)
     * @param driver
     */
    default void enlargeScreen(AndroidDriver driver) {
        MultiTouchAction multiTouchAction = new MultiTouchAction(driver);
        AndroidTouchAction actionA = new AndroidTouchAction(driver);
        AndroidTouchAction actionB = new AndroidTouchAction(driver);
        int width = driver.manage().window().getSize().width / 10;
        int height = driver.manage().window().getSize().height / 10;
        actionA.longPress(PointOption.point(width *4,height *4)).moveTo(PointOption.point(width *3,height *3)).release();
        actionB.longPress(PointOption.point(width *6,height *6)).moveTo(PointOption.point(width *7,height *7)).release();
        multiTouchAction.add(actionA).add(actionB).perform();
    }

    default void swipeDown(AndroidDriver driver) {
        swipeToDown(driver,1,1);
    }

    default void swipeUp(AndroidDriver driver) {
        swipeToUp(driver,1,1);
    }

    default void swipeLeft(AndroidDriver driver) {
        swipeToLeft(driver,1,1);
    }

    default void swipeRight(AndroidDriver driver) {
        swipeToRight(driver,1,1);
    }


    //type-1
    default void pressKey(AndroidDriver driver,String excelInputText) {
        if (ObjectUtil.isEmpty(excelInputText)) {
            throw new IllegalArgumentException("pressKey 输入值不能为空");
        }
        driver.pressKey(new KeyEvent(AndroidKey.valueOf(excelInputText)));
    }

    /**
     * 断言目标toast信息是否出现 (检查频率0.1秒) 供Excel自动化使用
     * @param driver
     * @param excelInputText 输入值配置不能为空! 格式:  等待时间:等待消息内容 例如  3:提交成功
     */
    default void assertToastHasAppeared(AndroidDriver driver, String excelInputText) {
        if (ObjectUtil.isEmpty(excelInputText)) {
            throw new IllegalArgumentException("assertToastHasAppeared 输入值配置不能为空! 格式:  等待时间:等待消息内容 例如  3:提交成功");
        }

        String[] split = excelInputText.split(":");
        if (split.length != 2) {
            throw new IllegalArgumentException("assertToastHasAppeared 输入值配置格式不合法 格式:  等待时间:等待消息内容 例如  3:提交成功");
        }

        assertToastHasAppeared(driver, Integer.parseInt(split[0]),split[1]);
    }

    default void clickText(AndroidDriver driver, String excelInputText) {

        if (ObjectUtil.isEmpty(excelInputText)) {
            throw new IllegalArgumentException("clickText 输入值 目标文本不能为空!");
        }

        new WebDriverWait(driver,Duration.ofSeconds(APP_COMMON_TIME_OUT),Duration.ofMillis(APP_COMMON_SLEEP_TIME))
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@text='"+excelInputText+"']")))
                .click();
    }

    default void clickText(AndroidDriver driver,long TIME_OUT, String excelInputText) {

        if (ObjectUtil.isEmpty(excelInputText)) {
            throw new IllegalArgumentException("clickText 输入值 目标文本不能为空!");
        }

        new WebDriverWait(driver,Duration.ofSeconds(TIME_OUT),Duration.ofMillis(100))
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@text='"+excelInputText+"']")))
                .click();
    }

    default void drawMarkInScreen(AndroidDriver driver,String excelInputText) {
        if (ObjectUtil.isEmpty(excelInputText)) {
            throw new IllegalArgumentException("drawMarkInScreen 输入值类型配置不能为空!  1-画勾 2-画叉 3-画圆形 ");
        }
        int i = Integer.parseInt(excelInputText);
        if (i > 3 || i < 1) {
            throw new IllegalArgumentException("drawMarkInScreen 输入值不合法  1-画勾 2-画叉 3-画圆形 ");
        }

        drawMarkInScreen(driver,i);
    }

    default void clickRandomPointInCustomArea(AndroidDriver driver,String excelInputText) {
        if (ObjectUtil.isEmpty(excelInputText)) {
            throw new IllegalArgumentException("clickRandomPointInCustomArea 输入值配置不能为空! 格式: 左上角点坐标x,y:右下角点坐标x,y 例如  100,200:500,900");
        }

        String[] split = excelInputText.split(":");
        if (split.length != 2) {
            throw new IllegalArgumentException("clickRandomPointInCustomArea 输入值配置格式不合法 格式: 左上角点坐标x,y:右下角点坐标x,y 2个坐标用:分割 例如  100,200:500,900");
        }

        String[] leftUpPoint = split[0].split(",");
        String[] rightDownPoint = split[1].split(",");

        if (leftUpPoint.length != 2 || rightDownPoint.length != 2) {
            throw new IllegalArgumentException("clickRandomPointInCustomArea 输入值配置格式不合法 格式: 坐标值-X坐标与Y坐标用,分割 例如  100,200:500,900");
        }

        clickRandomPointInCustomArea(driver, Double.parseDouble(leftUpPoint[0]),Double.parseDouble(leftUpPoint[1]),Double.parseDouble(rightDownPoint[0]),Double.parseDouble(rightDownPoint[1]));
    }

    default void clickPoint(AndroidDriver driver,String excelInputText) {
        if (ObjectUtil.isEmpty(excelInputText)) {
            throw new IllegalArgumentException("clickPoint 输入值坐标值不能为空! 格式: X坐标,Y坐标  例如 100,200");
        }

        String[] split = excelInputText.split(":");
        if (split.length != 2) {
            throw new IllegalArgumentException("clickPoint 输入值坐标值格式不合法 格式: X坐标,Y坐标  例如 100,200");
        }

        AndroidTouchAction touchAction = new AndroidTouchAction(driver);
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);

        reportLog.info(" ======== >> 点击屏幕点 [{},{}]",x,y);
        PointOption option = PointOption.point(x, y);
        touchAction.tap(option).release().perform();
    }

    default void setTimeOut(AndroidDriver driver,String excelInputText) {
        if (ObjectUtil.isEmpty(excelInputText)) {
            throw new IllegalArgumentException("clickPoint 输入值坐标值不能为空! 格式: X坐标,Y坐标  例如 100,200");
        }

        int second = Integer.parseInt(excelInputText);
        setTimeOut(driver,second);
    }

}
