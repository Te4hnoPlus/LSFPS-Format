package plus.format;


import plus.format.utils.UnsafeUtils;

public class Formatter <T> implements IVarGetter.Str<T>{
    protected final String[] items;
    protected final IVarGetter<T>[] getters;
    protected final int[][] gPositions;
    protected final int[][] iPositions;
    protected final int size;

    public Formatter(String[] items, int[][] iPositions, IVarGetter<T>[] getters, int[][] gPositions, int size) {
        this.items = items;
        this.getters = getters;
        this.gPositions = gPositions;
        this.iPositions = iPositions;
        this.size = size;
    }


    @Override
    public String get(T t) {
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

        return UnsafeUtils.mergeStringsFast(all);
    }
}