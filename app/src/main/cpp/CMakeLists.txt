cmake_minimum_required(VERSION 3.10.2)

project("equalizer-native")

add_library(
    equalizer-native
    SHARED
    native-lib.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    equalizer-native
    ${log-lib}
)
