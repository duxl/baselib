package com.duxl.baselib.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 算数相关工具类
 * create by duxl 2021/3/8
 */
public class ArithmeticUtils {

    /**
     * <pre>
     *     格式化数字成为有效的字符串
     *     将整数位最高位前面的0去掉，小数位末尾的0去掉
     *     例：
     *     输入 012.340 输出 12.34
     *     输入 .340  输出 0.34
     *     输入 0.00 输出 0
     * </pre>
     *
     * @param price
     * @return
     */
    public static String formatValid(BigDecimal price) {
        return new DecimalFormat("#.#######################################").format(price);
    }
}