# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 2.8

#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:

# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list

# Suppress display of executed commands.
$(VERBOSE).SILENT:

# A target that is always out of date.
cmake_force:
.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/jsrudani/hadoopcache/hadoopsrccode/hadoop-hdfs-project/hadoop-hdfs/src

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/jsrudani/hadoopcache/hadoopsrccode/hadoop-hdfs-project/hadoop-hdfs/target/native

# Include any dependencies generated for this target.
include CMakeFiles/posix_util.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/posix_util.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/posix_util.dir/flags.make

CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o: CMakeFiles/posix_util.dir/flags.make
CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o: /home/jsrudani/hadoopcache/hadoopsrccode/hadoop-hdfs-project/hadoop-hdfs/src/main/native/util/posix_util.c
	$(CMAKE_COMMAND) -E cmake_progress_report /home/jsrudani/hadoopcache/hadoopsrccode/hadoop-hdfs-project/hadoop-hdfs/target/native/CMakeFiles $(CMAKE_PROGRESS_1)
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Building C object CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o"
	/usr/bin/cc  $(C_DEFINES) $(C_FLAGS) -o CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o   -c /home/jsrudani/hadoopcache/hadoopsrccode/hadoop-hdfs-project/hadoop-hdfs/src/main/native/util/posix_util.c

CMakeFiles/posix_util.dir/main/native/util/posix_util.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/posix_util.dir/main/native/util/posix_util.c.i"
	/usr/bin/cc  $(C_DEFINES) $(C_FLAGS) -E /home/jsrudani/hadoopcache/hadoopsrccode/hadoop-hdfs-project/hadoop-hdfs/src/main/native/util/posix_util.c > CMakeFiles/posix_util.dir/main/native/util/posix_util.c.i

CMakeFiles/posix_util.dir/main/native/util/posix_util.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/posix_util.dir/main/native/util/posix_util.c.s"
	/usr/bin/cc  $(C_DEFINES) $(C_FLAGS) -S /home/jsrudani/hadoopcache/hadoopsrccode/hadoop-hdfs-project/hadoop-hdfs/src/main/native/util/posix_util.c -o CMakeFiles/posix_util.dir/main/native/util/posix_util.c.s

CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o.requires:
.PHONY : CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o.requires

CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o.provides: CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o.requires
	$(MAKE) -f CMakeFiles/posix_util.dir/build.make CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o.provides.build
.PHONY : CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o.provides

CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o.provides.build: CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o

# Object files for target posix_util
posix_util_OBJECTS = \
"CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o"

# External object files for target posix_util
posix_util_EXTERNAL_OBJECTS =

libposix_util.a: CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o
libposix_util.a: CMakeFiles/posix_util.dir/build.make
libposix_util.a: CMakeFiles/posix_util.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --red --bold "Linking C static library libposix_util.a"
	$(CMAKE_COMMAND) -P CMakeFiles/posix_util.dir/cmake_clean_target.cmake
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/posix_util.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/posix_util.dir/build: libposix_util.a
.PHONY : CMakeFiles/posix_util.dir/build

CMakeFiles/posix_util.dir/requires: CMakeFiles/posix_util.dir/main/native/util/posix_util.c.o.requires
.PHONY : CMakeFiles/posix_util.dir/requires

CMakeFiles/posix_util.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/posix_util.dir/cmake_clean.cmake
.PHONY : CMakeFiles/posix_util.dir/clean

CMakeFiles/posix_util.dir/depend:
	cd /home/jsrudani/hadoopcache/hadoopsrccode/hadoop-hdfs-project/hadoop-hdfs/target/native && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/jsrudani/hadoopcache/hadoopsrccode/hadoop-hdfs-project/hadoop-hdfs/src /home/jsrudani/hadoopcache/hadoopsrccode/hadoop-hdfs-project/hadoop-hdfs/src /home/jsrudani/hadoopcache/hadoopsrccode/hadoop-hdfs-project/hadoop-hdfs/target/native /home/jsrudani/hadoopcache/hadoopsrccode/hadoop-hdfs-project/hadoop-hdfs/target/native /home/jsrudani/hadoopcache/hadoopsrccode/hadoop-hdfs-project/hadoop-hdfs/target/native/CMakeFiles/posix_util.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : CMakeFiles/posix_util.dir/depend

