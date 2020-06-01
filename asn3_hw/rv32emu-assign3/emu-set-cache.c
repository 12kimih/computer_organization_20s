#include <stdio.h>
#include <sys/types.h>
#include <stdint.h>
#include <assert.h>
#include "emu-rv32i.h"
#include "emu-cache.h"

// Address in 4-Way Set-associative Cache
//
//   TAG   |      INDEX                         | OFFSET
//         | log(NUM_CACHE_BLOCKS)-log(NUM_WAY) | G
// 26 bits |     4 bits                         | 2 bits

#define NUM_WAY 4
#define INDEX_BIT_WIDTH 4
#define TAG_BIT_WIDTH (ADDR_BIT_WIDTH - G_BIT_WIDTH - INDEX_BIT_WIDTH)

struct cache_block
{
  uint32_t tag:TAG_BIT_WIDTH;
  uint32_t valid:1;
  uint32_t data;
  uint32_t counter:2;
};

struct cache_block cache[NUM_CACHE_BLOCKS];
uint8_t dirty[NUM_CACHE_BLOCKS];

void cache_init(void)
{
  num_cache_hit = 0;
  num_cache_miss = 0;

  for(uint32_t idx=0; idx<NUM_CACHE_BLOCKS; idx++) {
    cache[idx].valid = 0;
    cache[idx].counter = 3;
    dirty[idx] = 0;
  }
  return;
}

uint32_t cache_read(uint32_t addr)
{
  // TODO: Assignment #3
  uint32_t tag = addr >> 6;
  uint32_t idx = (addr >> 2) & 0xf;
  uint32_t g = addr & 0x3;
  uint32_t i = 0;
  uint32_t lru = 0;
  for (; i < 0x40; i += 0x10) {
    if (cache[idx + i].valid && cache[idx + i].tag == tag) {
      lru = i;
      break;
    }
    if (cache[idx + lru].counter < cache[idx + i].counter) lru = i;
  }
  if (i < 0x40) ++num_cache_hit;
  else {
    ++num_cache_miss;
    if (dirty[idx + lru]) {
      // write previous data back to the lower memory
      uint32_t write_addr = (cache[idx + lru].tag << 6) | (idx << 2);
      ram[write_addr] = cache[idx + lru].data & 0xff;
      ram[write_addr + 1] = (cache[idx + lru].data >> 8) & 0xff;
      ram[write_addr + 2] = (cache[idx + lru].data >> 16) & 0xff;
      ram[write_addr + 3] = (cache[idx + lru].data >> 24) & 0xff;
    }
    // read data from lower memory into the cache block
    uint32_t read_addr = (tag << 6) | (idx << 2);
    cache[idx + lru].tag = tag;
    cache[idx + lru].data = ram[read_addr] | (ram[read_addr + 1] << 8) | (ram[read_addr + 2] << 16) | (ram[read_addr + 3] << 24);
    cache[idx + lru].valid = 1;
    dirty[idx + lru] = 0;
  }
  // set lru counter
  for(i = 0; i < 0x40; i += 0x10) {
    if (cache[idx + i].counter < cache[idx + lru].counter) ++cache[idx + i].counter;
  }
  cache[idx + lru].counter = 0;
  return (cache[idx + lru].data >> (8 * g)) & 0xff;
}

void cache_write(uint32_t addr, uint32_t value)
{
  // TODO: Assignment #3
  uint32_t tag = addr >> 6;
  uint32_t idx = (addr >> 2) & 0xf;
  uint32_t g = addr & 0x3;
  uint32_t i = 0;
  uint32_t lru = 0;
  for (; i < 0x40; i += 0x10) {
    if (cache[idx + i].valid && cache[idx + i].tag == tag) {
      lru = i;
      break;
    }
    if (cache[idx + lru].counter < cache[idx + i].counter) lru = i;
  }
  if (i < 0x40) ++num_cache_hit;
  else {
    ++num_cache_miss;
    if (dirty[idx + lru]) {
      // write previous data back to the lower memory
      uint32_t write_addr = (cache[idx + lru].tag << 6) | (idx << 2);
      ram[write_addr] = cache[idx + lru].data & 0xff;
      ram[write_addr + 1] = (cache[idx + lru].data >> 8) & 0xff;
      ram[write_addr + 2] = (cache[idx + lru].data >> 16) & 0xff;
      ram[write_addr + 3] = (cache[idx + lru].data >> 24) & 0xff;
    }
    // read data from lower memory into the cache block
    uint32_t read_addr = (tag << 6) | (idx << 2);
    cache[idx + lru].tag = tag;
    cache[idx + lru].data = ram[read_addr] | (ram[read_addr + 1] << 8) | (ram[read_addr + 2] << 16) | (ram[read_addr + 3] << 24);
    cache[idx + lru].valid = 1;
  }
  // write the new data into the cache block
  if (g == 0) cache[idx + lru].data = (cache[idx + lru].data & 0xffffff00) | (value & 0xff);
  else if (g == 1) cache[idx + lru].data = (cache[idx + lru].data & 0xffff00ff) | (value & 0xff) << 8;
  else if (g == 2) cache[idx + lru].data = (cache[idx + lru].data & 0xff00ffff) | (value & 0xff) << 16;
  else if (g == 3) cache[idx + lru].data = (cache[idx + lru].data & 0x00ffffff) | (value & 0xff) << 24;
  dirty[idx + lru] = 1;
  // set lru counter
  for(i = 0; i < 0x40; i += 0x10) {
    if (cache[idx + i].counter < cache[idx + lru].counter) ++cache[idx + i].counter;
  }
  cache[idx + lru].counter = 0;
  return;
}
