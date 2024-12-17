package plus.format;

import java.lang.reflect.Constructor;
import java.util.*;


public final class FormatBuilder<T> {
    private static final IVarProvider[] EMPTY = new IVarProvider[0];

    private final IdentityHashMap<Class,IVarProvider[]> cache = new IdentityHashMap<>();
    private final HashMap<String,IVarGetter> getters = new HashMap<>();
    private final Class<T> inputType;
    private Class formatClazz = Formatter.class;
    private String format = "";
    private IVarProvider[] allProviders;
    private char start = '%', end = '%', ecran = '\\';
    private final String nextKey = "/";
    private IVarProvider onEmpty = null;
    private boolean fixEmpty = false;


    public FormatBuilder(Class<T> inputType) {
        this.inputType = inputType;
    }


    public FormatBuilder<T> setFormat(String format){
        this.format = format;
        return this;
    }


    public FormatBuilder<T> setMath(char stat, char end, char ecran){
        this.start = stat;
        this.end = end;
        this.ecran = ecran;
        return this;
    }


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


    public FormatBuilder<T> setOnEmpty(IVarProvider onEmpty){
        this.onEmpty = onEmpty;
        return this;
    }


    public FormatBuilder<T> fixEmpty(){
        this.fixEmpty = true;
        return this;
    }


    public <C extends Formatter<T>> FormatBuilder<T> setFormatterClass(Class<C> clazz){
        this.formatClazz = clazz;
        return this;
    }


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


    public Formatter<T> build(){
        char[] chars = format.toCharArray();
        char start = this.start, end = this.end, ecran = this.ecran;
        StringBuilder builder = new StringBuilder();
        HashMap<Key,FEntry> entries = new HashMap<>();

        int pos = 0;
        int countItems = 0, countFuncs = 0;

        boolean addNext = false;
        boolean isVar   = false;

        for (int cursor = 0, len = chars.length ;cursor < len; cursor++){
            char cur = chars[cursor];
            if(addNext){
                builder.append(cur);
                addNext = false;
                continue;
            }

            if(cur == ecran){
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


    private static boolean add(HashMap<Key,FEntry> entries, int pos, String key, boolean func){
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


    static class Key{
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


    static class FEntry{
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
