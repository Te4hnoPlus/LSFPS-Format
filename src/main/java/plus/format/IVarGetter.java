package plus.format;


public interface IVarGetter<T> {
    Object get(T t);


    default boolean isConst(){return false;}


    Class<?> getType();


    @FunctionalInterface
    interface Int<T> extends IVarGetter<T>{
        @Override
        default Class<?> getType(){return Integer.class;};
    }


    @FunctionalInterface
    interface Double<T> extends IVarGetter<T>{
        @Override
        default Class<?> getType(){return java.lang.Double.class;};
    }


    @FunctionalInterface
    interface Str<T> extends IVarGetter<T>{
        @Override
        default Class<?> getType(){return String.class;};
    }


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