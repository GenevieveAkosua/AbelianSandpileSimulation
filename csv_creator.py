# Program to create .csv files with various fill values
# Author: Genevieve Chikwanha
# Editor: Claude.ai
# Date:   07/08/2024

import csv
import os


def create_sandpile_csv(rows, columns, fill_value=0, centre_value=None, output_folder='input'):
    # Create the output folder if it doesn't exist
    if not os.path.exists(output_folder):
        os.makedirs(output_folder)

    # Create the grid
    grid = [[fill_value for _ in range(columns)] for _ in range(rows)]

    # Set the center value if specified
    if centre_value is not None:
        center_row, center_col = rows // 2, columns // 2
        grid[center_row][center_col] = centre_value

    # Determine the filename
    if centre_value is not None:
        filename = f"{rows}_by_{columns}_center_{centre_value}.csv"
    else:
        filename = f"{rows}_by_{columns}_all_{fill_value}.csv"

    # Write the grid to a CSV file
    filepath = os.path.join(output_folder, filename)
    with open(filepath, 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow([rows, columns])  # Write dimensions as the first row
        writer.writerows(grid)

    print(f"Created file: {filepath}")


# Create CSV files for different dimensions and patterns
dimensions = [
    (32, 32),
    (125, 125),
    (250, 250),
    (750, 750),
    (2000, 2000)
]

for rows, columns in dimensions:
    # Create a grid with all zeros
    create_sandpile_csv(rows, columns, fill_value=0)

    # Create a grid with all zeros and 10 in the center
    create_sandpile_csv(rows, columns, fill_value=0, centre_value=10)

    # Create a grid with all fours
    create_sandpile_csv(rows, columns, fill_value=4)

print("All files have been created successfully.")
