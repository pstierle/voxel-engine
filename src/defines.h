#pragma once

#include "cglm/cglm.h"
#include "gfx.h"

#define CAMERA_SPEED 300.0f
#define MOUSE_SENSITIVITY 0.2f

#define CHUNKS_COUNT 200
#define CHUNK_SIZE 200
#define RENDER_DISTANCE 500.0f

typedef struct Chunk {
  vec3 position;
  vec3 *cube_positions;
  int cube_positions_size;
  bool visible;
} Chunk;

typedef struct Camera {
  vec3 position;
  vec3 front;
  vec3 up;

  float fov;
  float yaw;
  float pitch;
} Camera;

typedef struct Renderer {
  float delta_time;
  float last_frame_time;

  bool wireframe_enabled;

  GLuint program_id;

  GLuint ibo_id;
  GLuint vbo_id;
  GLuint vao_id;
  GLuint instance_vbo_id;

  GLuint model_location;
  GLuint view_location;
  GLuint projection_location;

  int ibo_num_indices;
  int num_instances;
} Renderer;

typedef struct Mouse {
  float x;
  float y;
  bool first_move_handled;
} Mouse;

typedef struct Keyboard {
  bool w_pressed;
  bool a_pressed;
  bool s_pressed;
  bool d_pressed;
} Keyboard;

typedef struct Window {
  GLFWwindow *handle;
  int width, height;
  Mouse mouse;
  Keyboard keyboard;
} Window;

typedef struct State {
  Window window;
  Renderer renderer;
  Camera camera;
  Chunk *chunks;
} State;

extern struct State state;
