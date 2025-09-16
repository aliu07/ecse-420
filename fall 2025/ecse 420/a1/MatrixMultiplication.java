package a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MatrixMultiplication {

    private static final int NUMBER_THREADS = 5;
    private static final int MATRIX_SIZE = 2000;

    /**
     * Returns the result of a sequential matrix multiplication
     * The two matrices are randomly generated
     * 
     * @param a is the first matrix
     * @param b is the second matrix
     * @return the result of the multiplication
     */
    public static double[][] sequentialMultiplyMatrix(
            double[][] a,
            double[][] b) {
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
    public static double[][] parallelMultiplyMatrix(
            double[][] a,
            double[][] b) {
        Integer rows = a.length;
        Integer common = a[0].length;
        Integer cols = b[0].length;

        double[][] bTranspose = computeTranspose(b);
        double[][] res = new double[rows][cols];
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
        Future<?>[] futures = new Future<?>[rows];

        for (int r = 0; r < rows; r++) {
            futures[r] = executor.submit(new MultiplyMatrixTask(r, rows, common, cols, a, bTranspose, res));
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
        double[][] transpose = new double[M][N];

        for (int r = 0; r < M; r++) {
            for (int c = 0; c < N; c++) {
                transpose[c][r] = inputMatrix[r][c];
            }
        }

        return transpose;
    }

    public static void main(String[] args) {
        double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
        double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);

        // --- Sequential ---
        long startSeq = System.nanoTime();
        double[][] resSeq = sequentialMultiplyMatrix(a, b);
        long endSeq = System.nanoTime();
        double elapsedSeqMs = (endSeq - startSeq) / 1_000_000.0;
        System.out.printf("Sequential multiply took %.3f ms%n", elapsedSeqMs);

        // --- Parallel ---
        long startPar = System.nanoTime();
        double[][] resPar = parallelMultiplyMatrix(a, b);
        long endPar = System.nanoTime();
        double elapsedParMs = (endPar - startPar) / 1_000_000.0;
        System.out.printf("Parallel multiply took %.3f ms%n", elapsedParMs);

        // Generate two random matrices, same size
        // double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
        // double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
        // sequentialMultiplyMatrix(a, b);
        // parallelMultiplyMatrix(a, b);

        // double[][] a = generateRandomMatrix(2, 3);
        // double[][] b = generateRandomMatrix(3, 2);
        // System.out.println(Arrays.deepToString(a));
        // System.out.println(Arrays.deepToString(b));
        // System.out.println(Arrays.deepToString(sequentialMultiplyMatrix(a, b)));
        // System.out.println(Arrays.deepToString(parallelMultiplyMatrix(a, b)));
    }
}

class MultiplyMatrixTask implements Runnable {
    Integer rowNum; // Index of row in matrix A
    Integer rows;
    Integer common;
    Integer cols;
    double[][] a;
    double[][] b;
    double[][] res;

    public MultiplyMatrixTask(int rowNum, Integer rows, Integer common, Integer cols, double[][] a, double[][] b,
            double[][] res) {
        this.rowNum = rowNum;
        this.rows = rows;
        this.common = common;
        this.cols = cols;
        this.a = a;
        this.b = b;
        this.res = res;
    }

    public void run() {
        for (int col = 0; col < cols; col++) {
            double sum = 0.0;

            for (int i = 0; i < common; i++) {
                sum += a[rowNum][i] * b[col][i];
            }

            res[rowNum][col] = sum;
        }
    }
}