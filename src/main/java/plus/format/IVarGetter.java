package plus.format;


/**
 * Variable getter interface
 * @param <T> input type
 */
public interface IVarGetter<T> {
    /**
     * @param t input
     * @return result, class of result should be assignable to {@link #getType()}
     */
    Object get(T t);


    /**
     * @param t input
     * @return result as string, see {@link IVarGetter#get(Object)}
     */
    default String getStr(T t){
        Object r = get(t);
        if(r == null)return "null";
        return r.toString();
    }


    /**
     * @param t input
     * @return true if result equals "true", see {@link IVarGetter#get(Object)}
     */
    default boolean getBool(T t){
        Object result = get(t);
        if(result == null)return false;
        return Boolean.TRUE.equals(result);
    }


    /**
     * @param t input
     * @return try to get result as int, see {@link IVarGetter#get(Object)}
     * @throws ClassCastException if result not number
     */
    default int getInt(T t){
        Object r = get(t);
        if(r == null)return 0;
        return ((Number)r).intValue();
    }


    /**
     * @param t input
     * @return try to get result as double, see {@link IVarGetter#get(Object)}
     * @throws ClassCastException if result not number
     */
    default double getDouble(T t){
        Object r = get(t);
        if(r == null)return 0;
        return ((Number)r).doubleValue();
    }


    /**
     * @return true if getter is immutable and undependable of input
     */
    default boolean isConst(){return false;}


    /**
     * @return return type
     */
    Class<?> getType();


    /**
     * Variable getter for any type
     */
    @FunctionalInterface
    interface Aut<T> extends IVarGetter<T>{
        @Override
        default Class<?> getType(){return Object.class;}
    }


    /**
     * Variable getter for boolean type
     */
    @FunctionalInterface
    interface Bool<T> extends Aut<T>{
        default Object get(T t){return getBool(t) ? Boolean.TRUE : Boolean.FALSE;}
        boolean getBool(T t);
        default Class<?> getType(){return Boolean.class;}
    }


    /**
     * Variable getter for int type
     */
    @FunctionalInterface
    interface Int<T> extends Aut<T>{
        int getInt(T t);
        default Object get(T t) {return getInt(t);}
        default double getDouble(T t) {return getInt(t);}
        default Class<?> getType(){return Integer.class;}
    }


    /**
     * Variable getter for double type
     */
    @FunctionalInterface
    interface Double<T> extends Aut<T>{
        double getDouble(T t);
        default Object get(T t) {return getDouble(t);}
        default int getInt(T t) {return (int)getDouble(t);}
        default Class<?> getType(){return java.lang.Double.class;}
    }


    /**
     * Variable getter for string
     */
    @FunctionalInterface
    interface Str<T> extends Aut<T>{
        default Class<?> getType(){return String.class;}
    }


    /**
     * Empty vargetter instance, allways return null
     */
    IVarGetter EMPTY = new IVarGetter() {
        public Object get(Object o) {
            return null;
        }
        public Class<?> getType() {
            return Object.class;
        }
        public boolean isConst() {
            return true;
        }
    };
}