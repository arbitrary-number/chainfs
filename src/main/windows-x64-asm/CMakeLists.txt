cmake_minimum_required(VERSION 3.20)
project(secp256k1_masm LANGUAGES C ASM_MASM)

enable_language(ASM_MASM)

# ASM source
add_library(secp256k1_win64_masm SHARED secp256k1.asm)

# Freidly tweaks
set_target_properties(secp256k1_win64_masm PROPERTIES
                      OUTPUT_NAME secp256k1_win64_masm)

if(MSVC)
    # MSVC only flags – scoped to *C* only
    set_source_files_properties(secp256k1.asm
            PROPERTIES LANGUAGE ASM_M ASM_MASM)

    # C-specific flags (leave MASM alone)
    target_compile_options(secp256k1_win64_masm
         PRIVATE $<$<COMPILE_LANGUAGE:C>:/O2 /GS- /GL /arch:AVX2>)

    # Linker settings
    target_link_options(secp256k1_win64_masm PRIVATE /OPT:REF)
endif()
