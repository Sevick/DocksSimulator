package com.fbytes.docksimulator.stats;

import com.fbytes.docksimulator.model.StatsData;
import com.fbytes.docksimulator.stats.StatsAgg;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Created by S on 04.09.2016.
 */
public class StatsAggCollector<T extends StatsData> implements Collector<T,StatsAgg,StatsAgg> {

    private StatsAgg statsAgg=new StatsAgg();

    @Override
    public Supplier<StatsAgg> supplier() {
        return StatsAgg::new;
    }

    @Override
    public BiConsumer<StatsAgg, T> accumulator() {
        return (StatsAgg,statsData) -> {
            StatsAgg.accept(statsData);
        };
    }

    @Override
    public BinaryOperator<StatsAgg> combiner() {
        return (left,right) -> {
            left.andThen(right);
            return left;
        };
    }

    @Override
    public Function<StatsAgg, StatsAgg> finisher() {
        return (statsAggParam) ->{
            return statsAggParam;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {

        return EnumSet.of(Characteristics.UNORDERED);
    }
}
