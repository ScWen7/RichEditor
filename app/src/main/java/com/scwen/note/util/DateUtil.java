package com.scwen.note.util;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by scwen on 2019/5/7.
 * QQ ：811733738
 * 作用：
 */
public class DateUtil {
    public static final String PATTERN_1 = "MM月-dd日";

    public static final String PATTERN_2 = "yy年-MM月";

    public static final String PATTERN_3 = "MM月dd日";

    public static final String PATTERN_4 = "yyyy年";

    public static final String PATTERN_5 = "yyyy年MM月dd日";

    public static final String PATTERN_6 = "HH:mm";

    public static final String PATTERN_7 = "yyy-MM-dd";

    public static final String PATTERN_8 = "yyyy-MM-dd HH:mm";

    public static final String PATTERN_9 = "yyyy-MM-dd HH:mm:ss";

    public static final String PATTERN_10 = "HH:mm:ss";

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat dateFormat = new SimpleDateFormat();

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }


    public static String format(Date date, String pattern) {
        dateFormat.applyPattern(pattern);
        return dateFormat.format(date);
    }

    public static String convertFormat(String time, String currentPattern, String toPattern) {
        try {

            dateFormat.applyPattern(currentPattern);
            Date parse = dateFormat.parse(time);

            dateFormat.applyPattern(toPattern);
            String format = dateFormat.format(parse);
            return format;
        } catch (ParseException e) {
            e.printStackTrace();
            return time;
        }
    }


    /**
     * 字符串格式时长转 int
     *
     * @param time 字符串时长， 如 01:23
     * @return int 时长， 如 83
     */
    public static int getMinutes(String time) {
        try {
            String[] s = time.split(":");
            int hour = Integer.parseInt(s[0]);
            int min = Integer.parseInt(s[1]);

            return hour * 60 + min;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;

    }
}
