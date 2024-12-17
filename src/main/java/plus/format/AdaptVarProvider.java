package plus.format;


public abstract class AdaptVarProvider<T,R> implements IVarProvider<T>{
    protected final IVarProvider<R> parent;

    public AdaptVarProvider(IVarProvider<R> parent) {
        this.parent = parent;
    }


    @Override
    public IVarGetter<T> getFmGetter(String name) {
        IVarGetter<R> result = parent.getFmGetter(name);
        if(result == null)return null;
        return adapt(result);
    }


    public abstract IVarGetter<T> adapt(IVarGetter<R> src);


    @Override
    public Class getInputType() {
        return parent.getInputType();
    }
}
