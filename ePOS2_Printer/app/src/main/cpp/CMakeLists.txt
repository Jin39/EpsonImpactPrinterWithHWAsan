cmake_minimum_required(VERSION 3.18.1)

project("hwasantest")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find required libraries
find_library(log-lib log)
find_library(android-lib android)

# Add HWASan flags for the hwasan build type
    # Add HWASan compiler flags
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fsanitize=hwaddress -fno-omit-frame-pointer -fsanitize-recover=hwaddress")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fsanitize=hwaddress -fno-omit-frame-pointer -fsanitize-recover=hwaddress")
    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -fsanitize=hwaddress")
    
    # Additional debugging flags
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -g -O0")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -g -O0")
    
    message(STATUS "HWASan flags enabled for Debug build")

# Create the shared library
add_library(
    hwasantest
    SHARED
    hwasan_test.cpp
)

# Link libraries
target_link_libraries(
    hwasantest
    ${log-lib}
    ${android-lib}
)