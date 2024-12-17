package plus.format.utils;

import sun.misc.Unsafe;
import java.lang.reflect.Field;


public class UnsafeUtils {
    public static final Unsafe UNSAFE;
    private static final long STR_OFF;
    private static final boolean IS_STR_CHARS;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);

            Field value = String.class.getDeclaredField("value");
            STR_OFF      = UNSAFE.objectFieldOffset(value);
            IS_STR_CHARS = value.getType() == char[].class;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public static String mergeStringsFast(String[] args){
        if(args.length == 1)return args[0];

        int sumLen = 0, offset = 0;
        for(String arg:args) sumLen += arg.length();

        if(sumLen == 0)return "";

        if(IS_STR_CHARS){
            char[] chars = new char[sumLen];
            for(String arg:args){
                int len;
                char[] value = (char[]) UNSAFE.getObject(arg, STR_OFF);
                System.arraycopy(value, 0, chars, offset, len = arg.length());
                offset += len;
            }
            return stringOf(chars);
        } else {
            byte[] bytes = new byte[sumLen];
            for(String arg:args){
                int len;
                byte[] value = (byte[]) UNSAFE.getObject(arg, STR_OFF);
                System.arraycopy(value, 0, bytes, offset, len = arg.length());
                offset += len;
            }
            return stringOf(bytes);
        }
    }


    private static String stringOf(Object value) {
        try {
            String str = (String) UNSAFE.allocateInstance(String.class);
            UNSAFE.putObject(str, STR_OFF, value);
            return str;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}