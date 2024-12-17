package plus.format;


public interface IVarProvider<T> {

    IVarGetter<T> getFmGetter(String name);


    Class getInputType();


    IVarProvider EMPTY = new IVarProvider() {
        @Override
        public IVarGetter getFmGetter(String name) {
            return IVarGetter.EMPTY;
        }
        @Override
        public Class getInputType() {
            return Object.class;
        }
    };
}