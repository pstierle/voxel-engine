#include "window.h"
#include "camera.h"
#include "renderer.h"
#include <stdio.h>
#include <stdlib.h>

extern State state;

void window_mouse_callback(GLFWwindow *window, double x_pos, double y_pos) {
  Mouse *mouse = &state.window.mouse;

  (void)window;

  if (mouse->first_move_handled == false) {
    mouse->x = x_pos;
    mouse->y = y_pos;
    mouse->first_move_handled = true;
  }

  camera_mouse_move(mouse->x, x_pos, mouse->y, y_pos);

  mouse->x = x_pos;
  mouse->y = y_pos;
}

void window_scroll_callback(GLFWwindow *window, double x_offset,
                            double y_offset) {
  (void)window;
  (void)x_offset;

  camera_mouse_scroll((float)y_offset);
}

void window_key_callback(GLFWwindow *window, int key, int scancode, int action,
                         int mods) {
  (void)window;
  (void)scancode;
  (void)mods;

  if (key == GLFW_KEY_U && action == GLFW_PRESS) {
    renderer_toogle_wireframe();
  }
}

void window_init() {
  Window *window = &state.window;
  Keyboard *keyboard = &state.window.keyboard;
  Mouse *mouse = &state.window.mouse;

  window->width = 1600;
  window->height = 900;

  mouse->x = window->width / 2;
  mouse->y = window->height / 2;
  mouse->first_move_handled = false;

  keyboard->w_pressed = false;
  keyboard->a_pressed = false;
  keyboard->s_pressed = false;
  keyboard->d_pressed = false;

  if (!glfwInit()) {
    printf("Failed to initialize GLFW\n");
    exit(1);
  }

  glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
  glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
  glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

  window->handle =
      glfwCreateWindow(window->width, window->height, "Game", NULL, NULL);

  if (!window->handle) {
    printf("Failed to create GLFW window\n");
    exit(1);
  }

  glfwMakeContextCurrent(window->handle);

  if (!gladLoadGLLoader((GLADloadproc)glfwGetProcAddress)) {
    printf("Failed to initialize GLAD\n");
    exit(1);
  }

  glfwSetInputMode(window->handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

  glfwSetCursorPosCallback(window->handle, window_mouse_callback);
  glfwSetScrollCallback(window->handle, window_scroll_callback);
  glfwSetKeyCallback(window->handle, window_key_callback);

  glfwSetInputMode(window->handle, GLFW_LOCK_KEY_MODS, GLFW_TRUE);
}

void window_input() {
  glfwPollEvents();

  Keyboard *keyboard = &state.window.keyboard;

  if (glfwGetKey(state.window.handle, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
    glfwSetWindowShouldClose(state.window.handle, true);
    return;
  }
  if (glfwGetKey(state.window.handle, GLFW_KEY_W) == GLFW_PRESS) {
    keyboard->w_pressed = true;
  }
  if (glfwGetKey(state.window.handle, GLFW_KEY_S) == GLFW_PRESS) {
    keyboard->s_pressed = true;
  }
  if (glfwGetKey(state.window.handle, GLFW_KEY_A) == GLFW_PRESS) {
    keyboard->a_pressed = true;
  }
  if (glfwGetKey(state.window.handle, GLFW_KEY_D) == GLFW_PRESS) {
    keyboard->d_pressed = true;
  }

  if (glfwGetKey(state.window.handle, GLFW_KEY_W) == GLFW_RELEASE) {
    keyboard->w_pressed = false;
  }
  if (glfwGetKey(state.window.handle, GLFW_KEY_S) == GLFW_RELEASE) {
    keyboard->s_pressed = false;
  }
  if (glfwGetKey(state.window.handle, GLFW_KEY_A) == GLFW_RELEASE) {
    keyboard->a_pressed = false;
  }
  if (glfwGetKey(state.window.handle, GLFW_KEY_D) == GLFW_RELEASE) {
    keyboard->d_pressed = false;
  }
}
