/************************************************************************************************
 * Purpose:      Parallel program to simulate an Abelian Sandpile cellular automaton
 *               This class creates a grid with a specified width and height 
 * Adapted from: Michelle Kuttel 2024, University of Cape Town
 * Copyright:    Copyright M.M.Kuttel 2024 CSC2002S, UCT
 *
 * @author:      Genevieve Chikwanha
 * @version:     29/07/2024
 ************************************************************************************************/

 // Thoughts
 // What is a meaningful way to split the grids? Line by line? In square blocks? Maybe dynamic rectangular blocks?
 // Do I still get to divide up the numb of tasks, say 400 total and I divide by 10, so 40 each, and that gets put into a pool for worker threads to fetch?
 // Values inside the grids are shared bc each needs to update deoending on if the sandpile toppled or not

//package serialAbelianSandpile;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import java.util.concurrent.RecursiveTask; // Change to recursivetasks if no return

// This class is for the grid for the Abelian Sandpile cellular automaton
public class ParallelGrid extends RecursiveTask {
	private int rows, columns;     //  Could be used to represent the start and end
	private int start, end;        //  Start and end of thread's range
	private int[][] grid;          //  Grid
	private int[][] updateGrid;    //  Grid for next time step
	static final int SEQUENTIAL_THRESHOLD = 25;  // How do you decide on a threshhold if your program input varies significantly
    
	public ParallelGrid(int rows, int columns) {
		this.rows = rows + 2;                           // Add 2 for the "sink" border
		this.columns = columns + 2;                     // Add 2 for the "sink" border
	//	this.start = start;
	//	this.end = end;
		grid = new int[this.rows][this.columns];
		updateGrid = new int[this.rows][this.columns];
		// This is what we'd want to parallelise, I think
		/* grid  initialisation */ // NEED TO EDIT THESE RANGES, MAYBE TO START END VALS
		for(int i = 0; i < this.rows; i++ ) { // Initialise each grid block with 0
			for(int j = 0; j < this.columns; j++) {
				grid[i][j] = 0;
				updateGrid[i][j] = 0;
			}
		}
	}

	// NB readFromArray calls from here
	public ParallelGrid(int[][] newGrid) { // Beg and end have the start and finish values
		this(newGrid.length, newGrid[0].length); // call constructor above
		// Don't copy over sink border
		for(int i = 1; i < rows - 1; i++ ) {
			for( int j = 1; j < columns - 1; j++ ) {
				this.grid[i][j] = newGrid[i - 1][j - 1];
			}
		}
		
	}

	public ParallelGrid(ParallelGrid copyGrid) {
		this(copyGrid.rows,copyGrid.columns, int beg, int fin); //call constructor above
		/* grid  initialization */
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
	protected boolean compute() {
	    // If below threshold, run sequentially
        if((column - row) < SEQUENTIAL_THRESHOLD) {

            boolean change = false;
			//do not update border
			for(int i = 1; i < rows - 1; i++) {
				for(int j = 1; j < columns - 1; j++) {
					updateGrid[i][j] = (grid[i][j] % 4) + 
						              (grid[i - 1][j] / 4) +
						               (grid[i + 1][j] / 4) +
						               grid[i][j - 1] / 4 + 
						               grid[i][j + 1] / 4;
				    if(grid[i][j] != updateGrid[i][j]) {  
					    change = true;
				    }
		        }
			} //end nested for
	        if(change) { 
	            nextTimeStep();
	        }

	        return change;

		} else {
			// Split the grid in two
			// Need to check these ranges, maybe rows and cols not / 2
			// WHICH CONSTRUCTOR??
			ParallelGrid leftGrid = new ParallelGrid(grid); // Recurse the left half of the grid
			ParallelGrid rightGrid = new ParallelGrid(grid); // Recurse the right half of the grid			

		//	ParallelGrid leftGrid = new ParallelGrid((rows / 2), (columns / 2), start, ((start + end) / 2)); // Recurse the left half of the grid
		//	ParallelGrid rightGrid = new ParallelGrid(((rows / 2)), ((columns / 2)), ((start + end) / 2), end); // Recurse the right half of the grid

			// Create threads using fork/join method
			leftGrid.fork();      // Create subthreads to deal with the left side
			boolean rightRes = rightgrid.compute();  // Handle the right side in this thread
			boolean leftRes = leftGrid.join();      // Ensure the left side is finished before the right/main user thread terminates 
			return (rightRes && leftRes);
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

	void setAll(int value) {
		//borders are always 0
		for( int i = 1; i < rows - 1; i++ ) {
			for( int j = 1; j < columns - 1; j++ ) 			
				grid[i][j] = value;
			}
	}
	

	//for the next timestep - copy updateGrid into grid
	public void nextTimeStep() {
		for(int i=1; i<rows-1; i++ ) {
			for( int j = 1; j < columns - 1; j++ ) {
				this.grid[i][j] = updateGrid[i][j];
			}
		}
	}
	
//	//key method to calculate the next update grod
//	boolean update() {
//		boolean change=false;
//		//do not update border
//		for( int i = 1; i<rows-1; i++ ) {
//			for( int j = 1; j<columns-1; j++ ) {
//				updateGrid[i][j] = (grid[i][j] % 4) +
//						(grid[i-1][j] / 4) +
//						grid[i+1][j] / 4 +
//						grid[i][j-1] / 4 +
//						grid[i][j+1] / 4;
//				if (grid[i][j] != updateGrid[i][j]) {
//					change = true;
//				}
//		}} //end nested for
//	if (change) {
//	    nextTimeStep();
//	}
//	return change;
//	}
	
	
	
	//display the grid in text format
	void printGrid() {
		int i,j;
		//not border is not printed
		System.out.printf("Grid:\n");
		System.out.printf("+");
		for( j=1; j<columns-1; j++ ) System.out.printf("  --");
		System.out.printf("+\n");
		for( i=1; i<rows-1; i++ ) {
			System.out.printf("|");
			for( j=1; j<columns-1; j++ ) {
				if ( grid[i][j] > 0) 
					System.out.printf("%4d", grid[i][j] );
				else
					System.out.printf("    ");
			}
			System.out.printf("|\n");
		}
		System.out.printf("+");
		for( j=1; j<columns-1; j++ ) System.out.printf("  --");
		System.out.printf("+\n\n");
	}
	
	//write grid out as an image
	void gridToImage(String fileName) throws IOException {
        BufferedImage dstImage =
                new BufferedImage(rows, columns, BufferedImage.TYPE_INT_ARGB);
        //integer values from 0 to 255.
        int a=0;
        int g=0;//green
        int b=0;//blue
        int r=0;//red

		for( int i=0; i<rows; i++ ) {
			for( int j=0; j<columns; j++ ) {
			     g=0;//green
			     b=0;//blue
			     r=0;//red

				switch (grid[i][j]) {
					case 0:
		                break;
		            case 1:
		            	g=255;
		                break;
		            case 2:
		                b=255;
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
		              dstImage.setRGB(i, j, dpixel); //write it out

			
			}}
		
        File dstFile = new File(fileName);
        ImageIO.write(dstImage, "png", dstFile);
	}
	
	


}
