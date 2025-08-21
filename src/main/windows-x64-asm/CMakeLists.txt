cmake_minimum_required(VERSION 3.20)

project(secp256k1_masm LANGUAGES C ASM_MASM)

# Tell CMake where to find the asm source file
set(ASM_SOURCES src/main/windows-x64-asm/secp256k1.asm)

# Add library target (DLL)
add_library(secp256k1_win64_masm SHARED ${ASM_SOURCES})

# Set output name explicitly (optional)
set_target_properties(secp256k1_win64_masm PROPERTIES OUTPUT_NAME "secp256k1_win64_masm")

# Compiler flags for MSVC
if (MSVC)
    # Optimize for speed
    target_compile_options(secp256k1_win64_masm PRIVATE /O2 /GS- /GL /arch:AVX2)

    # Linker options for DLL
    target_link_options(secp256k1_win64_masm PRIVATE /OPT:REF)
endif()

# Specify export definitions if you have an exports.def file (optional)
# target_link_options(secp256k1_win64_masm PRIVATE "/DEF:${CMAKE_SOURCE_DIR}/exports.def")
