package solution;

import java.util.Optional;

import javax.annotation.Nonnull;

import noedit.Data;
import noedit.Registers;

/**
 * This solution represents a special CPU cache manager. It can store and lookup data.
 *
 * It should work as follows:
 * <ol>
 * <li>When storing a value, it should go into the first register.
 * <li>If a register is full, the oldest item is pushed to the next register (which may in turn 'overflow').
 * <li>When looking up an item, if the item is in cache, the register number is returned. If it is not in cache
 * (either because it was never encountered, or because it dropped out of the last register), then an empty
 * optional is returned.
 * </ol>
 *
 * Operations have to be fast for large numbers of items (linear at worst). Bonus points for minimizing temporary (heap) data.
 *
 * (This is not really how CPU cache works).
 */
public class Solution {

    @Nonnull
    private final Registers registers;

    public Solution(@Nonnull Registers registers) {
        this.registers = registers;
    }

    /**
     * Store the item into the first register, overflowing as necessary.
     */
    @Nonnull
    public void store(@Nonnull Data storeItem) {
        //TODO: Implement your solution here
        throw new UnsupportedOperationException("solution not yet implemented");
    }

    /**
     * Return the register that an item if cached in, or empty if not cached.
     */
    @Nonnull
    public Optional<Integer> lookup(@Nonnull Data searchItem) {
        //TODO: Implement your solution here
        throw new UnsupportedOperationException("solution not yet implemented");
    }
}
