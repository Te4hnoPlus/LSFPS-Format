package plus.format;

import java.util.Objects;


/**
 * Immutable vargetter
 * @param <T> input type, in this case ignored
 */
public final class ConstVarGetter<T> implements IVarGetter<T> {
    //immutable return`s value
    private final Object val;

    public ConstVarGetter(Object val) {
        this.val = val;
    }


    @Override
    public Object get(T o) {
        return val;
    }


    @Override
    public boolean isConst() {
        return true;
    }


    @Override
    public Class<?> getType() {
        return val.getClass();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(val, ((ConstVarGetter<?>) o).val);
    }
}