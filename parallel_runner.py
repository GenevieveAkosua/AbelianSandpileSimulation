###################################################################
# Purpose:   Script to run the AutomatonSimulation.java and its
#            parallel equivalent, ParallelAutomatonSim.java,
#            multiple times for experimentation and output the
#            full results for each trial and averages for each
#            io pair into an Excel file, as well as a correctness
#            evaluation for how correct each output is.
# Author:    Genevieve Chikwanha
# Version:   03/08/2024
# Credits:   See Report
#
###################################################################

import subprocess
import pandas as panda
import shlex
import os

'''
Run the parallel program followed by the serial program
'''

#def run_programs(input_file, output_file, program_version):
    #

# List of input/output pairs to dynamically call make run on
io_pairs = [
    ('8_by_8_all_4.csv', '8.png'),
    ('16_by_16_all_4.csv', '16.png'),
    ('16_by_16_one_100.csv', '16One.png'),
    ('65_by_65_all_4.csv', '65.png')
]
# ('517_by_517_centre_534578.csv', '517.png'),
# ('1001_by_1001_all_8.csv', '1001.png')

# Number of trials to run per io pair
TRIALS = 10

all_results = []

for input_file, output_file in io_pairs:
    pair_results = []

    for trial in range(TRIALS): # Call make run on specified io pair

        command = f'make run ARGS="input/{input_file} output/{output_file}"'

        # Run a subprocess
        result = subprocess.run(command, capture_output=True, text=True, shell=True)
        output_lines = result.stdout.strip().split('\n')

        # Test
        print(f"Command output:\n{result.stdout}")
        # Print all output lines for debugging
        print("All output lines:")
        for line in output_lines:
            print(line)

        # Extract particular lines of output
        # Namely time run and steps taken
        # Find the required output
        steps_line = next((line for line in output_lines if "Number of steps to stable state" in line), None)
        time_line = next((line for line in output_lines if "Time:" in line), None)

        # Testing
        print(f"Steps line: {steps_line}")
        print(f"Time line: {time_line}")

        # Extract the values needed from the output
        steps = int(steps_line.split(":")[1].strip()) if steps_line else None
        time = int(time_line.split(":")[1].strip().split()[0]) if time_line else None
    #    steps = output_lines[2] if len(output_lines) > 2 else None
    #    time = output_lines[3] if len(output_lines) > 3 else None

        # Test
        print(f"Extracted steps: {steps}")
        print(f"Extracted time: {time}")

        pair_results.append({
            'input_file': input_file,
            'output_file': output_file,
            'timesteps': steps,
            'timetaken': time
        })

        all_results.extend(pair_results)
    print(f"Completed {TRIALS} trials for {input_file} -> {output_file}")

# Convert results into a data frame
data_frame = panda.DataFrame(all_results)
print("Data frame contents: ")
print(data_frame)

# Calculate averages for each io pair
min_results = data_frame.groupby(['input_file', 'output_file']).agg({
    'timesteps': 'min',
    'timetaken': 'min'
}).reset_index()

print(min_results)

# Save the full results and averages in sheets in an Excel file
with panda.ExcelWriter('results.xlsx') as writer:
    data_frame.to_excel(writer, sheet_name='All Results', index=False)
    min_results.to_excel(writer, sheet_name='Minimum Results', index=False)

print("All trials are complete and results have been saved.")


