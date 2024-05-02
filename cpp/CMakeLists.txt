#set(CMAKE_MAKE_PROGRAM D:/ninja)


cmake_minimum_required(VERSION 3.22.1)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

project("myapplication")

# Add NEON flag for ARM architecture
if(ANDROID_ABI STREQUAL armeabi-v7a)
    set_target_properties(${myapllication} PROPERTIES COMPILE_FLAGS -mfpu=neon)
endif()



# Set the path to the Highway project
#set(HIGHWAY_DIR "D:/Download/highway-master")

# Add the Highway project as a subdirectory
#add_subdirectory(${HIGHWAY_DIR} highway_build)

# Add your application's source files
add_library(
        myapplication
        SHARED
        myapplication.cpp
        imageSharpening.cpp
        stackblur.h
        myapp.h
        avir/avir.h
        avir/avir_dil.h
        avir/avir_float4_sse.h
        avir/avir_float8_avx.h
        avir/thread_pool.hpp
        avir/lancir.h
)

# Link necessary libraries
target_link_libraries(myapplication android log )

# If you are using JNI, you don't need to manually find and link log-lib
# Just link to android and log as shown above

# If you need to link ljnigraphics, add it to target_link_libraries as well

#target_link_libraries(
#        myapplication
#        -ljnigraphics
#        ${log-lib}
#        )
