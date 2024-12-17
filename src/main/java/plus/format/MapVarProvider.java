package plus.format;

import plus.format.utils.FieldVarGetter;
import java.lang.reflect.Field;
import java.util.HashMap;


public class MapVarProvider<T> extends HashMap<String,IVarGetter<T>> implements IVarProvider<T>{
    private final Class<T> input;

    public MapVarProvider(Class<T> input) {
        this.input = input;
        init();
    }


    protected void init(){}


    @Override
    public IVarGetter<T> getFmGetter(String name) {
        IVarGetter<T> result = super.get(name);
        if(result != null)return result;

        if(super.containsKey(name))return null;
        synchronized (this) {
            int last;
            if ((last = name.lastIndexOf(".")) > 0) {
                String pre = name.substring(0, last);

                result = getFmGetter(pre);
                if (result != null) {
                    String post = name.substring(last + 1);

                    IVarGetter<T> getter = tryFiled(result.getType(), post);

                    if (getter != null) {
                        getter = new AdaptVarGetter<>(result, getter);
                    }
                    super.put(name, getter);
                    return getter;
                }
            } else {
                FieldVarGetter<T> getter = tryFiled(getInputType(), name);
                super.put(name, getter);
                return getter;
            }
        }
        return null;
    }


    private static FieldVarGetter tryFiled(Class clazz, String name){
        Class in = clazz;
        while (clazz != Object.class){
            Field[] fields = clazz.getDeclaredFields();
            for (Field field:fields){
                if(field.getName().contains(name)){
                    return FieldVarGetter.of(in, field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }


    public MapVarProvider<T> reg(String name, IVarGetter<T> getter){
        super.put(name, getter);
        return this;
    }


    public Section section(String prefix){
        return new Section(prefix+".");
    }


    @Override
    public Class<T> getInputType() {
        return input;
    }


    public class Section{
        private final String prefix;

        private Section(String prefix) {
            this.prefix = prefix;
        }


        public Section reg(String name, IVarGetter<T> getter){
            MapVarProvider.this.put(prefix+name, getter);
            return this;
        }
    }
}