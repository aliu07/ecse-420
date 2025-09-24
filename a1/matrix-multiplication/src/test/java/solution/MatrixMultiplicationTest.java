package solution;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static solution.MatrixMultiplication.parallelMultiplyMatrix;
import static solution.MatrixMultiplication.sequentialMultiplyMatrix;;

class MatrixMultiplicationTest {

    private static final int NUMBER_THREADS = 4; // Adjust as needed

    @Test
    void testBasic2x2WithNegatives() {
        Double[][] a = { { 1.0, -2.0 }, { 3.0, 4.0 } };
        Double[][] b = { { -5.0, 6.0 }, { 7.0, -8.0 } };
        Double[][] expected = { { -19.0, 22.0 }, { 13.0, -14.0 } };

        Double[][] seq = sequentialMultiplyMatrix(a, b);
        Double[][] par = parallelMultiplyMatrix(a, b, NUMBER_THREADS);

        assertArrayEquals(expected, seq, "Sequential 2x2 with negatives");
        assertArrayEquals(expected, par, "Parallel 2x2 with negatives");
        assertArrayEquals(seq, par, "Sequential vs Parallel consistency");
    }

    @Test
    void testSingleElementMatrix() {
        Double[][] a = { { -7.5 } };
        Double[][] b = { { 4.2 } };
        Double[][] expected = { { -31.5 } };

        assertArrayEquals(expected, sequentialMultiplyMatrix(a, b), "Sequential 1x1 with decimals");
        assertArrayEquals(expected, parallelMultiplyMatrix(a, b, NUMBER_THREADS), "Parallel 1x1 with decimals");
    }

    @Test
    void testRectangularMatrices() {
        Double[][] a = { { 1.5, -2.0, 0.5 } };
        Double[][] b = { { 2.0, -1.0 }, { 0.0, 3.0 }, { 4.0, 2.0 } };
        Double[][] expected = { { 5.0, -6.5 } };

        assertArrayEquals(expected, sequentialMultiplyMatrix(a, b), "Sequential 1x3 * 3x2");
        assertArrayEquals(expected, parallelMultiplyMatrix(a, b, NUMBER_THREADS), "Parallel 1x3 * 3x2");
    }

    @Test
    void testZeroMatrixBehavior() {
        Double[][] a = { { 0.0, 0.0 }, { 0.0, 0.0 } };
        Double[][] b = { { 5.0, -3.0 }, { 2.0, 1.0 } };
        Double[][] expected = { { 0.0, 0.0 }, { 0.0, 0.0 } };

        assertArrayEquals(expected, sequentialMultiplyMatrix(a, b), "Sequential zero matrix A");
        assertArrayEquals(expected, parallelMultiplyMatrix(a, b, NUMBER_THREADS), "Parallel zero matrix A");

        Double[][] a2 = { { 1.0, 2.0 }, { 3.0, 4.0 } };
        Double[][] b2 = { { 0.0, 0.0 }, { 0.0, 0.0 } };
        assertArrayEquals(expected, sequentialMultiplyMatrix(a2, b2), "Sequential zero matrix B");
        assertArrayEquals(expected, parallelMultiplyMatrix(a2, b2, NUMBER_THREADS), "Parallel zero matrix B");
    }

    @Test
    void testIdentityMatrixBehavior() {
        Double[][] a = { { 1.0, 0.0 }, { 0.0, 1.0 } };
        Double[][] b = { { 7.0, -2.0 }, { 3.0, 4.0 } };
        Double[][] expected = { { 7.0, -2.0 }, { 3.0, 4.0 } };

        assertArrayEquals(expected, sequentialMultiplyMatrix(a, b), "Sequential identity matrix");
        assertArrayEquals(expected, parallelMultiplyMatrix(a, b, NUMBER_THREADS), "Parallel identity matrix");
    }

    @Test
    void testPrecisionWithLargeAndSmallNumbers() {
        Double[][] a = { { 0.000001, 1000000.0 } };
        Double[][] b = { { 1000000.0 }, { 0.000001 } };
        Double[][] expected = { { 2.0 } };

        assertArrayEquals(expected, sequentialMultiplyMatrix(a, b), "Sequential precision test");
        assertArrayEquals(expected, parallelMultiplyMatrix(a, b, NUMBER_THREADS), "Parallel precision test");
    }

    @Test
    void testDimensionMismatch() {
        Double[][] a = { { 1.0, 2.0 } };
        Double[][] b = { { 3.0 }, { 4.0 }, { 5.0 } };

        assertThrows(IllegalArgumentException.class, () -> sequentialMultiplyMatrix(a, b),
                "Sequential dimension mismatch");
        assertThrows(IllegalArgumentException.class, () -> parallelMultiplyMatrix(a, b, NUMBER_THREADS),
                "Parallel dimension mismatch");
    }
}
