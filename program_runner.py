###################################################################
# Purpose:   Script to run the AutomatonSimulation.java and its
#            parallel equivalent, ParallelAutomatonSim.java,
#            multiple times for experimentation and output the
#            full results for each trial and averages for each
#            io pair into an Excel file, as well as a correctness
#            evaluation for how correct each output is.
# Author:    Genevieve Chikwanha
# Version:   05/08/2024
# Credits:   See Report
#
###################################################################

"""
import subprocess
import pandas as panda
import shlex
import os


Initiates the simulation for both the parallel and serial versions.

def initiate_run(input_file, output_file, program_folder):
    # Get cwd
    current_dir = os.getcwd()

    # Change to the appropriate directory
    os.chdir(program_folder)

    # Adjustable command to run the appropriate simulation

    # Change back to the cwd
    os.chdir(current_dir)

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

        # Command to run the simulation
        command = f'make run ARGS="input/{input_file} output/{output_file}"'
        # Run a subprocess
        result = subprocess.run(command, capture_output=True, text=True, shell=True)
        # Store output lines
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


import pandas as panda
import subprocess
import os
from PIL import Image
from pixelmatch import pixelmatch


def run_simulation(input_file, output_file, version_folder):
    original_dir = os.getcwd()
    os.chdir(version_folder)

    command = f'make run ARGS="input/{input_file} output/{output_file}"'
    result = subprocess.run(command, capture_output=True, text=True, shell=True)

    os.chdir(original_dir)
    return result.stdout.strip().split('\n')


def extract_metrics(output_lines):
    steps_line = next((line for line in output_lines if "Number of steps to stable state" in line), None)
    time_line = next((line for line in output_lines if "Time:" in line), None)

    steps = int(steps_line.split(":")[1].strip()) if steps_line else None
    time = int(time_line.split(":")[1].strip().split()[0]) if time_line else None

    return steps, time


def compare_outputs(serial_output, parallel_output):
    return


TRIALS = 2
serial_folder = "/Users/genevievechikwanha/Library/CloudStorage/OneDrive-UniversityofCapeTown/Uni Work/Computer Science/CSC2002S/CSC2002S 2024/1. Paralell Programming/Assignments/Assignment1/PCP_ParallelAssignment2024"
parallel_folder = "/Users/genevievechikwanha/Library/CloudStorage/OneDrive-UniversityofCapeTown/Uni Work/Computer Science/CSC2002S/CSC2002S 2024/1. Paralell Programming/Assignments/Assignment1/CHKKAR002"
io_pairs = [
    ('8_by_8_all_4.csv', '8.png'),
    ('16_by_16_all_4.csv', '16.png'),
    ('16_by_16_one_100.csv', '16One.png'),
    ('65_by_65_all_4.csv', '65.png')
]
# ('517_by_517_centre_534578.csv', '517.png'),
# ('1001_by_1001_all_8.csv', '1001.png')

all_results = []

for input_file, output_file in io_pairs:
    for trial in range(TRIALS):
        # Run serial version
        serial_output = run_simulation(input_file, output_file, serial_folder)
        serial_steps, serial_time = extract_metrics(serial_output)

        # Run parallel version
        parallel_output = run_simulation(input_file, output_file, parallel_folder)
        parallel_steps, parallel_time = extract_metrics(parallel_output)

        # Check correctness
        is_correct = compare_outputs(serial_output, parallel_output)

        # Store results
        all_results.append({
            'input_file': input_file,
            'output_file': output_file,
            'trial': trial,
            'serial_timesteps': serial_steps,
            'serial_timetaken': serial_time,
            'parallel_timesteps': parallel_steps,
            'parallel_timetaken': parallel_time,
            'is_correct': is_correct
        })

    print(f"Completed {TRIALS} trials for {input_file} -> {output_file}")

# Convert results into a data frame
data_frame = panda.DataFrame(all_results)

# Calculate minimum values and correctness for each io pair
min_results = data_frame.groupby(['input_file', 'output_file']).agg({
    'serial_timesteps': 'min',
    'serial_timetaken': 'min',
    'parallel_timesteps': 'min',
    'parallel_timetaken': 'min',
    'is_correct': 'mean'
}).reset_index()

min_results = min_results.rename(columns={'is_correct': 'correctness_rate'})

# Save the full results and minimum values in sheets in an Excel file
with panda.ExcelWriter('results.xlsx') as writer:
    data_frame.to_excel(writer, sheet_name='All Results', index=False)
    min_results.to_excel(writer, sheet_name='Summary', index=False)

print("All trials are complete and results have been saved.")
print("\nSummary:")
print(min_results)
"""


