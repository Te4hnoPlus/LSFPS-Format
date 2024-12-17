package plus.format.utils;

import plus.format.IVarGetter;
import java.lang.reflect.Field;


/**
 * Field`s vargetter template
 * <br>
 * Don`t extend this, see {@link FieldVarGetter#of(Class, Field)}
 * @param <T> input type
 */
public abstract class FieldVarGetter<T> implements IVarGetter<T> {
    //input type
    final Class<T> inType;
    //field`s offset
    final long off;

    /**
     * Don`t call this manually! Use {@link FieldVarGetter#of(Class, Field)}
     * @param type input type
     * @param field target field
     */
    FieldVarGetter(Class<T> type, Field field) {
        this.inType = type;
        off = UnsafeUtils.UNSAFE.objectFieldOffset(field);
    }


    /**
     * Create vargetter from field
     * @param type input type
     * @param field target field
     * @return field`s vargetter instance
     */
    public static <T> FieldVarGetter<T> of(Class<T> type, Field field){
        Class<?> fType = field.getType();
        if(fType == boolean.class)return new Bool<>(  type, field);
        if(fType == byte.class)   return new Byte<>(  type, field);
        if(fType == short.class)  return new Short<>( type, field);
        if(fType == int.class)    return new Int<>(   type, field);
        if(fType == long.class)   return new Long<>(  type, field);
        if(fType == float.class)  return new Float<>( type, field);
        if(fType == double.class) return new Double<>(type, field);
        return new Obj<>(type, field);
    }


    @Override
    public Object get(T t) {
        if(inType.isAssignableFrom(t.getClass()))
            return UnsafeUtils.UNSAFE.getObject(t, off);
        return null;
    }


    //Type-specific field getters
    static final class Bool<T> extends FieldVarGetter<T>{
        Bool(Class<T> type, Field field) {
            super(type, field);
        }
        public Object get(T t) {
            if(inType.isAssignableFrom(t.getClass()))
                return UnsafeUtils.UNSAFE.getBoolean(t, off);
            return null;
        }
        @Override
        public Class<?> getType() {
            return boolean.class;
        }
    }
    static final class Byte<T> extends FieldVarGetter<T>{
        Byte(Class<T> type, Field field) {
            super(type, field);
        }
        public Object get(T t) {
            if(inType.isAssignableFrom(t.getClass()))
                return UnsafeUtils.UNSAFE.getByte(t, off);
            return null;
        }
        public Class<?> getType() {
            return byte.class;
        }
    }
    static final class Short<T> extends FieldVarGetter<T>{
        Short(Class<T> type, Field field) {
            super(type, field);
        }
        public Object get(T t) {
            if(inType.isAssignableFrom(t.getClass()))
                return UnsafeUtils.UNSAFE.getShort(t, off);
            return null;
        }
        public Class<?> getType() {
            return short.class;
        }
    }
    static final class Int<T> extends FieldVarGetter<T>{
        Int(Class<T> type, Field field) {
            super(type, field);
        }
        public Object get(T t) {
            if(inType.isAssignableFrom(t.getClass()))
                return UnsafeUtils.UNSAFE.getInt(t, off);
            return null;
        }
        public Class<?> getType() {
            return int.class;
        }
    }
    static final class Long<T> extends FieldVarGetter<T>{
        Long(Class<T> type, Field field) {
            super(type, field);
        }
        public Object get(T t) {
            if(inType.isAssignableFrom(t.getClass()))
                return UnsafeUtils.UNSAFE.getLong(t, off);
            return null;
        }
        public Class<?> getType() {
            return long.class;
        }
    }
    static final class Float<T> extends FieldVarGetter<T>{
        Float(Class<T> type, Field field) {
            super(type, field);
        }
        public Object get(T t) {
            if(inType.isAssignableFrom(t.getClass()))
                return UnsafeUtils.UNSAFE.getFloat(t, off);
            return null;
        }
        public Class<?> getType() {
            return float.class;
        }
    }
    static final class Double<T> extends FieldVarGetter<T>{
        Double(Class<T> type, Field field) {
            super(type, field);
        }
        public Object get(T t) {
            if(inType.isAssignableFrom(t.getClass()))
                return UnsafeUtils.UNSAFE.getDouble(t, off);
            return null;
        }
        public Class<?> getType() {
            return double.class;
        }
    }
    static final class Obj<T> extends FieldVarGetter<T>{
        final Class<?> type;
        Obj(Class<T> type, Field field) {
            super(type, field);
            this.type = field.getType();
        }
        public Object get(T t) {
            if(inType.isAssignableFrom(t.getClass()))
                return UnsafeUtils.UNSAFE.getObject(t, off);
            return null;
        }
        public Class<?> getType() {
            return type;
        }
    }
}