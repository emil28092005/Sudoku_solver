#!/bin/bash

# Initialize the CSV file and write the header
echo "Run,ExecutionTime_ms" > execution_times.csv

FILE_NAME=save_neo.py

for i in {1..1000}
do
    start_time=$(perl -MTime::HiRes=time -e 'printf("%.0f\n", time()*1000)')
    python $FILE_NAME
    end_time=$(perl -MTime::HiRes=time -e 'printf("%.0f\n", time()*1000)')
    elapsed_time=$(($end_time - $start_time))
    echo "Run $i: $elapsed_time ms"
    # Append the result to the CSV file
    echo "$i,$elapsed_time" >> execution_times.csv
done
