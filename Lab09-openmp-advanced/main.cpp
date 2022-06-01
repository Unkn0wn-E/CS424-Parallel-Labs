#include <iostream>
#include <omp.h>

void q2_3_piComuptation()
{
  const int N = 1000000;
  double pi = 0;

  # pragma omp parallel
  {
    double sign = 1.0;

    # pragma omp for reduction(+:pi)
    for (int i = 0; i < N; i++)
    {
      sign = (i % 2 == 0) ? 1.0 : -1.0;
      pi += sign / (2 * i + 1);
    }
  }

  printf("PI = %.10f", pi * 4.0);
}

int main()
{
  double start, stop;
  start = omp_get_wtime();

  q2_3_piComuptation();

  stop = omp_get_wtime() - start;
  printf("\nTime taken in %.10fs\n", stop);
}