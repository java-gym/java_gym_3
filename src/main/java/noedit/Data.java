package noedit;

import javax.annotation.Nullable;

import org.checkerframework.checker.signedness.qual.SignedPositive;

public final class Data {

    @SignedPositive
    private final int nr;

    Data(int nr) {
        this.nr = nr;
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
        return String.format("#%05d}", nr);
    }
}
