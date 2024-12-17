package plus.format;

import java.lang.reflect.Constructor;
import java.util.*;


/**
 * Main class for formatting, use this to build string formater
 * <br>
 * It is not intended to be shared from multiple threads
 * <br>
 * <br>
 * Example:
 * <pre>{@code
 * final MapVarProvider<User> PROVIDER = new MapVarProvider<>(User.class);
 * ...
 * class User{
 *     String name; int age; User parent;
 *
 *     public User(String name, int age) {
 *         this.name = name;
 *         this.age = age;
 *     }
 * }
 * ...
 * FormatBuilder<User> builder = new FormatBuilder<>(User.class)
 *     .setProvider(PROVIDER).fixEmpty()
 *     .setFormat("User-%age%: %name%, parent-%parent.age%: %parent/name%");
 *
 * Formatter<User> format = fmBuilder.build();
 * ...
 * User user1 = new User("Homa"  , 1);
 * User user2 = new User("Te4hno", 5);
 *
 * user1.parent = user2;
 *
 * System.out.println(format.get(user1)); //result: "User-1: Homa, parent-5: Te4hno"
 * }</pre>
 * @param <T> - input format type
 */
public final class FormatBuilder<T> {
    private static final IVarProvider[] EMPTY = new IVarProvider[0];
    //Store cached providers for current class
    private final IdentityHashMap<Class, IVarProvider[]> cache = new IdentityHashMap<>();
    //Store cached var getters
    private final HashMap<String, IVarGetter> getters = new HashMap<>();
    private final Class<T> inputType;
    //Current format class
    private Class formatClazz = Formatter.class;
    //Current format
    private String format = "";
    //All available providers
    private IVarProvider[] allProviders;
    //Special characters for defining text boundaries and variables
    private char start = '%', end = '%', ignore = '\\';
    private final String nextKey = "/";
    //Default provider, used if no providers are found
    private IVarProvider onEmpty = null;
    //Should be fix if empty provider can`t build non-null result
    private boolean fixEmpty = false;

    /**
     * @param inputType format type
     */
    public FormatBuilder(Class<T> inputType) {
        this.inputType = inputType;
    }


    /**
     * Set format string
     */
    public FormatBuilder<T> setFormat(String format){
        this.format = format;
        return this;
    }


    /**
     * Edit special characters for find variables
     * @param start char beginning of variable
     * @param end char end of variable
     * @param ignore the character that should be ignored, character following it is not special
     */
    public FormatBuilder<T> setMath(char start, char end, char ignore){
        this.start = start;
        this.end = end;
        this.ignore = ignore;
        return this;
    }


    /**
     * Add providers that can be used to get vargetters
     * @param allProviders providers to add
     */
    public FormatBuilder<T> setProvider(IVarProvider... allProviders){
        if(this.allProviders == null){
            this.allProviders = allProviders;
        } else {
            IVarProvider[] prev = this.allProviders;
            IVarProvider[] cur  = this.allProviders = new IVarProvider[allProviders.length + prev.length];
            System.arraycopy(prev, 0, cur, 0, prev.length);
            System.arraycopy(allProviders, 0, cur, prev.length, allProviders.length);
        }
        return this;
    }


    /**
     * Set provider that can be used if vargetters not founded
     */
    public FormatBuilder<T> setOnEmpty(IVarProvider onEmpty){
        this.onEmpty = onEmpty;
        return this;
    }


    /**
     * Enable fix situation, when empty provider build null
     */
    public FormatBuilder<T> fixEmpty(){
        this.fixEmpty = true;
        return this;
    }


    /**
     * Edit the class whose object will be created
     * @param clazz new format class
     */
    public <C extends Formatter<T>> FormatBuilder<T> setFormatterClass(Class<C> clazz){
        this.formatClazz = clazz;
        return this;
    }


    /**
     * @param clazz input type
     * @return all providers, that can be used for this input
     */
    private IVarProvider[] getBy(Class clazz){
        IVarProvider[] all = this.allProviders;
        if(all == null)return EMPTY;

        IVarProvider[] result = cache.get(clazz);
        if(result != null)return result;

        ArrayList<Object> result1 = new ArrayList<>();

        for (IVarProvider pro:all){
            Class type = pro.getInputType();

            if(clazz == type){
                result1.add(0, pro);
            } else if(clazz.isAssignableFrom(type)){
                result1.add(pro);
            }
        }
        result = result1.toArray(EMPTY);

        cache.put(clazz, result);
        return result;
    }


    /**
     * @param name vargetter name
     * @return vargetter by name
     * @throws IllegalArgumentException if vargetter not found and empty fix not enabled
     */
    private IVarGetter getBy(String name){
        IVarGetter result, result1;
        if((result = getters.get(name)) != null)return result;
        String nk;

        if(name.contains(nk = nextKey)){
            Class input = this.inputType;
            l1:for (String key : name.split(nk)) {
                for (IVarProvider pro : getBy(input)) {
                    if ((result1 = pro.getFmGetter(key)) != null) {
                        input = result1.getType();

                        if (result == null) result = result1;
                        else                result = new AdaptVarGetter(result, result1);

                        continue l1;
                    }
                }
                onEmpty(name);
            }
        } else {
            for (IVarProvider pro: getBy(inputType))
                if((result = pro.getFmGetter(name)) != null)break;
        }

        if(result == null){
            result = onEmpty(name);
        }

        getters.put(name, result);
        return result;
    }


