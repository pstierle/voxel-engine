#define FNL_IMPL
#include "noise.h"

#include "camera.h"
#include "cglm/cglm.h"
#include "defines.h"
#include "renderer.h"
#include "window.h"
#include "world.h"

State state;

int main() {
  window_init();
  renderer_init();
  camera_init();
  world_generate();

  while (!glfwWindowShouldClose(state.window.handle)) {
    // input

    window_input();

    // update

    camera_update();
    renderer_update();
    world_update();

    // render
    renderer_prepare();
    world_render();

    glfwSwapBuffers(state.window.handle);
  }

  glfwTerminate();
  return 0;
}
