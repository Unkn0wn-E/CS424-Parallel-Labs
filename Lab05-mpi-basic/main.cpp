#include <iostream>
#include <mpi.h>

int comm_sz, my_rank;

void q1_helloWorld() 
{
    printf("Hello, world from process %d of %d", my_rank, comm_sz);
}

void q2_p2p()
{

    if (my_rank == 0) 
    {
        int msg = 2017;
        for (int i = 1; i < comm_sz; i++) MPI_Send(&msg, 1, MPI_INT, i, 0, MPI_COMM_WORLD);
    }
    else 
    {
        int src = 0;
        int msg;
        MPI_Recv(&msg, 1, MPI_INT, src, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        printf("Process %d received %d from process %d", my_rank, msg, src);
    }
}

int main()
{
    MPI_Init(NULL, NULL);
    MPI_Comm_size(MPI_COMM_WORLD, &comm_sz);
    MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
    
    //q1_helloWorld();
    //q2_p2p();

    MPI_Finalize();
}