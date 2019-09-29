package noedit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import solution.Solution;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class SolutionTest {

    @Nonnull
    Solution storeWithSingleLookup(@Nonnull Registers registers, @Nonnull DataStream dataStream, @Nonnull Data searchItem, @Nullable Integer expectedRegister) {
        var solution = new Solution(registers);
        while (true) {
            var dataItem = dataStream.next();
            if (dataItem.isEmpty()) {
                break;
            }
            solution.store(dataItem.get());
        }
        var answer = solution.lookup(searchItem);
        if (expectedRegister == null) {
            assertTrue(answer.isEmpty());
        } else {
            assertTrue(answer.isPresent());
            assertEquals(expectedRegister, answer.get());
        }
        return solution;
    }

    @Test
    void testCompletesWithoutErrors() {
        var dataStream = DataStream.range(0, 40, 1)
                .join(DataStream.range(0, 40, 2))
                .join(DataStream.of(2, 7, 15, 33, 100));
        var searchItem = Data.of(14);
        var registers = new Registers(2, 4, 3);
        var solution = new Solution(registers);
        while (true) {
            var dataItem = dataStream.next();
            if (dataItem.isEmpty()) {
                break;
            }
            solution.store(dataItem.get());
        }
        var answer = solution.lookup(searchItem);
        assertTrue(answer.isEmpty());
    }

    @Test
    void testAllIdenticalData() {
        var dataStream = DataStream.rep(14, 100);
        var searchItem = Data.of(14);
        var registers = new Registers(2, 4, 3);
        storeWithSingleLookup(registers, dataStream, searchItem, 0);
    }

    @Test
    void testNotEncountered() {
        var dataStream = DataStream.range(10, 110, 1);
        var searchItem = Data.of(9);
        var registers = new Registers(2, 4, 3, 7);
        storeWithSingleLookup(registers, dataStream, searchItem, null);
    }

    @Test
    void testDroppedOutOfCache() {
        var dataStream = DataStream.range(10, 2 * 4 * 3 + 11, 1);
        var searchItem = Data.of(10);
        var registers = new Registers(2, 4, 3);
        storeWithSingleLookup(registers, dataStream, searchItem, null);
    }

    @Test
    void testReencounterItemAsLast() {
        var dataStream = DataStream.range(0, 31, 2)
                .add(Data.of(4));
        var searchItem = Data.of(4);
        var registers = new Registers(2, 4, 3);
        storeWithSingleLookup(registers, dataStream, searchItem, 0);
    }

    @Test
    void testReencounterItemAndShift() {
        var dataStream = DataStream.range(0, 31, 2)
                .join(DataStream.range(0, 7, 2));
        var searchItem = Data.of(6);
        var registers = new Registers(2, 4, 3);
        storeWithSingleLookup(registers, dataStream, searchItem, 1);
    }

    @Test
    void testLookupAfterEachItem() {
        var registers = new Registers(1, 2, 3);
        var solution = new Solution(registers);
        solution.store(Data.of(1));
        assertEquals(Integer.valueOf(0), solution.lookup(Data.of(1)).get());
        solution.store(Data.of(1));
        assertEquals(Integer.valueOf(0), solution.lookup(Data.of(1)).get());
        assertEquals(Optional.empty(), solution.lookup(Data.of(4)));
        solution.store(Data.of(2));
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(1)).get());
        assertEquals(Integer.valueOf(0), solution.lookup(Data.of(2)).get());
        solution.store(Data.of(3));
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(1)).get());
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(2)).get());
        assertEquals(Integer.valueOf(0), solution.lookup(Data.of(3)).get());
        solution.store(Data.of(1));
        assertEquals(Integer.valueOf(0), solution.lookup(Data.of(1)).get());
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(2)).get());
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(3)).get());
        assertEquals(Optional.empty(), solution.lookup(Data.of(4)));
        solution.store(Data.of(4));
        solution.store(Data.of(5));
        solution.store(Data.of(6));
        solution.store(Data.of(7));
        assertEquals(Integer.valueOf(2), solution.lookup(Data.of(1)).get());
        assertEquals(Optional.empty(), solution.lookup(Data.of(2)));
        assertEquals(Integer.valueOf(2), solution.lookup(Data.of(3)).get());
        assertEquals(Integer.valueOf(2), solution.lookup(Data.of(4)).get());
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(5)).get());
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(6)).get());
        assertEquals(Integer.valueOf(0), solution.lookup(Data.of(7)).get());
        solution.store(Data.of(8));
        solution.store(Data.of(1));
        assertEquals(Integer.valueOf(0), solution.lookup(Data.of(1)).get());
        assertEquals(Optional.empty(), solution.lookup(Data.of(2)));
        assertEquals(Optional.empty(), solution.lookup(Data.of(3)));
        assertEquals(Integer.valueOf(2), solution.lookup(Data.of(4)).get());
        assertEquals(Integer.valueOf(2), solution.lookup(Data.of(5)).get());
        assertEquals(Integer.valueOf(2), solution.lookup(Data.of(6)).get());
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(7)).get());
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(8)).get());
    }

    @Test
    void testRecycleSeveralTimes() {
        var dataStream = DataStream.range(0, 12500, 1)
                .join(DataStream.range(0, 12500, 1))
                .join(DataStream.range(0, 12500, 1))
                .join(DataStream.range(0, 12500, 1))
                .join(DataStream.range(0, 12500, 1))
                .join(DataStream.range(0, 12500, 1))
                .join(DataStream.range(0, 12500, 1))
                .join(DataStream.range(0, 12500, 1))
                .reversed();
        var searchItem = Data.of(12499);
        var registers = new Registers(1000, 2000, 3000, 4000);
        Solution sol = storeWithSingleLookup(registers, dataStream, searchItem, 0);
        for (int i = 0; i < 1000; i += 11) {
            assertEquals(Integer.valueOf(0), sol.lookup(Data.of(i)).get());
        }
        for (int i = 1000; i < 3000; i += 11) {
            assertEquals(Integer.valueOf(1), sol.lookup(Data.of(i)).get());
        }
        for (int i = 3000; i < 6000; i += 11) {
            assertEquals(Integer.valueOf(2), sol.lookup(Data.of(i)).get());
        }
        for (int i = 6000; i < 10_000; i += 11) {
            assertEquals(Integer.valueOf(3), sol.lookup(Data.of(i)).get());
        }
        for (int i = 6000; i < 15_000; i += 11) {
            assertEquals(Integer.valueOf(3), sol.lookup(Data.of(i)).get());
        }
    }

    @Test
    void testBatchedData() {
        var dataStream = DataStream.rep(-1, 25)
                .join(DataStream.rep(0, 25))
                .join(DataStream.rep(1, 25))
                .join(DataStream.rep(2, 25))
                .join(DataStream.rep(3, 25))
                .join(DataStream.rep(4, 25))
                .join(DataStream.rep(5, 25))
                .join(DataStream.rep(6, 25))
                .join(DataStream.rep(7, 25))
                .join(DataStream.rep(8, 25))
                .join(DataStream.rep(9, 25));
        var searchItem = Data.of(0);
        var registers = new Registers(2, 4, 3);
        Solution sol = storeWithSingleLookup(registers, dataStream, searchItem, null);
        assertEquals(Optional.empty(), sol.lookup(Data.of(-1)));
        assertEquals(Integer.valueOf(0), sol.lookup(Data.of(9)).get());
        assertEquals(Integer.valueOf(0), sol.lookup(Data.of(8)).get());
        assertEquals(Integer.valueOf(1), sol.lookup(Data.of(7)).get());
        assertEquals(Integer.valueOf(1), sol.lookup(Data.of(6)).get());
        assertEquals(Integer.valueOf(1), sol.lookup(Data.of(5)).get());
        assertEquals(Integer.valueOf(1), sol.lookup(Data.of(4)).get());
        assertEquals(Integer.valueOf(2), sol.lookup(Data.of(3)).get());
        assertEquals(Integer.valueOf(2), sol.lookup(Data.of(2)).get());
        assertEquals(Integer.valueOf(2), sol.lookup(Data.of(1)).get());
    }

    @Test
    void testLargeRegisters() {
        var dataStream = DataStream.range(0, 200000, 1);
        var searchItem = Data.of((200000-(200+4200))-42);
        var registers = new Registers(200, 4200, 200, 200000);
        storeWithSingleLookup(registers, dataStream, searchItem, 2);
    }

    @Test
    void testLargeStream() {
        var dataStream = DataStream.range(0, 5_000_000, 1);
        var searchItem = Data.of(5_000_000-440);
        var registers = new Registers(12, 423, 20, 513);
        storeWithSingleLookup(registers, dataStream, searchItem, 2);
    }

    @Test
    void testLotsOfSmallRegistersWithoutRollover() {
        var dataStream = DataStream.range(0, 6, 1);
        var searchItem = Data.of(0);
        var registers = new Registers(1, 1, 1, 1, 1, 1);
        storeWithSingleLookup(registers, dataStream, searchItem, 5);
    }

    @Test
    void testLotsOfSmallRegistersWithRollover() {
        var dataStream = DataStream.range(0, 10, 1);
        var searchItem = Data.of(4);
        var registers = new Registers(1, 1, 1, 1, 1, 1);
        storeWithSingleLookup(registers, dataStream, searchItem, 5);
    }

    @Test
    void testFullRegisterContent() {
        var dataStream = DataStream.range(0, 200, 1);
        var registers = new Registers(2,3,4);

        var solution = new Solution(registers);
        while (true) {
            var dataItem = dataStream.next();
            if (dataItem.isEmpty()) {
                break;
            }
            solution.store(dataItem.get());
        }

        assertEquals((Integer) 0, solution.lookup(Data.of(199)).get());
        assertEquals((Integer) 0, solution.lookup(Data.of(198)).get());
        assertEquals((Integer) 1, solution.lookup(Data.of(197)).get());
        assertEquals((Integer) 1, solution.lookup(Data.of(196)).get());
        assertEquals((Integer) 1, solution.lookup(Data.of(195)).get());
        assertEquals((Integer) 2, solution.lookup(Data.of(194)).get());
        assertEquals((Integer) 2, solution.lookup(Data.of(193)).get());
        assertEquals((Integer) 2, solution.lookup(Data.of(192)).get());
        assertEquals((Integer) 2, solution.lookup(Data.of(191)).get());
    }
}
