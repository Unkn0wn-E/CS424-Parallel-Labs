import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

public class Main {
  public static void main(String ...args) throws Exception {
    // q1_piComputation(0);
    // q1_piComputation(1);
    // q1_piComputation(2);

    // q2_consumerProducer(0);
    // q2_consumerProducer(1);

    // ex1_factorial();
    // ex2_arrMax();
  }

  /**
   * 
   * @param option 
   * <p> 0 -> wihtout sync </p> 
   * <p> 1 -> with sync </p>
   * <p> 2 -> with locks </p>
   */
  public static void q1_piComputation(int option) {
    final int P = 50;
    final int N = 2000000000;
    double[] pi = {0}; // to make it modifiable by inner functions (i.e., globalSum)

    Lock lock = new ReentrantLock();
    Consumer<Double> globalSum = number -> {
      if (option == 0) {
        pi[0] += number;
      }
      else if (option == 1) {
        synchronized(Main.class) {
          pi[0] += number;
        }
      }
      else if (option == 2) {
        lock.lock();
        pi[0] += number;
        lock.unlock();
      }
    };
    Function<Integer, Runnable> func = rank -> () -> {
      int local_n = (int) Math.ceil(N/P);
      int local_start = rank * local_n;
      int local_stop = local_start + local_n;
      if (local_stop > N) local_stop = N;
      double local_sum = 0;

      double sign = 1;
      for (int i = local_start; i < local_stop; i++) {
        if (i % 2 == 0) sign = 1;
        else sign = -1;
        local_sum += (sign*1) / (2*i+1);
      }

      globalSum.accept(local_sum);
    };

    long start = System.currentTimeMillis();
    ExecutorService executor = Executors.newCachedThreadPool();
    for (int i = 0; i < P; i++) executor.execute(func.apply(i));
    executor.shutdown();
    while(!executor.isTerminated());
    long stop = System.currentTimeMillis() - start;

    System.out.printf("PI = %.15f\n", pi[0]*4);
    System.out.printf("Number of threads = %d\n", P);
    System.out.printf("Time taken = %dms\n", stop);
  }

  /**
   * 
   * @param option 
   * <p> 0 -> wihtout sync </p> 
   * <p> 1 -> with condition </p>
   */
  public static void q2_consumerProducer(int option) {
    final int P = 24;
    String[] inboxes = new String[P];
    
    Lock[] locks = new Lock[P];
    Condition[] conditions = new Condition[P];
    for (int i = 0; i < conditions.length; i++) {
      locks[i] = new ReentrantLock();
      conditions[i] = locks[i].newCondition();
    }

    Function<Integer, Runnable> sendAndReceive = rank -> () -> {
      int to   = Math.floorMod(rank+1, P);
      inboxes[to] = String.format("Hello to %2d from %2d", to, rank);

      System.out.printf("Thread %2d: %s\n", rank, inboxes[rank]);
    };

    Function <Integer, Runnable> sendAndReceive_condition = rank -> () -> {
      int to   = Math.floorMod(rank+1, P);
      locks[to].lock();
      inboxes[to] = String.format("Hello to %2d from %2d", to, rank);
      conditions[to].signal();
      locks[to].unlock();

      try {
        locks[rank].lock();
        if (inboxes[rank] == null) {
          System.out.printf("Thread %2d: waiting for message...\n", rank);
          conditions[rank].await();
        }
        System.out.printf("Thread %2d: %s\n", rank, inboxes[rank]);
      } 
      catch (InterruptedException e) {e.printStackTrace();}
      finally {locks[rank].unlock();}
    };

    Function <Integer, Runnable> func = sendAndReceive;
    if (option == 0) func = sendAndReceive;
    else if (option == 1) func = sendAndReceive_condition;

    ExecutorService executor = Executors.newFixedThreadPool(P);
    for (int i = 0; i < P; i++) executor.execute(func.apply(i));
    executor.shutdown();
    while(!executor.isTerminated());
  }

  public static void ex1_factorial() {
    final int P = 8;
    final int N = 20;
    long[] factorial = {1};
    Lock lock = new ReentrantLock();
    
    Function<Integer, Runnable> compute = rank -> () -> {
      int local_n = (int) Math.ceil((double) N/P);
      int local_start = rank * local_n;
      int local_stop  = local_start + local_n;
      if (local_stop > N) local_stop = N;
      long local_factorial = 1;

      for (int i = local_start; i < local_stop; i++) local_factorial *= i+1;
      
      lock.lock();
      factorial[0] *= local_factorial;
      lock.unlock();
    };

    long start = System.currentTimeMillis();
    ExecutorService executor = Executors.newFixedThreadPool(P);
    for (int i = 0; i < P; i++) executor.execute(compute.apply(i));
    executor.shutdown();
    while(!executor.isTerminated());
    long stop = System.currentTimeMillis() - start;

    System.out.printf("Factorial of %d = %d\n", N, factorial[0]);
    System.out.printf("Number of threads = %d\n", P);
    System.out.printf("Time taken = %dms\n", stop);
  }

  public static void ex2_arrMax() {
    Object syncLock = new Object();
    final int P = 24;
    final int N = 1000;
    Integer[] arr = new Integer[N];
    for (int i = 0; i < N; i++) arr[i] = (int) Math.round(N * Math.random());
    int[] globalMax = {arr[0]};

    Function<Integer, Runnable> findMax = rank -> () -> {
      int local_n = (int) Math.ceil((double) N/P);
      int local_start = rank * local_n;
      int local_stop = local_start + local_n;
      if (local_stop > N) local_stop = N;

      int local_max = arr[0];
      for (int i = local_start; i < local_stop; i++) {
        if (arr[i] > local_max) local_max = arr[i];
      }

      synchronized (syncLock) {
        if (local_max > globalMax[0]) globalMax[0] = local_max;
      }
    };

    long start = System.currentTimeMillis();
    ExecutorService executor = Executors.newFixedThreadPool(P);
    for (int i = 0; i < P; i++) executor.execute(findMax.apply(i));
    executor.shutdown();
    while(!executor.isTerminated());
    long stop = System.currentTimeMillis() - start;

    System.out.printf("Array = %s\n", Arrays.toString(arr));
    System.err.printf("Real Max = %d\n", Collections.max(Arrays.asList(arr)));
    System.out.printf("Parallel Max = %d\n", globalMax[0]);
    System.out.printf("Number of threads = %d\n", P);
    System.out.printf("Time taken = %dms\n", stop);
  }
}