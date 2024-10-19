#include "renderer.h"
#include "time.h"
#include "util.h"

extern State state;

void check_shader_status(GLuint shader_id) {
  GLint status;
  glGetShaderiv(shader_id, GL_COMPILE_STATUS, &status);
  if (status != GL_TRUE) {
    GLint log_length;
    glGetShaderiv(shader_id, GL_INFO_LOG_LENGTH, &log_length);

    GLchar buffer[log_length];

    GLsizei buffer_size;
    glGetShaderInfoLog(shader_id, log_length, &buffer_size, buffer);

    printf("Shader compile Error: %s", buffer);

    exit(1);
  }
}

void shader_init(GLuint *program_id, char *fs, char *vs) {
  char *fragment_shader = read_file(fs);
  char *vertex_shader = read_file(vs);

  GLuint fragment_shader_id = glCreateShader(GL_FRAGMENT_SHADER);
  GLuint vertex_shader_id = glCreateShader(GL_VERTEX_SHADER);

  const char *fragment_shader_source[] = {fragment_shader};
  const char *vertex_shader_source[] = {vertex_shader};

  glShaderSource(fragment_shader_id, 1, fragment_shader_source, NULL);
  glShaderSource(vertex_shader_id, 1, vertex_shader_source, NULL);

  glCompileShader(fragment_shader_id);
  glCompileShader(vertex_shader_id);

  check_shader_status(fragment_shader_id);
  check_shader_status(vertex_shader_id);

  *program_id = glCreateProgram();

  glAttachShader(*program_id, fragment_shader_id);
  glAttachShader(*program_id, vertex_shader_id);

  glLinkProgram(*program_id);

  glDeleteShader(fragment_shader_id);
  glDeleteShader(vertex_shader_id);

  free(fragment_shader);
  free(vertex_shader);
}

void renderer_init() {
  Renderer *renderer = &state.renderer;

  renderer->delta_time = 0.0f;
  renderer->last_frame_time = glfwGetTime();
  renderer->wireframe_enabled = false;

  shader_init(&renderer->program_id, "shaders/basic.fs", "shaders/basic.vs");

  renderer->model_location =
      glGetUniformLocation(renderer->program_id, "model");
  renderer->view_location = glGetUniformLocation(renderer->program_id, "view");
  renderer->projection_location =
      glGetUniformLocation(renderer->program_id, "projection");

  glGenVertexArrays(1, &renderer->vao_id);
  glGenBuffers(1, &renderer->vbo_id);
  glGenBuffers(1, &renderer->ibo_id);
  glGenBuffers(1, &renderer->instance_vbo_id);
}

void renderer_update() {
  Renderer *renderer = &state.renderer;
  Camera *camera = &state.camera;

  float current_frame_time = glfwGetTime();
  renderer->delta_time = current_frame_time - renderer->last_frame_time;
  renderer->last_frame_time = current_frame_time;

  mat4 model = GLM_MAT4_IDENTITY_INIT;

  mat4 view = GLM_MAT4_IDENTITY_INIT;

  vec3 center;
  glm_vec3_add(camera->position, camera->front, center);

  glm_lookat(camera->position, center, camera->up, view);

  mat4 projection = GLM_MAT4_IDENTITY_INIT;
  glm_perspective(glm_rad(camera->fov), 1600.0f / 900.0f, 0.1f, 5000.0f,
                  projection);

  glUniformMatrix4fv(renderer->model_location, 1, GL_FALSE,
                     (const GLfloat *)model);
  glUniformMatrix4fv(renderer->view_location, 1, GL_FALSE,
                     (const GLfloat *)view);
  glUniformMatrix4fv(renderer->projection_location, 1, GL_FALSE,
                     (const GLfloat *)projection);
}

void renderer_prepare() {
  glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
  glClear(GL_COLOR_BUFFER_BIT);
  glUseProgram(state.renderer.program_id);

  if (state.renderer.wireframe_enabled == true) {
    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
  } else {
    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
  }
}

void renderer_toogle_wireframe() {
  Renderer *renderer = &state.renderer;

  if (renderer->wireframe_enabled == true) {
    renderer->wireframe_enabled = false;
  } else {
    renderer->wireframe_enabled = true;
  }
}