    /**
     * Called when vargetter not founded
     * @param name vargetter name
     * @throws IllegalArgumentException if empty vargetter not exist and empty fix not enabled
     */
    private IVarGetter onEmpty(String name){
        IVarProvider onEmpty;
        IVarGetter result = null;
        if((onEmpty = this.onEmpty) != null){
            if(onEmpty == IVarProvider.EMPTY){
                result = new ConstVarGetter(start+name+end);
            } else {
                result = onEmpty.getFmGetter(name);
            }
        }
        if(result == null){
            if(fixEmpty)
                result = new ConstVarGetter(start+name+end);
            else
                throw new IllegalArgumentException("Cant find getter for ["+name+"]");
        }
        return result;
    }


    /**
     * Build immutable formatter
     * @return new Formatter`s instance
     * @throws IllegalArgumentException if format not valid
     */
    public Formatter<T> build(){
        char[] chars = format.toCharArray();
        char start = this.start, end = this.end, ignore = this.ignore;
        StringBuilder builder = new StringBuilder();
        HashMap<Key,FEntry> entries = new HashMap<>();

        int pos = 0;
        int countItems = 0, countFuncs = 0;

        boolean addNext = false;
        boolean isVar   = false;

        for(int cursor = 0, len = chars.length ;cursor < len; cursor++){
            char cur = chars[cursor];
            if(addNext){
                builder.append(cur);
                addNext = false;
                continue;
            }

            if(cur == ignore){
                addNext = true;
                continue;
            }

            if(isVar){
                if(cur == end){
                    isVar = false;
                    if(add(entries, pos, builder.toString(), true))
                        ++countFuncs;
                    ++pos;
                    builder.setLength(0);
                } else
                    builder.append(cur);
            } else {
                if(cur == start){
                    isVar = true;
                    if(add(entries, pos, builder.toString(), false))
                        ++countItems;
                    ++pos;
                    builder.setLength(0);
                } else
                    builder.append(cur);
            }
        }

        if(builder.length() > 0){
            if(isVar)throw new IllegalArgumentException("Invalid format!");

            if(add(entries, pos, builder.toString(), false))
                ++countItems;
            ++pos;
        }

        for (FEntry entry:entries.values()){
            if(entry.isFunc){
                entry.varGetter = getBy(entry.textValue);
                entry.textValue = null;
            }
        }

        String[] items       = new String[countItems];
        int[][] itemsPos     = new int[countItems][];
        IVarGetter[] getters = new IVarGetter[countFuncs];
        int[][] gettersPos   = new int[countFuncs][];

        int posI = 0, posG = 0;

        for (FEntry entry:entries.values()){
            if(entry.isFunc){
                getters[posG]    = entry.varGetter;
                gettersPos[posG] = entry.addPositions;
                ++posG;
            } else {
                items[posI]    = entry.textValue;
                itemsPos[posI] = entry.addPositions;
                ++posI;
            }
        }

        //Fast create formatter if used default class
        if(formatClazz == Formatter.class)
            return new Formatter<>(items, itemsPos, getters, gettersPos, pos);

        try {
            Constructor ctor = formatClazz.getDeclaredConstructor(
                    items.getClass(), itemsPos.getClass(), getters.getClass(), gettersPos.getClass(), int.class
            );
            return (Formatter)ctor.newInstance(items, itemsPos, getters, gettersPos, pos);
        } catch (Throwable e){
            throw new IllegalArgumentException(e);
        }
    }


    private static boolean add(HashMap<Key, FEntry> entries, int pos, String key, boolean func){
        Key key1 = new Key(key, func);
        FEntry entry = entries.get(key1);
        if(entry == null){
            entry = new FEntry();
            entry.textValue = key;
            entry.isFunc = func;
            entries.put(key1, entry);
            entry.addPos(pos);
            return true;
        } else {
            entry.addPos(pos);
            return false;
        }
    }


    private static class Key{
        final String key;
        final boolean isFunc;

        Key(String key, boolean isFunc) {
            this.key = key;
            this.isFunc = isFunc;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key1 = (Key) o;
            return isFunc == key1.isFunc && Objects.equals(key, key1.key);
        }
        @Override
        public int hashCode() {
            int result = Objects.hashCode(key);
            result = 31 * result + Boolean.hashCode(isFunc);
            return result;
        }
    }


    private static class FEntry{
        int[] addPositions;
        IVarGetter varGetter;
        String textValue;
        boolean isFunc;

        void addPos(int pos) {
            int[] ap = this.addPositions;
            if (ap == null) {
                addPositions = new int[]{pos};
            } else {
                ap = Arrays.copyOf(ap, ap.length + 1);
                ap[ap.length - 1] = pos;
                this.addPositions = ap;
            }
        }
    }
}
