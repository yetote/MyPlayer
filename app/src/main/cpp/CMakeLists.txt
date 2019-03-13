# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
set(path_project D:/AndroidStudioProjects/MyPlayer)
set(libs_path ${path_project}/app/libs)
include_directories(${path_project}/app/src/main/cpp/ffmpeg/includes)

add_library(avutil
        SHARED
        IMPORTED)
set_target_properties(avutil
        PROPERTIES IMPORTED_LOCATION
        ${libs_path}/${ANDROID_ABI}/libavutil.so)

add_library(swresample SHARED IMPORTED)
set_target_properties(swresample PROPERTIES IMPORTED_LOCATION ${libs_path}/${ANDROID_ABI}/libswresample.so)

add_library(avcodec
        SHARED
        IMPORTED
        )

set_target_properties(
        avcodec
        PROPERTIES IMPORTED_LOCATION
        ${libs_path}/${ANDROID_ABI}/libavcodec.so
)

add_library(avformat
        SHARED
        IMPORTED
        )

set_target_properties(
        avformat
        PROPERTIES IMPORTED_LOCATION
        ${libs_path}/${ANDROID_ABI}/libavformat.so
)

add_library(swscale
        SHARED
        IMPORTED
        )

set_target_properties(
        swscale
        PROPERTIES IMPORTED_LOCATION
        ${libs_path}/${ANDROID_ABI}/libswscale.so
)


add_library(avfilter
        SHARED
        IMPORTED
        )

set_target_properties(
        avfilter
        PROPERTIES IMPORTED_LOCATION
        ${libs_path}/${ANDROID_ABI}/libavfilter.so
)

add_library(avdevice
        SHARED
        IMPORTED
        )

set_target_properties(
        avdevice
        PROPERTIES IMPORTED_LOCATION
        ${libs_path}/${ANDROID_ABI}/libavdevice.so
)

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp
        decode/Decode.cpp)


find_library( # Sets the name of the path variable.
        log-lib


        log)


target_link_libraries( # Specifies the target library.
        native-lib

        avutil
        swresample
        avformat
        avcodec
        swscale
        avfilter
        avdevice
        ${log-lib})