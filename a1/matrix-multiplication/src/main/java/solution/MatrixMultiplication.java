package solution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import solution.tasks.MatrixMultiplicationTask;

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
            Double[][] b) {
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
            Integer numThreads) {
        validateInputMatrices(a, b);

        Integer rows = a.length;
        Integer cols = b[0].length;

        Double[][] bTranspose = computeTranspose(b);
        Double[][] res = new Double[rows][cols];

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        Future<?>[] futures = new Future<?>[rows];

        for (int r = 0; r < rows; r++) {
            futures[r] = executor.submit(
                    new MatrixMultiplicationTask(r, a, bTranspose, res));
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
     * @throws IllegalArgumentException if matrices are null or incompatible for
     *                                  multiplication
     */
    private static void validateInputMatrices(Double[][] a, Double[][] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Input matrices cannot be null");
        }

        if (a.length == 0 || b.length == 0) {
            throw new IllegalArgumentException(
                    "Input matrices cannot be empty");
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
                            bRows));
        }

        // Validate that all rows in matrix A have the same number of columns
        for (int i = 0; i < a.length; i++) {
            if (a[i] == null || a[i].length != aColumns) {
                throw new IllegalArgumentException(
                        String.format(
                                "Matrix A row %d has inconsistent dimensions",
                                i));
            }
        }

        // Validate that all rows in matrix B have the same number of columns
        int bColumns = b[0].length;
        for (int i = 0; i < b.length; i++) {
            if (b[i] == null || b[i].length != bColumns) {
                throw new IllegalArgumentException(
                        String.format(
                                "Matrix B row %d has inconsistent dimensions",
                                i));
            }
        }
    }

    /**
     * Compares two matrices for equality and prints test results.
     * Matrices are considered equal if they have the same dimensions and
     * all corresponding elements are within a tolerance of 0.001.
     *
     * @param expected the expected matrix values
     * @param actual   the actual matrix values to compare
     * @param testName the name of the test for display purposes
     */
    private static void assertEqual(
            Double[][] expected,
            Double[][] actual,
            String testName) {
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
                                    actual[i][j]);
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

    /**
     * Performs sequential matrix multiplication and measures execution time.
     * Prints the elapsed time in milliseconds to the console.
     *
     * @param a the first matrix
     * @param b the second matrix
     * @return the result of the matrix multiplication
     */
    private static Double[][] benchmarkSequential(Double[][] a, Double[][] b) {
        long startSeq = System.nanoTime();
        Double[][] res = sequentialMultiplyMatrix(a, b);
        long endSeq = System.nanoTime();

        double elapsedSeqMs = (endSeq - startSeq) / 1_000_000.0;
        System.out.printf("Sequential multiply took %.3f ms%n", elapsedSeqMs);

        return res;
    }

    /**
     * Performs parallel matrix multiplication and measures execution time.
     * Prints the elapsed time in milliseconds to the console.
     *
     * @param a          the first matrix
     * @param b          the second matrix
     * @param numThreads the number of threads to use for parallel execution
     * @return the result of the matrix multiplication
     */
    private static Double[][] benchmarkParallel(
            Double[][] a,
            Double[][] b,
            Integer numThreads) {
        long startSeq = System.nanoTime();
        Double[][] res = parallelMultiplyMatrix(a, b, numThreads);
        long endSeq = System.nanoTime();

        double elapsedSeqMs = (endSeq - startSeq) / 1_000_000.0;
        System.out.printf("Parallel multiply took %.3f ms%n", elapsedSeqMs);

        return res;
    }

    /**
     * Runs a benchmark suite that tests parallel matrix multiplication
     * performance across different thread counts (1-49 threads).
     * Uses a fixed matrix size of 4000x4000 and compares results
     * against sequential implementation for correctness.
     */
    @SuppressWarnings("unused")
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
                    i);
            System.out.print(testName);

            Double[][] parRes = benchmarkParallel(a, b, i);

            assertEqual(parRes, seqRes, testName);
        }
    }

    /**
     * Runs a benchmark suite that tests both sequential and parallel
     * matrix multiplication performance across different matrix sizes.
     * Uses the predefined MATRIX_SIZES array and compares results
     * for correctness while measuring execution times.
     */
    @SuppressWarnings("unused")
    private static void runBenchmarkByMatrixSizes() {
        System.out.println("\n--- Running Benchmark by Matrix Size Suite ---");

        for (int i = 0; i < MATRIX_SIZES.length; i++) {
            Integer size = MATRIX_SIZES[i];
            String testName = String.format(
                    "Benchmark %d: Matrix size = %d",
                    i,
                    size);
            System.out.println(testName);

            Double[][] a = generateRandomMatrix(size, size);
            Double[][] b = generateRandomMatrix(size, size);

            Double[][] seqRes = benchmarkSequential(a, b);
            Double[][] parRes = benchmarkParallel(a, b, NUMBER_THREADS);

            assertEqual(seqRes, parRes, testName);
        }
    }

    /**
     * Main method that executes the test suite and benchmarks.
     * Runs correctness tests followed by performance benchmarks
     * across different matrix sizes.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        // Benchmark by num of threads takes a very long time to run, uncomment if you
        // want
        // runBenchmarkByThreads();
        // Benchmark by matrix size takes a long time to run, uncomment if you want
        runBenchmarkByMatrixSizes();
    }
}