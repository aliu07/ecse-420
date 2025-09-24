package solution.tasks;

/**
 * Task class for parallel matrix multiplication that computes one row
 * of the result matrix. Implements Runnable to be executed by thread pool.
 */
public class MatrixMultiplicationTask implements Runnable {

    Integer rowIx; // Index of row in matrix A
    Double[][] a;
    Double[][] b;
    Double[][] res;

    /**
     * Constructs a matrix multiplication task for a specific row.
     *
     * @param rowIx the index of the row in matrix A to compute
     * @param a     the first matrix (matrix A)
     * @param b     the second matrix (matrix B, should be transposed)
     * @param res   the result matrix where computed values will be stored
     */
    public MatrixMultiplicationTask(
            Integer rowIx,
            Double[][] a,
            Double[][] b,
            Double[][] res) {
        this.rowIx = rowIx;
        this.a = a;
        this.b = b;
        this.res = res;
    }

    /**
     * Computes one row of the matrix multiplication result.
     * This method is called when the task is executed by a thread.
     * It calculates all column values for the assigned row index.
     */
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
