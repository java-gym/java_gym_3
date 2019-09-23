package noedit;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

public final class DataStream {

    private int index;
    @Nonnull
    private final List<Data> data;

    DataStream(@Nonnull List<Data> data) {
        this.index = 0;
        this.data = data;
    }

    @Nonnull
    public Optional<Data> next() {
        if (index >= data.size()) {
            return Optional.empty();
        }
        Data item = data.get(index);
        index++;
        return Optional.of(item);
    }
}
