package plus.format;

import plus.format.utils.FieldVarGetter;
import java.lang.reflect.Field;
import java.util.HashMap;


/**
 * Default variable provider for {@link FormatBuilder}
 * <br>
 * It can return vargetters from registered ones or create them dynamically from field names
 * <br>
 * <br>
 * Example:
 * <pre>{@code
 * public static MapVarProvider<User> PRO = new MapVarProvider<User>(User.class){
 *     @Override
 *     protected void init() {
 *         reg("name"  , (IVarGetter.Str<Examples.User>) user -> user.name);
 *         reg("parent", new IVarGetter<Examples.User>() {
 *             public Object get(User user) {
 *                 return user.parent;
 *             }
 *             public Class<?> getType() {
 *                 return Examples.User.class;
 *             }
 *         });
 *     }
 * };
 * }</pre>
 * @param <T> input type
 */
public class MapVarProvider<T> extends HashMap<String,IVarGetter<T>> implements IVarProvider<T>{
    //formatter`s input type
    private final Class<T> input;


    /**
     * @param input formatter`s input type
     */
    public MapVarProvider(Class<T> input) {
        this.input = input;
        init();
    }


    /**
     * Called when provider is created
     * Override this to call something after calling constructor
     */
    protected void init(){}


    @Override
    public IVarGetter<T> getFmGetter(String name) {
        IVarGetter<T> result = super.get(name);
        if(result != null)return result;

        if(super.containsKey(name))return null;
        //try to create getter from field
        //do int synchronized to avoid concurrent modifications
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


    /**
     * Try to create {@link FieldVarGetter}
     * @param clazz Target class
     * @param name Field`s name
     * @return FieldVarGetter instance or null if field not founded
     */
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


    /**
     * Register vargetter with name
     * <br>
     * <br>
     * Example:
     * <pre>{@code
     * provider.section("func")
     *     .reg("has-parent", (IVarGetter.Bool<User>) user -> user.parent != null)
     *     .reg("an-adult"  , (IVarGetter.Bool<User>) user -> user.age >= 18)
     *     .back().section("extra")
     *     .reg("name-caps" , (IVarGetter.Str<User>) user -> user.name.toUpperCase(Locale.ROOT));
     * }</pre>
     * @param name vargetter name
     * @param getter vargetter to register
     */
    public MapVarProvider<T> reg(String name, IVarGetter<T> getter){
        super.put(name, getter);
        return this;
    }


    /**
     * Create a prefix section for easy vargetter registration
     */
    public Section section(String prefix){
        return new Section(prefix+".");
    }


    @Override
    public Class<T> getInputType() {
        return input;
    }


    /**
     * Prefix section for easy vargetter registration
     */
    public class Section{
        private final String prefix;

        private Section(String prefix) {
            this.prefix = prefix;
        }


        /**
         * Register vargetter with "prefix" + "." + name
         * @param name vargetter name
         * @param getter vargetter to register
         */
        public Section reg(String name, IVarGetter<T> getter){
            MapVarProvider.this.put(prefix+name, getter);
            return this;
        }


        /**
         * @return parent provider
         */
        public MapVarProvider<T> back(){
            return MapVarProvider.this;
        }
    }
}