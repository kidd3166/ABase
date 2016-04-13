package com.ouj.library.webview;

public enum ResultStatus {
    SUCCES("succCallback"),
    ERROR("errorCallback");
    
    public String val;
    private ResultStatus(String value) {
        val = value;
    }
}