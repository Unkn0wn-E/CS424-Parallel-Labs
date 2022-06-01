#include <iostream>
#include <mpi.h>

int comm_sz, my_rank;

void q1_2_matrixMul()
{
    const int m = 4, n = 6;
    int A[m * n] = {
        1, 9, 4, 8, 3, 1,
        0, 6, 3, 8, 0, 5,
        0, 8, 4, 7, 9, 4,
        3, 8, 9, 1, 9, 6
    };
    int x[n] = {
        5,
        2,
        4,
        1,
        0,
        4
    };
    int y[m];
    double elapsed;

    const int local_m = m / comm_sz;
    double local_start, local_elapsed;
    int* local_A = new int[local_m * n];
    int* local_y = new int[local_m];
    MPI_Scatter(A, local_m * n, MPI_INT, local_A, local_m * n, MPI_INT, 0, MPI_COMM_WORLD);

    MPI_Barrier(MPI_COMM_WORLD);
    local_start = MPI_Wtime();

    for (int i = 0; i < local_m; i++) {
        local_y[i] = 0;
        for (int j = 0; j < n; j++) local_y[i] += local_A[i * n + j] * x[j];
    }

    local_elapsed = MPI_Wtime() - local_start;
    MPI_Reduce(&local_elapsed, &elapsed, 1, MPI_DOUBLE, MPI_MAX, 0, MPI_COMM_WORLD);

    MPI_Gather(local_y, local_m, MPI_INT, y, local_m, MPI_INT, 0, MPI_COMM_WORLD);

    if (my_rank == 0) {
        printf("Result:\n[\n");
        for (int cell : y) printf("%3d\n", cell);
        printf("]\n");
        printf("Time: %.10fs\n", elapsed);
    }

    delete[] local_A, local_y;
}

int main()
{
    MPI_Init(NULL, NULL);
    MPI_Comm_size(MPI_COMM_WORLD, &comm_sz);
    MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);

    q1_2_matrixMul();

    MPI_Finalize();
}