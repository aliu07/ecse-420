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
        Integer numThreads
    ) {
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
        System.out.println(
            "\n=========== Running Benchmark by Threads Suite ==========="
        );

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
        System.out.println(
            "\n=========== Running Benchmark by Matrix Size Suite ==========="
        );

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

            benchmarkSequential(a, b);
            benchmarkParallel(a, b, NUMBER_THREADS);
        }
    }

    /**
     * Helper function to convert a matrix to a readable string format
     * @param matrix the matrix to convert to string
     * @return formatted string representation of the matrix
     */
    private static String toString(Double[][] matrix) {
        if (matrix == null) return "null";

        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for (int i = 0; i < matrix.length; i++) {
            sb.append("  [");
            for (int j = 0; j < matrix[i].length; j++) {
                sb.append(String.format("%6.1f", matrix[i][j]));
                if (j < matrix[i].length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            if (i < matrix.length - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]");

        return sb.toString();
    }

    private static void runExample() {
        System.out.println(
            "=========== Matrix Multiplication Example ===========\n"
        );

        Double[][] a = { { 1.0, 2.0, 3.0 }, { 4.0, 5.0, 6.0 } };
        Double[][] b = { { 7.0, 8.0 }, { 9.0, 10.0 }, { 11.0, 12.0 } };

        System.out.println("Matrix A (2x3):");
        System.out.println(toString(a));
        System.out.println();

        System.out.println("Matrix B (3x2):");
        System.out.println(toString(b));
        System.out.println();

        System.out.println("Sequential multiplication result (A × B):");
        Double[][] sequentialResult = sequentialMultiplyMatrix(a, b);
        System.out.println(toString(sequentialResult));
        System.out.println();

        System.out.println("Parallel multiplication result (A × B):");
        Double[][] parallelResult = parallelMultiplyMatrix(a, b, 2);
        System.out.println(toString(parallelResult));
        System.out.println();
    }

    public static void main(String[] args) {
        // run example
        runExample();

        // benchmark by num of threads
        // runBenchmarkByThreads();

        // benchmark by matrix size
        // runBenchmarkByMatrixSizes();
    }
}
