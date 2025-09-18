import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MatrixMultiplication {

    private static final int NUMBER_THREADS = 8; // For theoretic optimum, set to number of cpu cores on machine
    private static final int[] MATRIX_SIZES = {
        100,
        200,
        500,
        1000,
        2000,
        3000,
        4000,
    };

    /**
     * Returns the result of a sequential matrix multiplication
     * The two matrices are randomly generated
     *
     * @param a is the first matrix
     * @param b is the second matrix
     * @return the result of the multiplication
     */
    public static Double[][] sequentialMultiplyMatrix(
        Double[][] a,
        Double[][] b
    ) {
        validateInputMatrices(a, b);

        Integer rows = a.length;
        Integer common = a[0].length;
        Integer cols = b[0].length;

        Double[][] bTranspose = computeTranspose(b);
        Double[][] res = new Double[rows][cols];

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
    public static Double[][] parallelMultiplyMatrix(
        Double[][] a,
        Double[][] b,
        Integer numThreads
    ) {
        validateInputMatrices(a, b);

        Integer rows = a.length;
        Integer cols = b[0].length;

        Double[][] bTranspose = computeTranspose(b);
        Double[][] res = new Double[rows][cols];

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        Future<?>[] futures = new Future<?>[rows];

        for (int r = 0; r < rows; r++) {
            futures[r] = executor.submit(
                new MatrixMultiplicationTask(r, a, bTranspose, res)
            );
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
    private static Double[][] generateRandomMatrix(int numRows, int numCols) {
        Double[][] matrix = new Double[numRows][numCols];

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
    private static Double[][] computeTranspose(Double[][] inputMatrix) {
        Integer M = inputMatrix.length;
        Integer N = inputMatrix[0].length;
        Double[][] transpose = new Double[N][M];

        for (int r = 0; r < M; r++) {
            for (int c = 0; c < N; c++) {
                transpose[c][r] = inputMatrix[r][c];
            }
        }

        return transpose;
    }

    /**
     * Validates whether two matrices are compatible for matrix multiplication.
     * For matrices A and B to be multiplied (A * B), the number of columns in A
     * must equal the number of rows in B.
     *
     * @param a the first matrix
     * @param b the second matrix
     * @throws IllegalArgumentException if matrices are null or incompatible for multiplication
     */
    private static void validateInputMatrices(Double[][] a, Double[][] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Input matrices cannot be null");
        }

        if (a.length == 0 || b.length == 0) {
            throw new IllegalArgumentException(
                "Input matrices cannot be empty"
            );
        }

        if (a[0] == null || b[0] == null) {
            throw new IllegalArgumentException("Matrix rows cannot be null");
        }

        int aColumns = a[0].length;
        int bRows = b.length;

        if (aColumns != bRows) {
            throw new IllegalArgumentException(
                String.format(
                    "Matrix dimensions incompatible for multiplication: " +
                        "A has %d columns but B has %d rows",
                    aColumns,
                    bRows
                )
            );
        }

        // Validate that all rows in matrix A have the same number of columns
        for (int i = 0; i < a.length; i++) {
            if (a[i] == null || a[i].length != aColumns) {
                throw new IllegalArgumentException(
                    String.format(
                        "Matrix A row %d has inconsistent dimensions",
                        i
                    )
                );
            }
        }

        // Validate that all rows in matrix B have the same number of columns
        int bColumns = b[0].length;
        for (int i = 0; i < b.length; i++) {
            if (b[i] == null || b[i].length != bColumns) {
                throw new IllegalArgumentException(
                    String.format(
                        "Matrix B row %d has inconsistent dimensions",
                        i
                    )
                );
            }
        }
    }

    /**
     * Compares two matrices for equality and prints test results.
     * Matrices are considered equal if they have the same dimensions and
     * all corresponding elements are within a tolerance of 0.001.
     *
     * @param expected the expected matrix values
     * @param actual the actual matrix values to compare
     * @param testName the name of the test for display purposes
     */
    private static void assertEqual(
        Double[][] expected,
        Double[][] actual,
        String testName
    ) {
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
                            errorMsg = String.format(
                                "Value mismatch at [%d][%d]: expected %.3f, got %.3f",
                                i,
                                j,
                                expected[i][j],
                                actual[i][j]
                            );
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
            System.out.println(
                "✗ " +
                    testName +
                    " FAILED: Expected exception but none was thrown"
            );
        } catch (Exception e) {
            System.out.println(
                "✓ " +
                    testName +
                    " PASSED: Caught expected exception - " +
                    e.getClass().getSimpleName()
            );
        }
    }

    private static void runTests() {
        System.out.println("\n--- Running Test Suite ---");
        int testCount = 0;

        // Test 1: Core functionality - Basic 2x2 with mixed positive/negative values
        testCount++;
        Double[][] a1 = { { 1.0, -2.0 }, { 3.0, 4.0 } };
        Double[][] b1 = { { -5.0, 6.0 }, { 7.0, -8.0 } };
        Double[][] expected1 = { { -19.0, 22.0 }, { 13.0, -14.0 } };

        Double[][] seq1 = sequentialMultiplyMatrix(a1, b1);
        Double[][] par1 = parallelMultiplyMatrix(a1, b1, NUMBER_THREADS);
        assertEqual(
            expected1,
            seq1,
            "Test " + testCount + "a: Sequential 2x2 with negatives"
        );
        assertEqual(
            expected1,
            par1,
            "Test " + testCount + "b: Parallel 2x2 with negatives"
        );
        assertEqual(
            seq1,
            par1,
            "Test " + testCount + "c: Sequential vs Parallel consistency"
        );

        // Test 2: Edge case - Single element (1x1)
        testCount++;
        Double[][] a2 = { { -7.5 } };
        Double[][] b2 = { { 4.2 } };
        Double[][] expected2 = { { -31.5 } };

        assertEqual(
            expected2,
            sequentialMultiplyMatrix(a2, b2),
            "Test " + testCount + "a: Sequential 1x1 with decimals"
        );
        assertEqual(
            expected2,
            parallelMultiplyMatrix(a2, b2, NUMBER_THREADS),
            "Test " + testCount + "b: Parallel 1x1 with decimals"
        );

        // Test 3: Rectangular matrices - Multiple scenarios in one
        testCount++;
        // 1x3 * 3x2 = 1x2
        Double[][] a3 = { { 1.5, -2.0, 0.5 } };
        Double[][] b3 = { { 2.0, -1.0 }, { 0.0, 3.0 }, { 4.0, 2.0 } };
        Double[][] expected3 = { { 5.0, -6.5 } };

        assertEqual(
            expected3,
            sequentialMultiplyMatrix(a3, b3),
            "Test " + testCount + "a: Sequential 1x3 * 3x2"
        );
        assertEqual(
            expected3,
            parallelMultiplyMatrix(a3, b3, NUMBER_THREADS),
            "Test " + testCount + "b: Parallel 1x3 * 3x2"
        );

        // Test 4: Zero matrix behavior
        testCount++;
        Double[][] a4 = { { 0.0, 0.0 }, { 0.0, 0.0 } };
        Double[][] b4 = { { 5.0, -3.0 }, { 2.0, 1.0 } };
        Double[][] expected4 = { { 0.0, 0.0 }, { 0.0, 0.0 } };

        assertEqual(
            expected4,
            sequentialMultiplyMatrix(a4, b4),
            "Test " + testCount + "a: Sequential zero matrix A"
        );
        assertEqual(
            expected4,
            parallelMultiplyMatrix(a4, b4, NUMBER_THREADS),
            "Test " + testCount + "b: Parallel zero matrix A"
        );

        // Zero matrix B
        Double[][] a4b = { { 1.0, 2.0 }, { 3.0, 4.0 } };
        Double[][] b4b = { { 0.0, 0.0 }, { 0.0, 0.0 } };
        assertEqual(
            expected4,
            sequentialMultiplyMatrix(a4b, b4b),
            "Test " + testCount + "c: Sequential zero matrix B"
        );
        assertEqual(
            expected4,
            parallelMultiplyMatrix(a4b, b4b, NUMBER_THREADS),
            "Test " + testCount + "d: Parallel zero matrix B"
        );

        // Test 5: Identity matrix behavior
        testCount++;
        Double[][] a5 = { { 1.0, 0.0 }, { 0.0, 1.0 } }; // Identity matrix
        Double[][] b5 = { { 7.0, -2.0 }, { 3.0, 4.0 } };
        Double[][] expected5 = { { 7.0, -2.0 }, { 3.0, 4.0 } }; // Should equal B

        assertEqual(
            expected5,
            sequentialMultiplyMatrix(a5, b5),
            "Test " + testCount + "a: Sequential identity matrix"
        );
        assertEqual(
            expected5,
            parallelMultiplyMatrix(a5, b5, NUMBER_THREADS),
            "Test " + testCount + "b: Parallel identity matrix"
        );

        // Test 6: Large precision decimals and small numbers
        testCount++;
        Double[][] a6 = { { 0.000001, 1000000.0 } };
        Double[][] b6 = { { 1000000.0 }, { 0.000001 } };
        Double[][] expected6 = { { 2.0 } };

        assertEqual(
            expected6,
            sequentialMultiplyMatrix(a6, b6),
            "Test " + testCount + "a: Sequential precision test"
        );
        assertEqual(
            expected6,
            parallelMultiplyMatrix(a6, b6, NUMBER_THREADS),
            "Test " + testCount + "b: Parallel precision test"
        );

        // Test 7: Dimension mismatch scenarios
        testCount++;
        Double[][] a7 = { { 1.0, 2.0 } };
        Double[][] b7 = { { 3.0 }, { 4.0 }, { 5.0 } };

        assertThrows(
            "Test " + testCount + "a: Sequential dimension mismatch",
            () -> sequentialMultiplyMatrix(a7, b7)
        );
        assertThrows(
            "Test " + testCount + "b: Parallel dimension mismatch",
            () -> parallelMultiplyMatrix(a7, b7, NUMBER_THREADS)
        );

        System.out.println(
            "--- Test Suite Complete: " +
                testCount +
                " test groups executed ---\n"
        );
    }

    private static Double[][] benchmarkSequential(Double[][] a, Double[][] b) {
        long startSeq = System.nanoTime();
        Double[][] res = sequentialMultiplyMatrix(a, b);
        long endSeq = System.nanoTime();

        double elapsedSeqMs = (endSeq - startSeq) / 1_000_000.0;
        System.out.printf("Sequential multiply took %.3f ms%n", elapsedSeqMs);

        return res;
    }

    private static Double[][] benchmarkParallel(
        Double[][] a,
        Double[][] b,
        Integer numThreads
    ) {
        long startSeq = System.nanoTime();
        Double[][] res = parallelMultiplyMatrix(a, b, numThreads);
        long endSeq = System.nanoTime();

        double elapsedSeqMs = (endSeq - startSeq) / 1_000_000.0;
        System.out.printf("Parallel multiply took %.3f ms%n", elapsedSeqMs);

        return res;
    }

    private static void runBenchmarkByThreads() {
        System.out.println("\n--- Running Benchmark by Threads Suite ---");

        Integer matrixSize = 4000;
        Double[][] a = generateRandomMatrix(matrixSize, matrixSize);
        Double[][] b = generateRandomMatrix(matrixSize, matrixSize);

        // Store sequential result as control
        Double[][] seqRes = benchmarkSequential(a, b);

        for (int i = 1; i < 50; i++) {
            String testName = String.format(
                "[MATRIX SIZE = %d] [NUMBER OF THREADS = %d] ",
                matrixSize,
                i
            );
            System.out.print(testName);

            Double[][] parRes = benchmarkParallel(a, b, i);

            assertEqual(parRes, seqRes, testName);
        }
    }

    private static void runBenchmarkByMatrixSizes() {
        System.out.println("\n--- Running Benchmark by Matrix Size Suite ---");

        for (int i = 0; i < MATRIX_SIZES.length; i++) {
            Integer size = MATRIX_SIZES[i];
            String testName = String.format(
                "Benchmark %d: Matrix size = %d",
                i,
                size
            );
            System.out.println(testName);

            Double[][] a = generateRandomMatrix(size, size);
            Double[][] b = generateRandomMatrix(size, size);

            Double[][] seqRes = benchmarkSequential(a, b);
            Double[][] parRes = benchmarkParallel(a, b, NUMBER_THREADS);

            assertEqual(seqRes, parRes, testName);
        }
    }

    public static void main(String[] args) {
        runTests();
        // runBenchmarkByThreads();
        runBenchmarkByMatrixSizes();
    }
}

class MatrixMultiplicationTask implements Runnable {

    Integer rowIx; // Index of row in matrix A
    Double[][] a;
    Double[][] b;
    Double[][] res;

    public MatrixMultiplicationTask(
        Integer rowIx,
        Double[][] a,
        Double[][] b,
        Double[][] res
    ) {
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
