#include <stdio.h>
#include <sys/types.h>
#include <stdint.h>
#include "emu-rv32i.h"
#include "emu-cache.h"

// Address in Direct Mapped Cache
//
//   TAG   |      INDEX            | OFFSET
//   TAG   | log(NUM_CACHE_BLOCKS) |   G
// 24 bits |     6 bits            | 2 bits

#define INDEX_BIT_WIDTH 6
#define TAG_BIT_WIDTH (ADDR_BIT_WIDTH - G_BIT_WIDTH - INDEX_BIT_WIDTH)

struct cache_block
{
  uint32_t tag:TAG_BIT_WIDTH;
  uint32_t valid:1;
  uint32_t data;
};

struct cache_block cache[NUM_CACHE_BLOCKS];
uint8_t dirty[NUM_CACHE_BLOCKS];

void cache_init(void)
{
  num_cache_hit = 0;
  num_cache_miss = 0;

  for(uint32_t idx=0; idx<NUM_CACHE_BLOCKS; idx++) {
    cache[idx].valid = 0;
    dirty[idx] = 0;
  }
  return;
}

// write-back, write-allocate
uint32_t cache_read(uint32_t addr)
{
  // TODO: Assignment #3
  uint32_t tag = addr >> 8;
  uint32_t idx = (addr >> 2) & 0x3f;
  uint32_t g = addr & 0x3;
  if (cache[idx].valid && cache[idx].tag == tag) ++num_cache_hit;
  else {
    ++num_cache_miss;
    if (dirty[idx]) {
      // write previous data back to the lower memory
      uint32_t write_addr = (cache[idx].tag << 8) | (idx << 2);
      ram[write_addr] = cache[idx].data & 0xff;
      ram[write_addr + 1] = (cache[idx].data >> 8) & 0xff;
      ram[write_addr + 2] = (cache[idx].data >> 16) & 0xff;
      ram[write_addr + 3] = (cache[idx].data >> 24) & 0xff;
    }
    // read data from lower memory into the cache block
    uint32_t read_addr = (tag << 8) | (idx << 2);
    cache[idx].tag = tag;
    cache[idx].data = ram[read_addr] | (ram[read_addr + 1] << 8) | (ram[read_addr + 2] << 16) | (ram[read_addr + 3] << 24);
    cache[idx].valid = 1;
    dirty[idx] = 0;
  }
  return (cache[idx].data >> (8 * g)) & 0xff;
}

void cache_write(uint32_t addr, uint32_t value)
{
  // TODO: Assignment #3
  uint32_t tag = addr >> 8;
  uint32_t idx = (addr >> 2) & 0x3f;
  uint32_t g = addr & 0x3;
  if (cache[idx].valid && cache[idx].tag == tag) ++num_cache_hit;
  else {
    ++num_cache_miss;
    if (dirty[idx]) {
      // write previous data back to the lower memory
      uint32_t write_addr = (cache[idx].tag << 8) | (idx << 2);
      ram[write_addr] = cache[idx].data & 0xff;
      ram[write_addr + 1] = (cache[idx].data >> 8) & 0xff;
      ram[write_addr + 2] = (cache[idx].data >> 16) & 0xff;
      ram[write_addr + 3] = (cache[idx].data >> 24) & 0xff;
    }
    // read data from lower memory into the cache block
    uint32_t read_addr = (tag << 8) | (idx << 2);
    cache[idx].tag = tag;
    cache[idx].data = ram[read_addr] | (ram[read_addr + 1] << 8) | (ram[read_addr + 2] << 16) | (ram[read_addr + 3] << 24);
    cache[idx].valid = 1;
  }
  // write the new data into the cache block
  if (g == 0) cache[idx].data = (cache[idx].data & 0xffffff00) | (value & 0xff);
  else if (g == 1) cache[idx].data = (cache[idx].data & 0xffff00ff) | (value & 0xff) << 8;
  else if (g == 2) cache[idx].data = (cache[idx].data & 0xff00ffff) | (value & 0xff) << 16;
  else if (g == 3) cache[idx].data = (cache[idx].data & 0x00ffffff) | (value & 0xff) << 24;
  dirty[idx] = 1;
  return;
}

/*
// write-through, write-no-allocate
uint32_t cache_read(uint32_t addr) {
  // TODO: Assignment #3
  uint32_t tag = addr >> 8;
  uint32_t idx = (addr >> 2) & 0x3f;
  uint32_t g = addr & 0x3;
  if (cache[idx].valid && cache[idx].tag == tag) ++num_cache_hit;
  else {
    ++num_cache_miss;
    // read data from lower memory into the cache block
    uint32_t read_addr = (tag << 8) | (idx << 2);
    cache[idx].tag = tag;
    cache[idx].data = ram[read_addr] | (ram[read_addr + 1] << 8) | (ram[read_addr + 2] << 16) | (ram[read_addr + 3] << 24);
    cache[idx].valid = 1;
  }
  return (cache[idx].data >> (8 * g)) & 0xff;
}

void cache_write(uint32_t addr, uint32_t value) {
  // TODO: Assignment #3
  uint32_t tag = addr >> 8;
  uint32_t idx = (addr >> 2) & 0x3f;
  uint32_t g = addr & 0x3;
  if (cache[idx].valid && cache[idx].tag == tag) {
    ++num_cache_hit;
    // write the new data into the cache block
    if (g == 0) cache[idx].data = (cache[idx].data & 0xffffff00) | (value & 0xff);
    else if (g == 1) cache[idx].data = (cache[idx].data & 0xffff00ff) | (value & 0xff) << 8;
    else if (g == 2) cache[idx].data = (cache[idx].data & 0xff00ffff) | (value & 0xff) << 16;
    else if (g == 3) cache[idx].data = (cache[idx].data & 0x00ffffff) | (value & 0xff) << 24;
  } else ++num_cache_miss;
  // write the new data into the lower memory
  ram[addr] = value & 0xff;
  return;
}
*/
