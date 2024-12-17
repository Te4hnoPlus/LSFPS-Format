package plus.format;


/**
 * Adapt var provider`s template for required type
 * @param <T> input type
 * @param <P> parent type
 */
public abstract class AdaptVarProvider<T, P> implements IVarProvider<T>{
    protected final IVarProvider<P> parent;

    /**
     * @param parent parent var provider
     * @throws IllegalArgumentException if parent is null
     */
    public AdaptVarProvider(IVarProvider<P> parent) {
        if(parent == null)throw new IllegalArgumentException();
        this.parent = parent;
    }


    @Override
    public IVarGetter<T> getFmGetter(String name) {
        IVarGetter<P> result = parent.getFmGetter(name);
        if(result == null)return null;
        return adapt(result);
    }


    /**
     * Adapt vargetter
     * @param src non-null vargetter
     * @return vargetter of required input
     */
    public abstract IVarGetter<T> adapt(IVarGetter<P> src);


    @Override
    public Class getInputType() {
        return parent.getInputType();
    }
}