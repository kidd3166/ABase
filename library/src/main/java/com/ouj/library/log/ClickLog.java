/*
 * Copyright (C) 2014 Qiujuer <qiujuer@live.cn>
 * WebSite http://www.qiujuer.net
 * Created 09/16/2014
 * Changed 01/14/2015
 * Version 1.0.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ouj.library.log;

/**
 * Created by Liqi
 */
public final class ClickLog implements Comparable<ClickLog> {


    private static ClickEvent clickEvent;


    static {
        clickEvent = null;
    }


    public static void e(String name, String... params) {
        StringBuilder builder = new StringBuilder();
        if (params != null && params.length > 0) {
            for (String param : params) {
                if (builder.length() != 0)
                    builder.append("&");
                builder.append(param);
            }
        }
        ClickLog log = new ClickLog(name, builder.toString());
        saveFile(log);
    }

    public static void close() {
        if (clickEvent != null) {
            clickEvent.checkLogLength(true);
        }
    }

    /**
     * *********************************************************************************************
     * Private methods
     * *********************************************************************************************
     */
    /**
     * Save File
     *
     * @param log Log
     */
    private static void saveFile(ClickLog log) {
        try {
            if (clickEvent == null)
                clickEvent = new ClickEvent(10, 0.1f, ClickEvent.getDefaultLogPath());
            if (clickEvent != null) {
                clickEvent.addLog(log);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Class
     */
    private long time;
    private String name;
    private String params;

    public ClickLog(String name, String params) {
        this.time = System.currentTimeMillis();
        this.name = name;
        this.params = params;
    }

    public String toString() {
        return String.format("%d %s %s\r\n", time, name, params);
    }

    @Override
    public int compareTo(ClickLog another) {
        return 0;
    }
}



