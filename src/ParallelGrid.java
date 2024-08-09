/************************************************************************************************
 * Purpose:      Parallel program to simulate an Abelian Sandpile cellular automaton
 *               This class creates a grid with a specified width and height 
 * Adapted from: Michelle Kuttel 2024, University of Cape Town
 * Copyright:    Copyright M.M.Kuttel 2024 CSC2002S, UCT
 *
 * @author:      Genevieve Chikwanha
 * @version:     05/08/2024
 ************************************************************************************************/

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.concurrent.RecursiveTask; 

public class ParallelGrid extends RecursiveTask<Boolean> {
	// The number of rows and columns 
	private int rows, columns;
	// The start and end row indexes 
	private int start, end;
	// A 2D grid
	static int[][] grid;
	// A copy of the 2D grid
	static int[][] updateGrid;
	// The cutoff point for the compute method
	protected static final int SEQUENTIAL_THRESHOLD = 2;

    
	/**
	 * General constructor that creates an empty ParallelGrid of a specified height(rows) 
	 * and width(columns).
	 * 
	 * @param rows    The number of rows the 2D grid contains
	 * @param columns The number of columns the 2D grid contains
	 */
	public ParallelGrid(int rows, int columns) {
		this.rows = rows + 2;                     // Add 2 for the "sink" border
		this.columns = columns + 2;               // Add 2 for the "sink" border
		grid = new int[this.rows][this.columns];
		updateGrid = new int[this.rows][this.columns];
		// Grid  initialisation
		for(int i = 0; i < this.rows; i++ ) {
			for(int j = 0; j < this.columns; j++) {
				grid[i][j] = 0;
				updateGrid[i][j] = 0;
			}
		}
	}


	/**
	 * General constructor to create a grid from a 2D array.
	 * Calls the consturctor above and fills it with the contents of the array.
	 * 
	 * @param newGrid A 2D array
	 */
	public ParallelGrid(int[][] newGrid) {
		this(newGrid.length, newGrid[0].length);
		// Don't copy over sink border
		for(int i = 1; i < rows - 1; i++ ) {
			for( int j = 1; j < columns - 1; j++ ) {
				this.grid[i][j] = newGrid[i - 1][j - 1];
			}
		}
		
	}


	/**
	 * Constructor to create a thread of object ParallelGrid type.
	 * 
	 * @param grid        A 2D grid with specified height and width.
	 * @param updateGrid  A copy of the 2D grid with the same specified height and width.
	 * @param start       The start row for the thread.
	 * @param end         The end row for the thread.
	 * @param columns     The total number of columns in the grid.
	 */
	public ParallelGrid(int[][] grid, int[][] updateGrid, int start, int end, int columns) {
		this.grid = grid;
		this.updateGrid = updateGrid;
		this.start = start;
		this.end = end;
		this.columns = columns;
	}


