#!/bin/bash

# Initialize the CSV file and write the header
echo "Run,ExecutionTime_ms,Result" > execution_times.csv

FILE_NAME=Main.java

for i in {1..1000}
do
    # Record the start time in milliseconds
    start_time=$(perl -MTime::HiRes=time -e 'printf("%.0f\n", time()*1000)')

    # Execute the Python script and capture its output
    # Use `stdout` and `stderr` to capture all outputs
    output=$(python "$FILE_NAME" 2>&1)

    # Record the end time in milliseconds
    end_time=$(perl -MTime::HiRes=time -e 'printf("%.0f\n", time()*1000)')

    # Calculate the elapsed time
    elapsed_time=$((end_time - start_time))

    # Extract the result from the Python script's output
    # Assumes the output is in the format "e <number>"
    result_line=$(echo "$output" | grep '^e ')

    if [ -n "$result_line" ]; then
        # Extract the number after 'e '
        number=$(echo "$result_line" | awk '{print $2}')

        # Determine Result as 1 or 0 based on the number
        if [ "$number" -gt 0 ]; then
            result=1
        elif [ "$number" -eq -1 ]; then
            result=0
        else
            # Handle unexpected numbers
            result="Unexpected_$number"
            echo "Run $i: $elapsed_time ms, Result: $result (Unexpected number)"
        fi
    else
        # If the expected line is not found
        result="N/A"
        echo "Run $i: $elapsed_time ms, Result: $result (Missing 'e ' in output)"
    fi

    # Log the execution time and result
    echo "Run $i: $elapsed_time ms, Result: $result"

    # Append the run number, elapsed time, and result to the CSV file
    echo "$i,$elapsed_time,$result" >> execution_times.csv
done
