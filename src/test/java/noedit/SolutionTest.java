package noedit;

import org.junit.jupiter.api.Test;

import solution.Solution;

public class SolutionTest {

    @Test
    void testSolutionCompletesWithoutErrors() {
        var dataStream = new DataStream(
                0,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10,
                11,
                12,
                13,
                14,
                15,
                16,
                17,
                18,
                19,
                20,
                21,
                22,
                23,
                24,
                25,
                26,
                27,
                28,
                29,
                30,
                31,
                32,
                33,
                34,
                35,
                36,
                37,
                38,
                39,
                40,
                0,
                2,
                4,
                6,
                8,
                10,
                12,
                14,
                16,
                18,
                20,
                22,
                24,
                26,
                28,
                30,
                32,
                34,
                36,
                38,
                40,
                15
        );
        var searchItem = new Data(14);
        var registers = new Registers();
        var solution = new Solution();
        solution.solve(registers, dataStream, searchItem);
    }
}
