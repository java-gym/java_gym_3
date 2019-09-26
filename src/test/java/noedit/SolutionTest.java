package noedit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import solution.Solution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolutionTest {

    void storeWithSingleLookup(@Nonnull Registers registers, @Nonnull DataStream dataStream, @Nonnull Data searchItem, @Nullable Integer expectedRegister) {
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
    }

    @Test
    void testCompletesWithoutErrors() {
        var dataStream = DataStream.range(0, 40, 1)
                .join(DataStream.range(0, 40, 2))
                .join(new DataStream(2, 7, 15, 33, 100));
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
}
