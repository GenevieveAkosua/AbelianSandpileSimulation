/************************************************************************************************
 * Purpose:      Parallel program to simulate an Abelian Sandpile cellular automaton
 *               This class runs the simulation using the grid from the ParallelGrid.java class
 * Adapted from: Michelle Kuttel 2024, University of Cape Town
 * Copyright:    Copyright M.M.Kuttel 2024 CSC2002S, UCT
 *
 * @author:      Genevieve Chikwanha
 * @version:     04/08/2024
 ************************************************************************************************/

import java.io.BufferedReader;
import java.io.FileReader;
iimport java.io.IOException;
import java.util.concurrent.ForkJoinPool; 

class ParallelAutomatonSim {
	// When true, will print output for debugging
	static final boolean DEBUG = false;

	// Timers to measure program run time
	static long startTime = 0;
	static long endTime = 0;
                          
	// ForkJoinPool object using the common pool approach
	static final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool(); 


	/**
	 * Records program start time
	 *
	 * @param  none
	 * @return void
	 */
	private static void tick() {
		startTime = System.currentTimeMillis();
	}


	/**
	 * Records program start time
	 *
	 * @param  none
	 * @return void
	 */
	private static void tock() {
		endTime = System.currentTimeMillis(); 
	}
	

	/**
	 * Reads in the values from a .csv file and creates a 2D array with a height
	 * and width specified in the command-line input to store the values from the file 
	 *
	 * @param  filePath The file path of the file to be read from
	 * @return A 2D array with specified height (num of rows) and width (num of columns)
	 */
	 public static int[][] readArrayFromCSV(String filePath) {
		 int[][] array = null;
	        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
	            String line = br.readLine();
	            if (line != null) {
				    // Read in the array dimensions, i.e. height and width from input
	                String[] dimensions = line.split(",");
	                int width = Integer.parseInt(dimensions[0]);
	                int height = Integer.parseInt(dimensions[1]);
	               	System.out.printf("Rows: %d, Columns: %d\n", width, height);
					array = new int[height][width];
	                
					// Read in cell values from .csv file
					int rowIndex = 0;
	                while ((line = br.readLine()) != null && rowIndex < height) {
	                    String[] values = line.split(",");
	                    for (int colIndex = 0; colIndex < width; colIndex++) {
	                        array[rowIndex][colIndex] = Integer.parseInt(values[colIndex]);
	                    }
	                    rowIndex++;
	                }
	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return array;
	    }


    /**
	 * The main method of the program which initialises a ParallelGrid object
	 * and runs the update() function for the ParallelGrid class until
	 * a stable state is reached. The result of the simulation, i.e. the 
	 * number of timesteps and the time taken are calculated and printed 
	 * to the stdout. A grid image is also drawn as saved as a .png file.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
    	  	
    	// Check if correct arguments are provided in the command-line
		if (args.length != 2) { 
    		System.out.println("Incorrect number of command line arguments provided.");   	
    		System.exit(0);
    	}

    	// Read command-line argument values 
  		String inputFileName = args[0];   // Input file name
		String outputFileName = args[1];  // Output file name
    
		// Read from input .csv file and create a ParallelGrid object
		ParallelGrid simulationGrid = new ParallelGrid(readArrayFromCSV(inputFileName));

    	// For debugging - hardcoded re-initialisation options
    	//simulationGrid.set(rows/2,columns/2,rows*columns*2);
    	//simulationGrid.set(rows/2,columns/2,55000);
    	//simulationGrid.setAll(4);
    	//simulationGrid.setAll(8);
   	
    	// Initialise counter and start timer
		int counter = 0; // Counts number of timesteps taken
    	tick();          // Starts timer to measure runtime

    	// Prints the grid if Debug is set to true
		if (DEBUG) {
    		System.out.printf("starting config: %d \n", counter);
    		simulationGrid.printGrid();
    	}

        // Begin simulation updates by invoking ForkJoinPool if the grid has not reached stable state
		int rowNum = simulationGrid.getRows();    // Get rows
		int colNum = simulationGrid.getColumns(); // Get columns
		boolean changed;                          // Boolean to check if there has been a change in the grid between timestep n and timestep n - 1
        do {                                      // Continue loop until no change, i.e. stable state is reached
			changed = forkJoinPool.invoke(new ParallelGrid(simulationGrid.getGrid(), simulationGrid.getUpdateGrid(), 1, rowNum, colNum));
			
			//Run double buffering technique
			if (changed) {
				simulationGrid.swapGrids();
			}
		

	    	if (DEBUG) {
			    simulationGrid.printGrid();
			}
	    		counter++;
	    } while (changed);
   		tock(); // End timer
   		
        System.out.println("Simulation complete, writing image...");
		// Write grid as image
    	simulationGrid.gridToImage(outputFileName);

		// Write grid as .csv
		simulationGrid.gridToCSV("outputGrids/CSVGrid_" + rowNum + "_by_" + colNum + ".csv");

       	// Print simulation details     	
		//System.out.printf("\t Rows: %d, Columns: %d\n", simulationGrid.getRows(), simulationGrid.getColumns());
		System.out.printf("Number of steps to stable state: %d \n", counter);
		System.out.printf("Time: %d ms\n",endTime - startTime );  //  Total computation time 		
    }
}
