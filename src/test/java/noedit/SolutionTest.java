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
                .join(DataStream.range(0, 7, 2).reversed());
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
        Solution sol = storeWithSingleLookup(registers, dataStream, searchItem, null);
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
        for (int i = 10_000; i < 15_000; i += 11) {
            assertTrue(sol.lookup(Data.of(i)).isEmpty());
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

    @Test
    void testDuplicateDataConsecutive() {
        var dataStream = DataStream.of(3, 1, 1, 2);
        var registers = new Registers(1, 1, 1);
        Solution solution = storeWithSingleLookup(registers, dataStream, Data.of(4), null);

        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(1)).get());
        assertEquals(Integer.valueOf(0), solution.lookup(Data.of(2)).get());
        assertEquals(Integer.valueOf(2), solution.lookup(Data.of(3)).get());
    }

    @Test
    void testDuplicateDataSpread() {
        var dataStream = DataStream.of(1, 3, 1, 2);
        var registers = new Registers(1, 1, 1);
        Solution solution = storeWithSingleLookup(registers, dataStream, Data.of(4), null);

        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(1)).get());
        assertEquals(Integer.valueOf(0), solution.lookup(Data.of(2)).get());
        assertEquals(Integer.valueOf(2), solution.lookup(Data.of(3)).get());
    }

    @Test
    void testDuplicateDataMore() {
        var dataStream = DataStream.of(1, 3, 4, 1, 1, 1, 2, 3);
        var registers = new Registers(1, 1, 1);
        Solution solution = storeWithSingleLookup(registers, dataStream, Data.of(5), null);

        assertEquals(Integer.valueOf(2), solution.lookup(Data.of(1)).get());
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(2)).get());
        assertEquals(Integer.valueOf(0), solution.lookup(Data.of(3)).get());
        assertEquals(Integer.valueOf(3), solution.lookup(Data.of(4)).get());
    }

    @Test
    void testDuplicateLotsOfRandomItems() {
        var dataStream = DataStream.of(80, 35, 77, 82, 14, 39, 66, 9, 79, 6, 74, 51, 92, 48, 42, 84, 68,
                11, 5, 73, 81, 22, 75, 67, 32, 57, 78, 43, 10, 97, 83, 21, 38, 28, 23, 88, 90, 96, 49,
                58, 50, 18, 59, 45, 37, 1, 1, 1, 1, 27, 15, 53, 46, 17, 56, 76, 30, 63, 33, 64, 31, 86,
                95, 72, 64, 53, 38, 71, 58, 51, 84, 39, 91, 77, 85, 14, 66, 7, 11, 55, 10, 22, 92, 1,
                76, 37, 27, 16, 2, 29, 88, 9, 89, 36, 45, 8, 46, 21, 62, 57, 47, 41, 17, 32, 73, 3, 52,
                97, 61, 79, 44, 54, 63, 24, 26, 80, 30, 5, 34, 96, 31, 50, 67, 78, 18, 20, 93, 6, 28,
                75, 69, 83, 19, 42, 59, 12, 60, 90, 68, 98, 74, 48, 43, 82, 100, 35, 56, 41, 52, 63,
                31, 39, 27, 93, 2, 67, 14, 98, 73, 71, 65, 12, 17, 90, 86, 87, 61, 48, 26, 92, 85, 74,
                5, 99, 81, 70, 75, 6, 35, 36, 22, 10, 64, 21, 80, 57, 44, 24, 72, 30, 66, 53, 84, 18,
                79, 4, 47, 7, 96, 43, 49, 95, 55, 15, 45, 77, 37, 100, 19, 20, 97, 62, 58, 83, 59, 69,
                32, 34, 94, 13, 88, 29, 33, 25, 46, 76, 8, 89, 65, 81, 49, 25, 4, 40, 36, 87, 60, 86,
                85, 4, 29, 61, 94, 95, 44, 7, 89, 25, 8, 52, 70, 2, 47, 72, 91, 99, 93, 10, 35, 42, 43,
                54, 94, 83, 31, 87, 51, 98, 92, 95, 23, 61, 48, 12, 89, 40, 86, 63, 41, 21, 82, 71, 13,
                74, 16, 53, 15, 2, 79, 50, 58, 90, 8, 96, 3, 28, 84, 5, 69, 33, 62, 37, 18, 45, 68, 80,
                17, 67, 32, 34, 38, 46, 77, 60, 22, 78, 29, 49, 70, 76, 47, 44, 30, 9, 55, 39, 56, 7,
                64, 65, 85, 91, 24, 93, 11, 19, 88, 16, 20, 41, 55, 26, 54, 98, 24, 65);
        var registers = new Registers(2, 5, 1, 1, 8, 11, 12, 9, 2, 8, 1);
        Solution solution = storeWithSingleLookup(registers, dataStream, Data.of(0), null);

        assertEquals(Integer.valueOf(10), solution.lookup(Data.of(13)).get());
        assertEquals(Integer.valueOf(9), solution.lookup(Data.of(74)).get());
        assertEquals(Integer.valueOf(9), solution.lookup(Data.of(53)).get());
        assertEquals(Integer.valueOf(9), solution.lookup(Data.of(15)).get());
        assertEquals(Integer.valueOf(9), solution.lookup(Data.of(2)).get());
        assertEquals(Integer.valueOf(9), solution.lookup(Data.of(79)).get());
        assertEquals(Integer.valueOf(9), solution.lookup(Data.of(50)).get());
        assertEquals(Integer.valueOf(9), solution.lookup(Data.of(58)).get());
        assertEquals(Integer.valueOf(9), solution.lookup(Data.of(90)).get());
        assertEquals(Integer.valueOf(8), solution.lookup(Data.of(8)).get());
        assertEquals(Integer.valueOf(8), solution.lookup(Data.of(96)).get());
        assertEquals(Integer.valueOf(7), solution.lookup(Data.of(3)).get());
        assertEquals(Integer.valueOf(7), solution.lookup(Data.of(28)).get());
        assertEquals(Integer.valueOf(7), solution.lookup(Data.of(84)).get());
        assertEquals(Integer.valueOf(7), solution.lookup(Data.of(5)).get());
        assertEquals(Integer.valueOf(7), solution.lookup(Data.of(69)).get());
        assertEquals(Integer.valueOf(7), solution.lookup(Data.of(33)).get());
        assertEquals(Integer.valueOf(7), solution.lookup(Data.of(62)).get());
        assertEquals(Integer.valueOf(7), solution.lookup(Data.of(37)).get());
        assertEquals(Integer.valueOf(7), solution.lookup(Data.of(18)).get());
        assertEquals(Integer.valueOf(6), solution.lookup(Data.of(45)).get());
        assertEquals(Integer.valueOf(6), solution.lookup(Data.of(68)).get());
        assertEquals(Integer.valueOf(6), solution.lookup(Data.of(80)).get());
        assertEquals(Integer.valueOf(6), solution.lookup(Data.of(17)).get());
        assertEquals(Integer.valueOf(6), solution.lookup(Data.of(67)).get());
        assertEquals(Integer.valueOf(6), solution.lookup(Data.of(32)).get());
        assertEquals(Integer.valueOf(6), solution.lookup(Data.of(34)).get());
        assertEquals(Integer.valueOf(6), solution.lookup(Data.of(38)).get());
        assertEquals(Integer.valueOf(6), solution.lookup(Data.of(46)).get());
        assertEquals(Integer.valueOf(6), solution.lookup(Data.of(77)).get());
        assertEquals(Integer.valueOf(6), solution.lookup(Data.of(60)).get());
        assertEquals(Integer.valueOf(6), solution.lookup(Data.of(22)).get());
        assertEquals(Integer.valueOf(5), solution.lookup(Data.of(78)).get());
        assertEquals(Integer.valueOf(5), solution.lookup(Data.of(29)).get());
        assertEquals(Integer.valueOf(5), solution.lookup(Data.of(49)).get());
        assertEquals(Integer.valueOf(5), solution.lookup(Data.of(70)).get());
        assertEquals(Integer.valueOf(5), solution.lookup(Data.of(76)).get());
        assertEquals(Integer.valueOf(5), solution.lookup(Data.of(47)).get());
        assertEquals(Integer.valueOf(5), solution.lookup(Data.of(44)).get());
        assertEquals(Integer.valueOf(5), solution.lookup(Data.of(30)).get());
        assertEquals(Integer.valueOf(5), solution.lookup(Data.of(9)).get());
        assertEquals(Integer.valueOf(5), solution.lookup(Data.of(39)).get());
        assertEquals(Integer.valueOf(5), solution.lookup(Data.of(56)).get());
        assertEquals(Integer.valueOf(4), solution.lookup(Data.of(7)).get());
        assertEquals(Integer.valueOf(4), solution.lookup(Data.of(64)).get());
        assertEquals(Integer.valueOf(4), solution.lookup(Data.of(85)).get());
        assertEquals(Integer.valueOf(4), solution.lookup(Data.of(91)).get());
        assertEquals(Integer.valueOf(4), solution.lookup(Data.of(93)).get());
        assertEquals(Integer.valueOf(4), solution.lookup(Data.of(11)).get());
        assertEquals(Integer.valueOf(4), solution.lookup(Data.of(19)).get());
        assertEquals(Integer.valueOf(4), solution.lookup(Data.of(88)).get());
        assertEquals(Integer.valueOf(3), solution.lookup(Data.of(16)).get());
        assertEquals(Integer.valueOf(2), solution.lookup(Data.of(20)).get());
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(41)).get());
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(55)).get());
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(26)).get());
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(54)).get());
        assertEquals(Integer.valueOf(1), solution.lookup(Data.of(98)).get());
        assertEquals(Integer.valueOf(0), solution.lookup(Data.of(24)).get());
        assertEquals(Integer.valueOf(0), solution.lookup(Data.of(65)).get());
    }
}
