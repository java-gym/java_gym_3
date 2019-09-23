package noedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.common.value.qual.MinLen;

public final class DataStream {

    private int index;

    @MinLen(1)
    @Nonnull
    private final List<Data> data;

    private DataStream(@Nonnull Data... data) {
        this.index = 0;
        this.data = Arrays.asList(data);
    }

    DataStream(@Nonnull Integer... data) {
        this(Arrays.stream(data)
                .map(Data::of)
                .<Data>toArray(Data[]::new)
        );
    }

    @Nonnull
    public Optional<Data> next() {
        if (index >= data.size()) {
            return Optional.empty();
        }
        var item = data.get(index);
        index++;
        return Optional.of(item);
    }

    @Nonnull
    static DataStream range(@NonNegative int start, @NonNegative int end, int step) {
        Validate.isTrue((step > 0 && end >= start) || (step < 0 && end >= start));
        var items = new ArrayList<Data>();
        for (int i = start; i < end; i += step) {
            items.add(Data.of(i));
        }
        return new DataStream(items.toArray(new Data[0]));
    }

    @Nonnull
    static DataStream rep(@NonNegative int value, @Positive int reps) {
        var items = new Data[reps];
        var dataItem = Data.of(value);
        for (int i = 0; i < reps; i++) {
            items[i] = dataItem;
        }
        return new DataStream(items);
    }

    @Nonnull
    DataStream join(@Nonnull DataStream other) {
        var items = new ArrayList<Data>(this.data.size() + other.data.size());
        items.addAll(this.data);
        items.addAll(other.data);
        return new DataStream(items.toArray(new Data[0]));
    }
}
