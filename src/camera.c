#include "camera.h"

extern State state;

void camera_init() {
  Camera *camera = &state.camera;

  glm_vec3_copy((vec3){200.0f, 50.0f, 200.0f}, camera->position);
  glm_vec3_copy((vec3){0.0f, 0.0f, -1.0f}, camera->front);
  glm_vec3_copy((vec3){0.0f, 1.0f, 0.0f}, camera->up);

  camera->yaw = -90.0f;
  camera->pitch = -10.0f;
  camera->fov = 45.0f;
}

void camera_update() {
  Camera *camera = &state.camera;
  Keyboard *keyboard = &state.window.keyboard;
  float delta_time = state.renderer.delta_time;

  if (keyboard->w_pressed) {
    vec3 intermediate;
    glm_vec3_scale(camera->front, CAMERA_SPEED * delta_time, intermediate);
    glm_vec3_add(camera->position, intermediate, camera->position);
  }
  if (keyboard->s_pressed) {
    vec3 intermediate;
    glm_vec3_scale(camera->front, CAMERA_SPEED * delta_time, intermediate);
    glm_vec3_sub(camera->position, intermediate, camera->position);
  }
  if (keyboard->d_pressed) {
    vec3 intermediate;
    glm_vec3_cross(camera->front, camera->up, intermediate);
    glm_vec3_normalize(intermediate);
    glm_vec3_scale(intermediate, CAMERA_SPEED * delta_time, intermediate);
    glm_vec3_add(camera->position, intermediate, camera->position);
  }
  if (keyboard->a_pressed) {
    vec3 intermediate;
    glm_vec3_cross(camera->front, camera->up, intermediate);
    glm_vec3_normalize(intermediate);
    glm_vec3_scale(intermediate, CAMERA_SPEED * delta_time, intermediate);
    glm_vec3_sub(camera->position, intermediate, camera->position);
  }

  float dir_x = cos(glm_rad(camera->yaw)) * cos(glm_rad(camera->pitch));
  float dir_y = sin(glm_rad(camera->pitch));
  float dir_z = sin(glm_rad(camera->yaw)) * cos(glm_rad(camera->pitch));

  vec3 direction = {dir_x, dir_y, dir_z};

  glm_vec3_normalize(direction);
  camera->front[0] = direction[0];
  camera->front[1] = direction[1];
  camera->front[2] = direction[2];
}

void camera_mouse_move(float old_x, float new_x, float old_y, float new_y) {
  Camera *camera = &state.camera;

  float x_offset = new_x - old_x;
  float y_offset = old_y - new_y;

  x_offset *= MOUSE_SENSITIVITY;
  y_offset *= MOUSE_SENSITIVITY;

  camera->yaw += x_offset;
  camera->pitch += y_offset;

  if (camera->pitch > 89.0f)
    camera->pitch = 89.0f;
  if (camera->pitch < -89.0f)
    camera->pitch = -89.0f;
}

void camera_mouse_scroll(float y_offset) {
  Camera *camera = &state.camera;

  camera->fov -= y_offset;
  if (camera->fov < 1.0f)
    camera->fov = 1.0f;
  if (camera->fov > 90.0f)
    camera->fov = 90.0f;
}
