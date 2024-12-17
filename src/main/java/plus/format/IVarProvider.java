package plus.format;


/**
 * Vargetter provider
 * <br>
 * Create a class implementing this interface to use your own variables in formatting, see {@link MapVarProvider}
 * @param <T> input type
 */
public interface IVarProvider<T> {
    /**
     * @param name variable name
     * @return vargetter for name or null if not founded
     */
    IVarGetter<T> getFmGetter(String name);


    /**
     * @return formatter`s input type
     */
    Class<T> getInputType();


    /**
     * Empty provider instance, this allways return empty vargetter
     */
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