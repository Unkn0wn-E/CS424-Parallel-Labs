#include <iostream>
#include <omp.h>

void q1_2_helloWorld()
{
  int my_rank, total_threads;
  # pragma omp parallel
  {
    my_rank = omp_get_thread_num();
    total_threads = omp_get_num_threads();
    printf("Hello from thread %d of %d\n", my_rank, total_threads);
  }
}

void q3_4_5_vecAddition()
{
  const int N = 100000;
  int vec[N] = {};
  for (int i = 0; i < N; i++) vec[i] = 1;
  
  int global_sum = 0;

  # pragma omp parallel num_threads(100)
  {
    int rank, total_threads;
    int local_sum = 0;
    rank = omp_get_thread_num();
    total_threads = omp_get_num_threads();

    # pragma omp for
    for (int i = 0; i < N; i++)
    {
      local_sum += vec[i];
    }

    # pragma omp atomic
    global_sum += local_sum;
  }

  printf("\nSum = %d\n", global_sum);
}

void q6_7_vecAdditionReduction()
{
  const int N = 1000;
  int vec[N] = {};
  for (int i = 0; i < N; i++) vec[i] = 1;

  long global_sum = 0;

  # pragma omp parallel for num_threads(100) reduction(+:global_sum)
  for (int i = 0; i < N; i++) global_sum += vec[i];

  printf("\nSum = %d\n", global_sum);
}

int main()
{
  double start, stop;
  start = omp_get_wtime();

  //q1_2_helloWorld();
  //q3_4_5_vecAddition();
  //q6_7_vecAdditionReduction();

  stop = omp_get_wtime() - start;
  printf("\nTime taken: %.10fs\n", stop);
}