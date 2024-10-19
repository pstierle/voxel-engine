#pragma once

#include "defines.h"
#include "fast-noise/noise.h"

Chunk *chunk_create(fnl_state noise, int world_offset_x, int world_offset_z);
void chunk_update(Chunk *chunk);
void chunk_render(Chunk *chunk);
