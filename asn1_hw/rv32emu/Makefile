BINS = emu-rv32i first bubble_recur bubble_iter atoi

CROSS_COMPILE = riscv-none-embed-
RV32I_CFLAGS = -march=rv32i -mabi=ilp32 -O0 -nostdlib

CFLAGS = -O3 -Wall
LDFLAGS = -lelf

all: $(BINS)

emu-rv32i: emu-rv32i.c
	$(CC) $(CFLAGS) -o $@ $< $(LDFLAGS)

first: first.c
	$(CROSS_COMPILE)gcc $(RV32I_CFLAGS) -o $@ $<

bubble_recur: bubble_recur.c
	$(CROSS_COMPILE)gcc $(RV32I_CFLAGS) -o $@ $<

bubble_iter: bubble_iter.c
	$(CROSS_COMPILE)gcc $(RV32I_CFLAGS) -o $@ $<

atoi: atoi.c
	$(CROSS_COMPILE)gcc $(RV32I_CFLAGS) -o $@ $<

check: $(BINS)
	./emu-rv32i first
	./emu-rv32i bubble_recur
	./emu-rv32i bubble_iter
	./emu-rv32i atoi

clean:
	$(RM) $(BINS)

