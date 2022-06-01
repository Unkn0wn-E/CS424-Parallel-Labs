#include <iostream>
#include <mpi.h>

int comm_sz, my_rank;

void q1_sumRanks() 
{
    int result = 0;
    MPI_Reduce(&my_rank, &result, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);
    if (my_rank == 0) printf("Sum rank of %d processes is %d", comm_sz, result);
}

void q2_sumRanksBcast()
{
    int result = 0;
    MPI_Reduce(&my_rank, &result, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);
    MPI_Bcast(&result, 1, MPI_INT, 0, MPI_COMM_WORLD);
    printf("Process %2d: Sum rank of %d processes is %d", my_rank, comm_sz, result);
}

void q3_sumRanksAllreduce()
{
    int result = 0;
    MPI_Allreduce(&my_rank, &result, 1, MPI_INT, MPI_SUM, MPI_COMM_WORLD);
    printf("Process %2d: Sum rank of %d processes is %d", my_rank, comm_sz, result);
}

void q4_vecAddition()
{
    const int N = 8;
    int vec1[N] = { 1,2,3,4,5,6,7,8 };
    int vec2[N] = { 9,10,11,12,13,14,15,16 };
    int vec3[N] = {};

    const int local_n = N / comm_sz;
    int* local_vec1 = new int[local_n];
    int* local_vec2 = new int[local_n];
    int* local_vec3 = new int[local_n];
    MPI_Scatter(vec1, local_n, MPI_INT, local_vec1, local_n, MPI_INT, 0, MPI_COMM_WORLD);
    MPI_Scatter(vec2, local_n, MPI_INT, local_vec2, local_n, MPI_INT, 0, MPI_COMM_WORLD);
    for (int i = 0; i < local_n; i++) local_vec3[i] = local_vec1[i] + local_vec2[i];

    MPI_Gather(local_vec3, local_n, MPI_INT, vec3, local_n, MPI_INT, 0, MPI_COMM_WORLD);
    if (my_rank == 0) 
    {
        printf("Result:\n[");
        for (int cell : vec3) printf("%3d ", cell);
        printf("]\n");
    }

    delete[] local_vec1, local_vec2, local_vec3;
}

void q5_vecAdditionAllgather()
{
    const int N = 8;
    int vec1[N] = { 1,2,3,4,5,6,7,8 };
    int vec2[N] = { 9,10,11,12,13,14,15,16 };
    int vec3[N] = {};

    const int local_n = N / comm_sz;
    int* local_vec1 = new int[local_n];
    int* local_vec2 = new int[local_n];
    int* local_vec3 = new int[local_n];
    MPI_Scatter(vec1, local_n, MPI_INT, local_vec1, local_n, MPI_INT, 0, MPI_COMM_WORLD);
    MPI_Scatter(vec2, local_n, MPI_INT, local_vec2, local_n, MPI_INT, 0, MPI_COMM_WORLD);
    for (int i = 0; i < local_n; i++) local_vec3[i] = local_vec1[i] + local_vec2[i];

    MPI_Allgather(local_vec3, local_n, MPI_INT, vec3, local_n, MPI_INT, MPI_COMM_WORLD);

    printf("Process %2d Result:\n[", my_rank);
    for (int cell : vec3) printf("%3d ", cell);
    printf("]\n");

    delete[] local_vec1, local_vec2, local_vec3;
}

int main()
{
    MPI_Init(NULL, NULL);
    MPI_Comm_size(MPI_COMM_WORLD, &comm_sz);
    MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
    
    //q1_sumRanks();
    //q2_sumRanksBcast();
    //q3_sumRanksAllreduce();
    //q4_vecAddition();
    // q5_vecAdditionAllgather();

    MPI_Finalize();
}