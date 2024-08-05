/************************************************************************************************
 * Purpose:      Parallel program to simulate an Abelian Sandpile cellular automaton
 *               This class creates a grid with a specified width and height 
 * Adapted from: Michelle Kuttel 2024, University of Cape Town
 * Copyright:    Copyright M.M.Kuttel 2024 CSC2002S, UCT
 *
 * @author:      Genevieve Chikwanha
 * @version:     04/08/2024
 ************************************************************************************************/

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import java.util.concurrent.RecursiveTask; 

public class ParallelGrid extends RecursiveTask<Boolean> {
	private int rows, columns;
	private int start, end;
	static int[][] grid;
	static int[][] updateGrid;
	protected static final int SEQUENTIAL_THRESHOLD = 4;
    
	/**
	 * Constructor that creates an empty ParallelGrid of a specified height(rows) 
	 * and width(columns).
	 * 
	 * @param rows    The number of rows the 2D grid contains
	 * @param columns The number of columns the 2D grid contains
	 */
	public ParallelGrid(int rows, int columns) {
		this.rows = rows + 2;                     // Add 2 for the "sink" border
		this.columns = columns + 2;               // Add 2 for the "sink" border
	    //	this.start = start;
	    //	this.end = end;
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
	 * Constructor to create a grid from a 2D array.
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
	 * A constructor for the parallel operations.
	 * 
	 * @param grid
	 * @param updateGrid
	 * @param start
	 * @param end
	 * @param columns
	 */
	public ParallelGrid(int[][] grid, int[][] updateGrid, int start, int end, int columns) {
		this.grid = grid;
		this.updateGrid = updateGrid;
		this.start = start;
		this.end = end;
		this.columns = columns;
	}

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
	 * Method to ...
	 * @param  none
	 * @return change A boolean that shows whether the grid cell has been changed or not
	 */
	protected Boolean compute() {
	    // If below threshold, run sequentially
        if((end - start) <= SEQUENTIAL_THRESHOLD) {
			//System.out.println("Check to see if we reach sequential threashold");
		    return update();
		} else {

		    // Way to split the grid -- WIP, currently experimental
			//System.out.println("Test to see if we enter the else statement of compute.");
			int midPoint = (end + start) / 2;
			// Split the grid in two
			ParallelGrid partOneGrid = new ParallelGrid(grid, updateGrid, start, midPoint, columns); // Recurse the first half of the grid
			ParallelGrid partTwoGrid = new ParallelGrid(grid, updateGrid, midPoint, end, columns);   // Recurse the second half of the grid

			// Create threads using ForkJoin method
			partOneGrid.fork();                         // Create subthreads to deal with the left side
			boolean partTwoRes = partTwoGrid.compute(); // Handle the right side in this thread
			boolean partOneRes = partOneGrid.join();    // Ensure the left side is finished before the right/main user thread terminates 
			return (partTwoRes || partOneRes);          // Maybe use &&
		}
	}
	
	public int getRows() {
		return rows - 2;      //  Less the sink
	}

	public int getColumns() {
		return columns - 2;   //  Less the sink
	}


	int get(int i, int j) {
		return this.grid[i][j];
	}

	public int[][] getGrid() {
		return this.grid;
	}

	public int[][] getUpdateGrid() {
		return this.updateGrid;
	}

	void setAll(int value) {
		//borders are always 0
		for( int i = 1; i < rows - 1; i++ ) {
			for( int j = 1; j < columns - 1; j++ ) 			
				grid[i][j] = value;
			}
	}
	

	//for the next timestep - copy updateGrid into grid
	public void nextTimeStep() {
		for(int i = 1; i < rows - 1; i++ ) {
			for( int j = 1; j < columns - 1; j++ ) {
				this.grid[i][j] = updateGrid[i][j];
			}
		}
	}
	
	//This runs in a timestep
	boolean update() {
		boolean change = false;
		//do not update border
		for( int i = this.start; i < this.end + 1; i++ ) {
			//System.out.println("Check number of columns: " +columns);
			for( int j = 1; j < columns + 1; j++ ) {
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
		} //end nested for
	if(change) {    // If there's been a change to the grid, then move to the next time step
	    nextTimeStep();
	}

	return change; // If this is false, the loop stops since we're done changing the grid

	}

	public void swapGrids() {
		int[][] temp = grid;
		grid = updateGrid;
		updateGrid = temp;
	}



	// NB, am I supposed to update this????
	// Display the grid in text format
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
	
	// Write grid out as an image
	void gridToImage(String fileName) throws IOException {
        BufferedImage dstImage =
                new BufferedImage(rows, columns, BufferedImage.TYPE_INT_ARGB);
        // Integer values from 0 to 255.
        int a = 0;
        int g = 0; // Green
        int b = 0; // Blue
        int r = 0; // Red

		for(int i = 0; i < rows; i++ ) {
			for(int j = 0; j < columns; j++ ) {
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
}
