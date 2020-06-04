#!/bin/bash

rm row_col.txt
for order in 2 4 8 16
do             
    for major in col row
    do
        for cache_type in dir set
        do
            EMU_PROG=mat-$major$order
            echo "./emu-rv32i-$cache_type-cache ./$EMU_PROG" >> row_col.txt
            ./emu-rv32i-$cache_type-cache ./$EMU_PROG|awk /./|grep -a "cache hit ratio" >> row_col.txt
        done
    done
    echo "" >> row_col.txt
done