	/**
	 * Copy constructor.
	 * 
	 * @param copyGrid A copy of the ParallelGrid object
	 */
	public ParallelGrid(ParallelGrid copyGrid) {
		this(copyGrid.rows,copyGrid.columns); // Call constructor above
		// Grid  initialization
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				this.grid[i][j] = copyGrid.get(i,j);
			}
		}
	}


    /**
	 * Method to recursivly divide the grid row by row and initialise threads
	 * to update each section of the grid and return if the grid has changed
	 * or not.
	 * 
	 * @param  none
	 * @return Change a boolean that shows whether the grid cell has been changed or not
	 */
	protected Boolean compute() {
	    // If below threshold, run sequentially
        if((end - start) <= SEQUENTIAL_THRESHOLD) {
			//System.out.println("Check to see if we reach sequential threashold");
		    return update();
		} else {
			//System.out.println("Test to see if we enter the else statement of compute.");
			int midPoint = (end + start) / 2;
			// Split the grid in two and create threads for each half
			ParallelGrid partOneGrid = new ParallelGrid(grid, updateGrid, start, midPoint, columns);
			ParallelGrid partTwoGrid = new ParallelGrid(grid, updateGrid, midPoint, end, columns); 

			// Create threads using ForkJoin method
			partOneGrid.fork();                         // Create subthreads to deal with the left side
			boolean partTwoRes = partTwoGrid.compute(); // Handle the right side in this thread
			boolean partOneRes = partOneGrid.join();    // Ensure the left side is finished before the right/main user thread terminates 
			return (partTwoRes || partOneRes);     
		}
	}

	
	/**
	 * Gets the number of rows in the grid without the sink border.
	 * 
	 * @param  none
	 * @return Number of rows - 2
	 */
	public int getRows() {
		return rows - 2;      //  Less the sink
	}


	/**
	 * Gets the number of columns in the grid without the sink border.
	 *
	 * @param  none
	 * @return Number of columns - 2
	 */
	public int getColumns() {
		return columns - 2;   //  Less the sink
	}


	/**
	 * Gets a specific cell in the grid in row i, column j.
	 * 
	 * @param  i The row number
	 * @param  j The column number
	 * @return A cell in the grid
	 */
	public int get(int i, int j) {
		return this.grid[i][j];
	}

	
	/**
	 * Gets the entire grid as a 2D array.
	 * 
	 * @param  none
	 * @return The 2D array
	 */
	public int[][] getGrid() {
		return this.grid;
	}

	
    /**
	 * Gets the entire updateGrid as a 2D array.
	 *
	 * @param  none
	 * @return The 2D array
	 */
	public int[][] getUpdateGrid() {
		return this.updateGrid;
	}


	/**
	 * Sets all cells in the grid to a specified value.
	 *
	 * @param  value The value to set the cell to
	 * @return void
	 */
	void setAll(int value) {
		//borders are always 0
		for (int i = 1; i < rows - 1; i++) {
			for(int j = 1; j < columns - 1; j++) {			
				grid[i][j] = value;
			}
		}
	}
	

	//for the next timestep - copy updateGrid into grid
	public void nextTimeStep() {
		for (int i = 1; i < rows - 1; i++) {
			for (int j = 1; j < columns - 1; j++) {
				this.grid[i][j] = updateGrid[i][j];
			}
		}
	}
	
	//This runs in a timestep
	boolean update() {
		boolean change = false;
		// Do not update border
		for(int i = this.start; i < this.end + 1; i++) {
			//System.out.println("Check number of columns: " +columns);
			for(int j = 1; j < columns + 1; j++) {
				//System.out.println("Check to see if we enter the update bit");
				updateGrid[i][j] = (grid[i][j] % 4) +
						(grid[i - 1][j] / 4) +
						grid[i + 1][j] / 4 +
						grid[i][j - 1] / 4 +
						grid[i][j + 1] / 4;
				if (grid[i][j] != updateGrid[i][j]) {     // There has been a change to the grid then
					change = true;
				}
		    }
		} 
	if (change) {        // If there's been a change to the grid, then move to the next time step
	    nextTimeStep();
	}

	return change;      // If this is false, the loop stops since we're done changing the grid

	}

    /**
	 * Switches the grids (only the grid references 
	 * since arrays are pass by reference).
	 * 
	 * @param  none
	 * @return void
	 */
	public void swapGrids() {
		int[][] temp = grid;
		grid = updateGrid;
		updateGrid = temp;
	}


	/**
	 * Prints the grid to stdout without the borders (padded with zeros).
	 * 
	 * @param  none
	 * @return void
	 */
	void printGrid() {
		int i,j;
		// Not border is not printed
		System.out.printf("Grid:\n");
		System.out.printf("+");
		for( j = 1; j < columns-1; j++ ) System.out.printf("  --");
		System.out.printf("+\n");
		for( i = 1; i < rows - 1; i++ ) {
			System.out.printf("|");
			for( j = 1; j < columns - 1; j++ ) {
				if ( grid[i][j] > 0) 
					System.out.printf("%4d", grid[i][j] );
				else
					System.out.printf("    ");
			}
			System.out.printf("|\n");
		}
		System.out.printf("+");
		for( j = 1; j < columns - 1; j++ ) System.out.printf("  --");
		System.out.printf("+\n\n");
	}
	
	/**
	 * Writes the grid out as an image which is stored as 
	 * a .png file in the ouput folder. 
	 * 
	 * @param  fileName The name of the .png file the image is saved as
	 * @return void     Writes an image to a file; nothing is returned.
	 */
	void gridToImage(String fileName) throws IOException {
        BufferedImage dstImage = new BufferedImage(rows, columns, BufferedImage.TYPE_INT_ARGB);
        // Integer values from 0 to 255.
        int a = 0;
        int g = 0; // Green
        int b = 0; // Blue
        int r = 0; // Red

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
			     g = 0; // Green
			     b = 0; // Blue
			     r = 0; // Red

				switch (grid[i][j]) {
					case 0:
		                break;
		            case 1:
		            	g = 255;
		                break;
		            case 2:
		                b = 255;
		                break;
		            case 3:
		                r = 255;
		                break;
		            default:
		                break;
				
				}
		                // Set destination pixel to mean
		                // Re-assemble destination pixel.
		              int dpixel = (0xff000000)
		                		| (a << 24)
		                        | (r << 16)
		                        | (g<< 8)
		                        | b; 
		              dstImage.setRGB(i, j, dpixel); // Write it out
			}
		}
		
        File dstFile = new File(fileName);
        ImageIO.write(dstImage, "png", dstFile);
	}

	/**
	 * Creates a .csv file representation of the grid.
	 * 
	 * @param   fileName The name that the .csv file should be saved as
	 * @return  void
	 */
	public void gridToCSV(String fileName) throws IOException{
        FileWriter write = new FileWriter(fileName);

		//System.out.println(r + " and " + c);
		//System.out.println("Grid dimensions: " + grid.length + "x" + grid[0].length);
        //System.out.println("Last cell value: " + grid[grid.length-8][grid[0].length-2]);

		// Write the dimensions to the .csv
		write.append(Integer.toString(rows - 2)).append(",").append(Integer.toString(columns - 2));

		// Add empty cells after the dimenstions in the .csv
		for (int i = 2; i < columns; i++) {
            write.append(",");
		}
		write.append("\n");

		// Write the grid to the .csv
		for (int j = 1; j < rows; j++) {
            for (int i = 1; i < columns - 1; i++) {
                write.append(Integer.toString(grid[j][i]));

				if (i < columns - 1) {
                    write.append(",");
				}
			}

			write.append("\n");
		}
	}
}
