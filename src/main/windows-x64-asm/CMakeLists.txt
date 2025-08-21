cmake_minimum_required(VERSION 3.20)
project(secp256k1_masm LANGUAGES C ASM_MASM)

enable_language(ASM_MASM)

# Shared library from MASM source
add_library(secp256k1_win64_masm SHARED secp256k1.asm)

# Force MASM treatment on the .asm file
set_source_files_properties(secp256k1.asm
    PROPERTIES LANGUAGE ASM_MASM)

# Rename output DLL
set_target_properties(secp256k1_win64_masm PROPERTIES
    OUTPUT_NAME secp256k1_win64_masm
    PREFIX ""  # avoid "lib" prefix on DLL
)

# MSVC-specific flags
if(MSVC)
    # C compiler flags (if you add .c sources later)
    target_compile_options(secp256k1_win64_masm PRIVATE
        $<$<COMPILE_LANGUAGE:C>:/O2 /GS- /GL /arch:AVX2>)

    # Linker flags
    target_link_options(secp256k1_win64_masm PRIVATE /OPT:REF)
endif()
