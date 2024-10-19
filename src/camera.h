#pragma once
#include "defines.h"

void camera_init();
void camera_update();

void camera_mouse_move(float old_x, float new_x, float old_y, float new_y);
void camera_mouse_scroll(float y_offset);
