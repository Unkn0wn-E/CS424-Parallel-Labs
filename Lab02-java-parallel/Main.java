import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.*;
import java.util.function.Function;

public class Main {
  public static void main(String ...args) throws Exception {
    // q1_helloWorld();

    // q2_vecAddition(0);
    // q2_vecAddition(1);

    // q2_matrixMul(0);
    // q2_matrixMul(1);
  }

  public static void q1_helloWorld() throws InterruptedException {
    final int P = 2;
    Function<Integer, Runnable> hello = rank -> () -> System.out.printf("Hello from thread %d out of %d\n", rank, P);
    Thread[] threads = new Thread[P];
    for (int i = 0; i < threads.length; i++) threads[i] = new Thread(hello.apply(i));
    for (Thread thread : threads) thread.start();
  }

  /**
   * @param option <p>0 -> Using Thread class</p> <p>1 -> Using Executor class</p>
   */
  public static void q2_vecAddition(int option) throws InterruptedException {
    final int P = 4;
    final int VEC_LENGTH = 6;
    int[] vec1 = new int[VEC_LENGTH];
    int[] vec2 = new int[VEC_LENGTH];
    int[] vecResult = new int[VEC_LENGTH];
    for (int i = 0; i < VEC_LENGTH; i++) {
      vec1[i] = i+1;
      vec2[i] = vec1[i] + VEC_LENGTH;
    }
    
    Function<Integer, Runnable> sum = rank -> () -> {
      int local_n = (int) Math.ceil((double) VEC_LENGTH/P);
      int local_start = rank * local_n;
      int local_stop = local_start + local_n;
      if (local_stop > VEC_LENGTH) local_stop = VEC_LENGTH;
      
      for (int i = local_start; i < local_stop; i++) vecResult[i] = vec1[i] + vec2[i];
    };

    long start = System.currentTimeMillis();
    if (option == 0) {
      Thread[] threads = new Thread[P];
      for (int i = 0; i < threads.length; i++) threads[i] = new Thread(sum.apply(i));
      for (Thread thread : threads) thread.start();
      for (Thread thread : threads) thread.join();
    } 
    else if (option == 1) {
      ExecutorService executor = Executors.newFixedThreadPool(P);
      for (int i = 0; i < P; i++) executor.execute(sum.apply(i));
      executor.shutdown();
      while(!executor.isTerminated());
    }
    long stop = System.currentTimeMillis() - start;

    String[] strings = new String[3];
    strings[0] = "x:\t";
    strings[1] = "y:\t";
    strings[2] = "z(x+y):\t";
    for (int i = 0; i < VEC_LENGTH; i++) {
      strings[0] += String.format("%2d ", vec1[i]);
      strings[1] += String.format("%2d ", vec2[i]);
      strings[2] += String.format("%2d ", vecResult[i]);
    }

    System.out.printf("%s\n%s\n%s\n", strings[0], strings[1], strings[2]);
    System.out.printf("Time taken: %dms\n", stop);
  }

  /**
   * @param option <p>0 -> Using Thread class</p> <p>1 -> Using Executor class</p>
   * @throws InterruptedException
   */
  public static void q2_matrixMul(int option) throws InterruptedException {
    final int P = 4;
    final int m = 12;
    final int n = 2;
    int[][] matrix = new int[m][n];
    int[] vec = new int[n];
    int[] result = new int[m];

    String[] strings = new String[3];
    strings[0] = "Matrix M:"+m+"*"+n;
    strings[1] = "Vector x:"+n;
    strings[2] = "Result M*x:";

    // Filling matrix&vector with numbers
    for (int i = 0; i < m; i++) {
      strings[0] += "\n";
      for (int j = 0; j < n; j++) {
        matrix[i][j] = (i*n+j) % 6 + 1;
        strings[0] += String.format("%3d ", matrix[i][j]);
      }
    }
    for (int i = 0; i < n; i++) {
      vec[i] = i+1;
      strings[1] += String.format("\n%3d", vec[i]);
    }

    final int N = m;
    Function<Integer, Runnable> multiply = rank -> () -> {
      int local_n = (int) Math.ceil((double) N/P);
      int local_start = rank * local_n;
      int local_stop = local_start + local_n;
      if (local_stop > N) local_stop = n;

      // System.out.printf("Thread %d is working.. start = %d, stop = %d\n", rank, local_start, local_stop);
      for (int i = local_start; i < local_stop; i++) {
        for (int j = 0; j < n; j++) {
          result[i] += matrix[i][j] * vec[j];
        }
      }
    };

    long start = System.currentTimeMillis();
    if (option == 0) {
      Thread[] threads = new Thread[P];
      for (int i = 0; i < threads.length; i++) threads[i] = new Thread(multiply.apply(i));
      for (Thread thread : threads) thread.start();
      for (Thread thread : threads) thread.join();
    }
    else if (option == 1) {
      ExecutorService executor = Executors.newFixedThreadPool(P);
      for (int i = 0; i < P; i++) executor.execute(multiply.apply(i));
      executor.shutdown();
      while(!executor.isTerminated());
    }
    long stop = System.currentTimeMillis() - start;

    strings[2] += "\n";
    for (int cell : result) strings[2] += String.format("%3d ", cell);
    System.out.printf("%s\n%s\n%s\n", strings[0], strings[1], strings[2]);
    System.out.printf("Time taken: %dms\n", stop);
  }
}