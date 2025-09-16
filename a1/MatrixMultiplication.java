package a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MatrixMultiplication {

    private static final int NUMBER_THREADS = 2;
    private static final int MATRIX_SIZE = 2000;

    /**
     * Returns the result of a sequential matrix multiplication
     * The two matrices are randomly generated
     * 
     * @param a is the first matrix
     * @param b is the second matrix
     * @return the result of the multiplication
     */
    public static double[][] sequentialMultiplyMatrix(double[][] a, double[][] b) {
        Integer rows = a.length;
        Integer common = a[0].length;
        Integer cols = b[0].length;

        double[][] bTranspose = computeTranspose(b);
        double[][] res = new double[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double sum = 0.0;

                for (int i = 0; i < common; i++) {
                    sum += a[r][i] * bTranspose[c][i];
                }

                res[r][c] = sum;
            }
        }

        return res;
    }

    /**
     * Returns the result of a concurrent matrix multiplication
     * The two matrices are randomly generated
     * 
     * @param a is the first matrix
     * @param b is the second matrix
     * @return the result of the multiplication
     */
    public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b) {
        Integer rows = a.length;
        Integer cols = b[0].length;

        double[][] bTranspose = computeTranspose(b);
        double[][] res = new double[rows][cols];

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
        Future<?>[] futures = new Future<?>[rows];

        for (int r = 0; r < rows; r++) {
            futures[r] = executor.submit(new MultiplyMatrixTask(r, a, bTranspose, res));
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        executor.shutdown();
        return res;
    }

    /**
     * Populates a matrix of given size with randomly generated integers between
     * 0-10.
     * 
     * @param numRows number of rows
     * @param numCols number of cols
     * @return matrix
     */
    private static double[][] generateRandomMatrix(int numRows, int numCols) {
        double matrix[][] = new double[numRows][numCols];

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                matrix[row][col] = (double) ((int) (Math.random() * 10.0));
            }
        }

        return matrix;
    }

    /**
     * Returns the tranpose of a given matrix. This function is used to leverage
     * cache locality when performing matrix multiplications in order to boost
     * runtime performance.
     * 
     * @param inputMatrix is the input matrix whose transpose we want to compute
     * @return the result of the tranpose operation
     */
    private static double[][] computeTranspose(double[][] inputMatrix) {
        Integer M = inputMatrix.length;
        Integer N = inputMatrix[0].length;
        double[][] transpose = new double[N][M];

        for (int r = 0; r < M; r++) {
            for (int c = 0; c < N; c++) {
                transpose[c][r] = inputMatrix[r][c];
            }
        }

        return transpose;
    }

    public static void main(String[] args) {
        runTests();
        runBenchmarks();
    }

    private static void assertEqual(double[][] expected, double[][] actual, String testName) {
        boolean passed = true;
        String errorMsg = "";

        if (expected.length != actual.length) {
            passed = false;
            errorMsg = "Row count mismatch";
        } else {
            for (int i = 0; i < expected.length && passed; i++) {
                if (expected[i].length != actual[i].length) {
                    passed = false;
                    errorMsg = "Column count mismatch at row " + i;
                } else {
                    for (int j = 0; j < expected[i].length; j++) {
                        if (Math.abs(expected[i][j] - actual[i][j]) > 0.001) {
                            passed = false;
                            errorMsg = String.format("Value mismatch at [%d][%d]: expected %.3f, got %.3f",
                                    i, j, expected[i][j], actual[i][j]);
                            break;
                        }
                    }
                }
            }
        }

        if (passed) {
            System.out.println("✓ " + testName + " PASSED");
        } else {
            System.out.println("✗ " + testName + " FAILED: " + errorMsg);
        }
    }

    private static void assertThrows(String testName, Runnable operation) {
        try {
            operation.run();
            System.out.println("✗ " + testName + " FAILED: Expected exception but none was thrown");
        } catch (Exception e) {
            System.out
                    .println("✓ " + testName + " PASSED: Caught expected exception - " + e.getClass().getSimpleName());
        }
    }

    private static void runTests() {
        System.out.println("\n--- Running Test Suite ---");
        int testCount = 0;

        // Test 1: Core functionality - Basic 2x2 with mixed positive/negative values
        testCount++;
        double[][] a1 = { { 1.0, -2.0 }, { 3.0, 4.0 } };
        double[][] b1 = { { -5.0, 6.0 }, { 7.0, -8.0 } };
        double[][] expected1 = { { -19.0, 22.0 }, { 13.0, -14.0 } };

        double[][] seq1 = sequentialMultiplyMatrix(a1, b1);
        double[][] par1 = parallelMultiplyMatrix(a1, b1);
        assertEqual(expected1, seq1, "Test " + testCount + "a: Sequential 2x2 with negatives");
        assertEqual(expected1, par1, "Test " + testCount + "b: Parallel 2x2 with negatives");
        assertEqual(seq1, par1, "Test " + testCount + "c: Sequential vs Parallel consistency");

        // Test 2: Edge case - Single element (1x1)
        testCount++;
        double[][] a2 = { { -7.5 } };
        double[][] b2 = { { 4.2 } };
        double[][] expected2 = { { -31.5 } };

        assertEqual(expected2, sequentialMultiplyMatrix(a2, b2),
                "Test " + testCount + "a: Sequential 1x1 with decimals");
        assertEqual(expected2, parallelMultiplyMatrix(a2, b2), "Test " + testCount + "b: Parallel 1x1 with decimals");

        // Test 3: Rectangular matrices - Multiple scenarios in one
        testCount++;
        // 1x3 * 3x2 = 1x2
        double[][] a3 = { { 1.5, -2.0, 0.5 } };
        double[][] b3 = { { 2.0, -1.0 }, { 0.0, 3.0 }, { 4.0, 2.0 } };
        double[][] expected3 = { { 5.0, -6.5 } };

        assertEqual(expected3, sequentialMultiplyMatrix(a3, b3), "Test " + testCount + "a: Sequential 1x3 * 3x2");
        assertEqual(expected3, parallelMultiplyMatrix(a3, b3), "Test " + testCount + "b: Parallel 1x3 * 3x2");

        // Test 4: Zero matrix behavior
        testCount++;
        double[][] a4 = { { 0.0, 0.0 }, { 0.0, 0.0 } };
        double[][] b4 = { { 5.0, -3.0 }, { 2.0, 1.0 } };
        double[][] expected4 = { { 0.0, 0.0 }, { 0.0, 0.0 } };

        assertEqual(expected4, sequentialMultiplyMatrix(a4, b4), "Test " + testCount + "a: Sequential zero matrix A");
        assertEqual(expected4, parallelMultiplyMatrix(a4, b4), "Test " + testCount + "b: Parallel zero matrix A");

        // Zero matrix B
        double[][] a4b = { { 1.0, 2.0 }, { 3.0, 4.0 } };
        double[][] b4b = { { 0.0, 0.0 }, { 0.0, 0.0 } };
        assertEqual(expected4, sequentialMultiplyMatrix(a4b, b4b), "Test " + testCount + "c: Sequential zero matrix B");
        assertEqual(expected4, parallelMultiplyMatrix(a4b, b4b), "Test " + testCount + "d: Parallel zero matrix B");

        // Test 5: Identity matrix behavior
        testCount++;
        double[][] a5 = { { 1.0, 0.0 }, { 0.0, 1.0 } }; // Identity matrix
        double[][] b5 = { { 7.0, -2.0 }, { 3.0, 4.0 } };
        double[][] expected5 = { { 7.0, -2.0 }, { 3.0, 4.0 } }; // Should equal B

        assertEqual(expected5, sequentialMultiplyMatrix(a5, b5), "Test " + testCount + "a: Sequential identity matrix");
        assertEqual(expected5, parallelMultiplyMatrix(a5, b5), "Test " + testCount + "b: Parallel identity matrix");

        // Test 6: Large precision decimals and small numbers
        testCount++;
        double[][] a6 = { { 0.000001, 1000000.0 } };
        double[][] b6 = { { 1000000.0 }, { 0.000001 } };
        double[][] expected6 = { { 2.0 } };

        assertEqual(expected6, sequentialMultiplyMatrix(a6, b6), "Test " + testCount + "a: Sequential precision test");
        assertEqual(expected6, parallelMultiplyMatrix(a6, b6), "Test " + testCount + "b: Parallel precision test");

        // Test 7: Dimension mismatch scenarios
        testCount++;
        double[][] a7 = { { 1.0, 2.0 } };
        double[][] b7 = { { 3.0 }, { 4.0 }, { 5.0 } };

        assertThrows("Test " + testCount + "a: Sequential dimension mismatch",
                () -> sequentialMultiplyMatrix(a7, b7));
        assertThrows("Test " + testCount + "b: Parallel dimension mismatch",
                () -> parallelMultiplyMatrix(a7, b7));

        System.out.println("--- Test Suite Complete: " + testCount + " test groups executed ---\n");
    }
}

class MultiplyMatrixTask implements Runnable {
    Integer rowIx; // Index of row in matrix A
    double[][] a;
    double[][] b;
    double[][] res;

    public MultiplyMatrixTask(Integer rowIx, double[][] a, double[][] b, double[][] res) {
        this.rowIx = rowIx;
        this.a = a;
        this.b = b;
        this.res = res;
    }

    public void run() {
        Integer common = a[0].length;
        Integer cols = res[0].length;

        for (int col = 0; col < cols; col++) {
            double sum = 0.0;

            for (int i = 0; i < common; i++) {
                sum += a[rowIx][i] * b[col][i];
            }

            res[rowIx][col] = sum;
        }
    }
}