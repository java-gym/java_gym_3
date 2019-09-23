package noedit;

import java.util.List;

import javax.annotation.Nonnull;

import org.checkerframework.checker.index.qual.LengthOf;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.common.value.qual.MinLen;

/**
 * Represents data caches, that can hold a fixed number of items each.
 */
public final class Registers {

    @MinLen(1)
    @Nonnull
    private final List<Integer> registerSizes;

    Registers(@Nonnull Integer... registerSizes) {
        this.registerSizes = List.of(registerSizes);
    }

    @LengthOf("registerSizes")
    @Positive
    public int registerCount() {
        return registerSizes.size();
    }

    @Positive
    public int registerSize(int registerNumber) {
        return registerSizes.get(registerNumber);
    }
}
