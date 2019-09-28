package noedit;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.checkerframework.checker.signedness.qual.SignedPositive;

public final class Data {

    private static final Map<Integer, Data> CACHE = new HashMap<>(16_384);

    @SignedPositive
    private final int nr;

    private Data(@SignedPositive int nr) {
        this.nr = nr;
    }

    @Nonnull
    static Data of(@SignedPositive int nr) {
        return CACHE.computeIfAbsent(nr, (Function<Integer, Data>)Data::new);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return nr == data.nr;
    }

    @Override
    public int hashCode() {
        return nr;
    }

    @Override
    public String toString() {
        return String.format("#%05d", nr);
    }
}