import pandas as panda
import subprocess
import os
from PIL import Image
from pixelmatch.contrib.PIL import pixelmatch


def run_simulation(input_file, output_file, version_folder):
    original_dir = os.getcwd()
    os.chdir(version_folder)

    command = f'make run ARGS="input/{input_file} output/{output_file}"'
    result = subprocess.run(command, capture_output=True, text=True, shell=True)

    os.chdir(original_dir)
    return result.stdout.strip().split('\n')


def extract_metrics(output_lines):
    steps_line = next((line for line in output_lines if "Number of steps to stable state" in line), None)
    time_line = next((line for line in output_lines if "Time:" in line), None)

    steps = int(steps_line.split(":")[1].strip()) if steps_line else None
    time = int(time_line.split(":")[1].strip().split()[0]) if time_line else None

    return steps, time


def compare_outputs(serial_folder, parallel_folder, output_file):
    serial_image_path = os.path.join(serial_folder, 'output', output_file)
    parallel_image_path = os.path.join(parallel_folder, 'output', output_file)

    try:
        serial_image = Image.open(serial_image_path)
        parallel_image = Image.open(parallel_image_path)

        if serial_image.size != parallel_image.size:
            print(f"Image sizes do not match for {output_file}")
            return False

        mismatch = pixelmatch(serial_image, parallel_image)

        if mismatch == 0:
            print(f"Images match perfectly for {output_file}")
            return True
        else:
            print(f"Images differ by {mismatch} pixels for {output_file}")
            return False
    except Exception as e:
        print(f"Error comparing images for {output_file}: {str(e)}")
        return False


TRIALS = 2
serial_folder = "/Users/genevievechikwanha/Library/CloudStorage/OneDrive-UniversityofCapeTown/Uni Work/Computer Science/CSC2002S/CSC2002S 2024/1. Paralell Programming/Assignments/Assignment1/PCP_ParallelAssignment2024"
parallel_folder = "/Users/genevievechikwanha/Library/CloudStorage/OneDrive-UniversityofCapeTown/Uni Work/Computer Science/CSC2002S/CSC2002S 2024/1. Paralell Programming/Assignments/Assignment1/CHKKAR002"
io_pairs = [
    ('8_by_8_all_4.csv', '8.png'),
    ('16_by_16_all_4.csv', '16.png'),
    ('16_by_16_one_100.csv', '16One.png'),
    ('65_by_65_all_4.csv', '65.png'),
    ('125_by_125_all_0.csv', '125.png'),
    ('250_by_250_all_4.csv', '250.png'),
    ('517_by_517_centre_534578.csv', '517.png'),
    ('750_by_750_all_0.csv', '750.png'),
    ('1001_by_1001_all_8.csv', '1001.png')
]

all_results = []

# Run serial version for all io_pairs first
for input_file, output_file in io_pairs:
    serial_output = run_simulation(input_file, output_file, serial_folder)
    print(f"Completed serial run for {input_file} -> {output_file}")

# Then run parallel version and compare outputs
for input_file, output_file in io_pairs:
    for trial in range(TRIALS):
        # Run serial version (metrics only)
        serial_output = run_simulation(input_file, output_file, serial_folder)
        serial_steps, serial_time = extract_metrics(serial_output)

        # Run parallel version
        parallel_output = run_simulation(input_file, output_file, parallel_folder)
        parallel_steps, parallel_time = extract_metrics(parallel_output)

        # Check correctness
        is_correct = compare_outputs(serial_folder, parallel_folder, output_file)

        # Store results
        all_results.append({
            'input_file': input_file,
            'output_file': output_file,
            'trial': trial,
            'serial_timesteps': serial_steps,
            'serial_timetaken': serial_time,
            'parallel_timesteps': parallel_steps,
            'parallel_timetaken': parallel_time,
            'is_correct': is_correct
        })

    print(f"Completed {TRIALS} trials for {input_file} -> {output_file}")

# Convert results into a data frame
data_frame = panda.DataFrame(all_results)

# Calculate minimum values and correctness for each io pair
min_results = data_frame.groupby(['input_file', 'output_file']).agg({
    'serial_timesteps': 'min',
    'serial_timetaken': 'min',
    'parallel_timesteps': 'min',
    'parallel_timetaken': 'min',
    'is_correct': 'mean'
}).reset_index()

min_results = min_results.rename(columns={'is_correct': 'correctness_rate'})

# Save the full results and minimum values in sheets in an Excel file
with panda.ExcelWriter('results.xlsx') as writer:
    data_frame.to_excel(writer, sheet_name='All Results', index=False)
    min_results.to_excel(writer, sheet_name='Summary', index=False)

print("All trials are complete and results have been saved.")
print("\nSummary:")
print(min_results)
