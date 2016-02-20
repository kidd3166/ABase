package com.ouj.library.util;

public class NumberUtils {

    public static int parseIntDef(String val) {
        int def = 0;
        try{
            def = Integer.parseInt(val);
        }catch (Exception e) {
            def = 0;
        }
        return def;
    }
    
    public static int parseInt(String val, int defInt) {
        int def = defInt;
        try{
            def = Integer.parseInt(val);
        }catch (Exception e) {
        }
        return def;
    }

    public static float parseFloat(String val, float defInt) {
        float def = defInt;
        try{
            def = Float.parseFloat(val);
        }catch (Exception e) {
        }
        return def;
    }
    
    public static long parseLongDef(String val) {
        long def = 0;
        try{
            def = Long.parseLong(val);
        }catch (Exception e) {
            def = 0;
        }
        return def;
    }
    
    public static int aroundInt(float val) {
        return new java.math.BigDecimal(val).setScale(0, java.math.BigDecimal.ROUND_HALF_UP).intValue();
    }
    
    public static int aroundIntMore(float val) {
        int intVal = aroundInt(val);
        if (val > intVal) {
            return intVal + 1;
        }
        return intVal;
    }
}
