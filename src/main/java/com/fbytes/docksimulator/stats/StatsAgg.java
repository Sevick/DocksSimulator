package com.fbytes.docksimulator.stats;

import com.fbytes.docksimulator.model.StatsData;

import java.util.function.Consumer;

/**
 * Created by S on 04.09.2016.
 */
public class StatsAgg implements Consumer<StatsData> {


    long count;
    long sum;

    long minCargoLoad=Long.MAX_VALUE;
    long maxCargoLoad=0;

    public long getCount() {
        return count;
    }

    public long getSum() {
        return sum;
    }

    public long getMinCargoLoad() {
        return minCargoLoad;
    }

    public long getMaxCargoLoad() {
        return maxCargoLoad;
    }

    @Override
    public void accept(StatsData stats) {
        ++count;
        sum+=stats.getCargoLoadInteger();
        minCargoLoad=Math.min(minCargoLoad,stats.getCargoLoadInteger());
        maxCargoLoad=Math.max(maxCargoLoad,stats.getCargoLoadInteger());
    }

    @Override
    public Consumer<StatsData> andThen(Consumer<? super StatsData> after) {

        StatsAgg stats=(StatsAgg) after;
        count+=stats.count;
        sum+=stats.sum;
        minCargoLoad=Math.min(minCargoLoad,stats.minCargoLoad);
        maxCargoLoad=Math.max(maxCargoLoad,stats.maxCargoLoad);
        return this;
    }
}
