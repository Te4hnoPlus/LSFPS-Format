package plus.format;

import plus.format.utils.UnsafeUtils;


/**
 * String formatter, to create this use {@link FormatBuilder}
 * <br>
 * You can inherit this class by saving the constructor parameters {@link FormatBuilder#FormatBuilder(Class)}  FormatBuilder}
 * @param <T> input type
 */
public class Formatter <T> implements IVarGetter.Str<T>{
    //immutable format string
    protected final String[] items;
    //variable getters
    protected final IVarGetter<T>[] getters;
    //immutable string`s positions
    protected final int[][] gPositions;
    //variable getter`s positions
    protected final int[][] iPositions;
    //count of all items
    protected final int size;

    /**
     * Don't call this manually! Use {@link FormatBuilder}
     * @param items immutable format string
     * @param iPositions immutable string`s positions
     * @param getters variable getters
     * @param gPositions variable getter`s positions
     * @param size count of all items
     */
    protected Formatter(String[] items, int[][] iPositions, IVarGetter<T>[] getters, int[][] gPositions, int size) {
        this.items = items;
        this.getters = getters;
        this.gPositions = gPositions;
        this.iPositions = iPositions;
        this.size = size;
    }


    @Override
    public String get(T t) {
        //move references to stack to do it faster
        String[] items = this.items;
        IVarGetter<T>[] getters = this.getters;
        int[][] gPositions = this.gPositions;
        int[][] iPositions = this.iPositions;

        String[] all = new String[size];

        for(int i = 0, l = getters.length; i < l; i++){
            Object result1 = getters[i].get(t);
            String result;
            if(result1 == null) result = "null";
            else                result = result1.toString();

            for (int pos: gPositions[i]) all[pos] = result;
        }

        for(int i = 0, l = items.length; i < l; i++){
            String result = items[i];
            for (int pos: iPositions[i]) all[pos] = result;
        }

        //use unsafe to merge stings faster
        return UnsafeUtils.mergeStringsFast(all);
    }


    @Override
    public boolean isConst() {
        for (IVarGetter<T> getter:getters)
            if(!getter.isConst())return false;
        return true;
    }


    /**
     * Try to simplify formatter
     * @return immutable copy or this
     */
    public IVarGetter<T> simplify() {
        try {
            return new ConstVarGetter<>(get(null));
        } catch (Throwable e){
            e.printStackTrace();
            return this;
        }
    }
}