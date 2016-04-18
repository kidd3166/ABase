package com.ouj.library.module;

import com.ouj.library.BaseEntity;

/**
 * Created by liqi on 2016-4-16.
 */
public class UpdateResponse extends BaseEntity {

    public String versionName, updateContent, apkUrl;
    public int haveNewVersion, mustUpdate;
    public long apkSize;
}
