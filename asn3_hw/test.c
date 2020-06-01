#include <stdio.h>
#include <stdint.h>

struct cache_block {
  uint32_t tag:24;
  uint32_t valid:1;
  uint32_t data;
};

int main() {
    uint8_t a = 0xff;
    uint32_t b = a << 8;
    printf("%#010x\n", b);

    int8_t c = 0xff;
    uint32_t d = c;
    printf("%#010x\n", d);

    uint8_t e = 0xff;
    int32_t f = e;
    printf("%#010x\n", f);

    uint32_t g = 0xffffffff >> 16;
    printf("%#010x\n", g);

    printf("%#010x\n", g << 8);
    printf("%#010x\n", g << 16);
    printf("%#010x\n", g << 24);

    uint64_t h = (uint32_t)0xffffff << 8;
    printf("%#018lx\n", h);

    int32_t i = 0xffffffff;
    int32_t j = 0xffffffff;
    uint64_t k = i | j;
    printf("%#018lx\n", k);

    uint32_t l = 0x0fffffff;
    uint8_t m = l;
    printf("%#010x\n", m);

    struct cache_block c1 = {0xffffff, 0x1, 0xffffffff};
    printf("%#010x\n", c1.data);
    printf("%#010x\n", c1.valid);
    printf("%#010x\n", c1.tag);

    uint8_t ram0 = 0x78;
    uint8_t ram1 = 0x56;
    uint8_t ram2 = 0x34;
    uint8_t ram3 = 0x12;
    uint32_t value = ram0 | (ram1 << 8) | (ram2 << 16) | (ram3 << 24);
    printf("%#010x\n", value);

    uint8_t ram[4];
    ram[0] = value & 0xff;
    ram[1] = (value >> 8) & 0xff;
    ram[2] = (value >> 16) & 0xff;
    ram[3] = (value >> 24) & 0xff;
    printf("%#010x\n", ram[0]);
    printf("%#010x\n", ram[1]);
    printf("%#010x\n", ram[2]);
    printf("%#010x\n", ram[3]);

    g = 3;
    uint32_t temp = 0xaaaaaaaa;
    uint32_t data = (temp & ((uint32_t)0xffffffff << (8 * (g + 1)))) | ((value & 0xff) << (8 * g)) | (temp & ((uint32_t)0xffffffff >> (8 * (4 - g))));
    printf("%#010x\n", value);
    printf("%#010x\n", data);
    printf("%#010x\n", (temp & ((uint32_t)0xffffffff << (8 * (g + 1)))));
    printf("%#010x\n", ((value & 0xff) << (8 * g)));
    printf("%#010x\n", (temp & ((uint32_t)0xffffffff >> (8 * (4 - g)))));
    printf("%#010x\n", ((uint32_t)0xffffffff >> 24));
}