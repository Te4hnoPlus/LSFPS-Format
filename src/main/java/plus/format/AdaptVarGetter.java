package plus.format;


public class AdaptVarGetter<T> implements IVarGetter<T>{
    private final IVarGetter<T> parent;
    private final IVarGetter current;

    public AdaptVarGetter(IVarGetter<T> parent, IVarGetter cur) {
        if(parent == cur)throw new IllegalArgumentException();
        this.parent = parent;
        this.current = cur;
    }


    @Override
    public Object get(T t) {
        return current.get(parent.get(t));
    }


    @Override
    public boolean isConst() {
        return parent.isConst() && current.isConst();
    }


    @Override
    public Class<?> getType() {
        return current.getType();
    }
}