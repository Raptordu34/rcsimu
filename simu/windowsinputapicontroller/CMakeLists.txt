cmake_minimum_required(VERSION 3.15)

project(sim_input LANGUAGES CXX)

# Standard C++
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Cria a DLL a partir do mapping.cpp
add_library(sim_input SHARED
    native/mapping.cpp
)

# Onde está o mapping.h
target_include_directories(sim_input PRIVATE
    native
)

# Windows only
if (WIN32)
    target_compile_definitions(sim_input PRIVATE DIRECTINPUT_VERSION=0x0800)

    # Linka as libs necessárias (XInput + DirectInput)
    target_link_libraries(sim_input PRIVATE dinput8 dxguid Xinput9_1_0)
endif()

# Coloca a DLL em build/bin
set_target_properties(sim_input PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin"
)
