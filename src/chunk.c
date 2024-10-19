#include "chunk.h"

extern State state;

Chunk *chunk_create(fnl_state noise, int world_offset_x, int world_offset_z) {
  Chunk *chunk = malloc(sizeof(Chunk));
  chunk->cube_positions = malloc(CHUNK_SIZE * CHUNK_SIZE * sizeof(vec3) * 100);

  chunk->position[0] = world_offset_x + CHUNK_SIZE / 2;
  chunk->position[1] = 0.0f;
  chunk->position[2] = world_offset_z + CHUNK_SIZE / 2;
  chunk->visible = false;

  int cube_positions_size = 0;

  for (int x = 0; x < CHUNK_SIZE; ++x) {
    for (int z = 0; z < CHUNK_SIZE; ++z) {
      float noise_val_y =
          fnlGetNoise3D(&noise, x + world_offset_x, 0, z + world_offset_z);
      float scaled_noise_val_y =
          fmaxf(0.0f, fminf(128.0f, noise_val_y * 60.0f + 64.0f));
      int rounded_noise_val_y = (int)roundf(scaled_noise_val_y);

      chunk->cube_positions[cube_positions_size][0] = x + world_offset_x;
      chunk->cube_positions[cube_positions_size][1] = rounded_noise_val_y;
      chunk->cube_positions[cube_positions_size][2] = z + world_offset_z;

      cube_positions_size++;

      for (int nx = -1; nx <= 1; ++nx) {
        for (int nz = -1; nz <= 1; ++nz) {
          if (nx == 0 && nz == 0)
            continue;

          float neighbor_noise_val_y = fnlGetNoise3D(
              &noise, x + world_offset_x + nx, 0, z + world_offset_z + nz);
          float scaled_neighbor_noise_val_y =
              fmaxf(0.0f, fminf(128.0f, neighbor_noise_val_y * 60.0f + 64.0f));
          int rounded_neighbor_noise_val_y =
              (int)roundf(scaled_neighbor_noise_val_y);

          if (rounded_neighbor_noise_val_y < rounded_noise_val_y - 1) {
            for (int fill_y = rounded_neighbor_noise_val_y + 1;
                 fill_y < rounded_noise_val_y; ++fill_y) {
              chunk->cube_positions[cube_positions_size][0] =
                  x + world_offset_x;
              chunk->cube_positions[cube_positions_size][1] = fill_y;
              chunk->cube_positions[cube_positions_size][2] =
                  z + world_offset_z;
              cube_positions_size++;
            }
          }
        }
      }
    }
  }

  chunk->cube_positions_size = cube_positions_size;

  return chunk;
}

void chunk_update(Chunk *chunk) {
  if (glm_vec3_distance(state.camera.position, chunk->position) <
      RENDER_DISTANCE) {
    chunk->visible = true;
  } else {
    chunk->visible = false;
  }
}

void chunk_render(Chunk *chunk) {

  Renderer *renderer = &state.renderer;

  vec3 cube_positions[] = {{0.5f, 0.5f, 0.5f},    {-0.5f, 0.5f, -0.5f},
                           {-0.5f, 0.5f, 0.5f},   {0.5f, -0.5f, -0.5f},
                           {-0.5f, -0.5f, -0.5f}, {0.5f, 0.5f, -0.5f},
                           {0.5f, -0.5f, 0.5f},   {-0.5f, -0.5f, 0.5f}};

  GLushort cube_indices[] = {0, 1, 2, 1, 3, 4, 5, 6, 3, 7, 3, 6,
                             2, 4, 7, 0, 7, 6, 0, 5, 1, 1, 5, 3,
                             5, 0, 6, 7, 4, 3, 2, 1, 4, 0, 2, 7};

  glBindVertexArray(renderer->vao_id);

  glBindBuffer(GL_ARRAY_BUFFER, renderer->vbo_id);
  glBufferData(GL_ARRAY_BUFFER, sizeof(cube_positions), cube_positions,
               GL_STATIC_DRAW);

  glEnableVertexAttribArray(0);
  glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(float), (void *)0);

  glBindBuffer(GL_ARRAY_BUFFER, renderer->instance_vbo_id);
  glBufferData(GL_ARRAY_BUFFER, chunk->cube_positions_size * sizeof(vec3),
               chunk->cube_positions, GL_DYNAMIC_DRAW);

  glEnableVertexAttribArray(1);
  glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(float), (void *)0);
  glVertexAttribDivisor(1, 1);

  glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, renderer->ibo_id);
  glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(cube_indices), cube_indices,
               GL_STATIC_DRAW);

  renderer->ibo_num_indices = sizeof(cube_indices) / sizeof(GLushort);
  renderer->num_instances = chunk->cube_positions_size;

  glBindVertexArray(state.renderer.vao_id);
  glDrawElementsInstanced(GL_TRIANGLES, state.renderer.ibo_num_indices,
                          GL_UNSIGNED_SHORT, 0, state.renderer.num_instances);
}
