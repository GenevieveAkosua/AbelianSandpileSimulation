# Makefile for compiling and running a parallel program in java

# Directories
SRC_DIR = src
BIN_DIR = bin

# Source files
JAVA_FILES = $(wildcard $(SRC_DIR)/*.java)

# Compiled class files 
CLASS_FILES = $(patsubst $(SRC_DIR)/%.java, $(BIN_DIR)/%.class, $(JAVA_FILES))

# Compilation flags
JAVAC_FLAGS = -d bin -sourcepath src

# Main class
MAIN_CLASS = ParallelAutomatonSim

# Default arguments
ARGS ?= input/65_by_65_all_4.csv output/65_by_65_all_4.png

# Targets
.PHONY: all clean run directories

all: directories $(CLASS_FILES)

directories:
		@mkdir -p $(BIN_DIR)

$(BIN_DIR)/%.class: $(SRC_DIR)/%.java
		javac $(JAVAC_FLAGS) $<

clean:
		rm -rf bin/*

run: all
		java -classpath bin $(MAIN_CLASS) $(ARGS)
