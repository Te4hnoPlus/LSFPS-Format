package plus.format;


/**
 * Variable getter interface
 * @param <T> input type
 */
public interface IVarGetter<T> {
    /**
     *
     * @param t input
     * @return result, class of result should be assignable to {@link #getType()}
     */
    Object get(T t);


    /**
     * @return true if getter is immutable and undependable of input
     */
    default boolean isConst(){return false;}


    /**
     * @return return type
     */
    Class<?> getType();


    /**
     * Variable getter for boolean type
     */
    @FunctionalInterface
    interface Bool<T> extends IVarGetter<T>{
        @Override
        default Class<?> getType(){return Boolean.class;}
    }


    /**
     * Variable getter for int type
     */
    @FunctionalInterface
    interface Int<T> extends IVarGetter<T>{
        @Override
        default Class<?> getType(){return Integer.class;}
    }


    /**
     * Variable getter for double type
     */
    @FunctionalInterface
    interface Double<T> extends IVarGetter<T>{
        @Override
        default Class<?> getType(){return java.lang.Double.class;}
    }


    /**
     * Variable getter for string
     */
    @FunctionalInterface
    interface Str<T> extends IVarGetter<T>{
        @Override
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
        @Override
        public boolean isConst() {
            return true;
        }
    };
}