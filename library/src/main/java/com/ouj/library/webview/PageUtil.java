package com.ouj.library.webview;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageUtil {
    public static String getPage(String url) {
        if (url == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("(^|!|&)page=([^&]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        } else {
            return null;
        }
    }

}















