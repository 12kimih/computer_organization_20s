#!/bin/bash

rm mat_order.txt
for cache_type in dir set
do             
    for major in col row
    do
        for order in 2 4 8 16
        do
            EMU_PROG=mat-$major$order
            echo "./emu-rv32i-$cache_type-cache ./$EMU_PROG" >> mat_order.txt
            ./emu-rv32i-$cache_type-cache ./$EMU_PROG|awk /./|grep -a "cache hit ratio" >> mat_order.txt
        done
        echo "" >> mat_order.txt
    done
done
