#include "util.h"
#include <stdio.h>
#include <stdlib.h>

char *read_file(char path[]) {
  FILE *file;
  char *buffer = NULL;
  int buffer_size = 0;
  int i = 0;
  file = fopen(path, "r");

  if (file == NULL) {
    printf("Error: Failed to open file '%s'.\n", path);
    exit(1);
  }

  int character;
  while ((character = fgetc(file)) != EOF) {
    if (i >= buffer_size) {
      buffer_size += 1000;
      buffer = realloc(buffer, buffer_size + 1);
      if (buffer == NULL) {
        printf("Error: while reading file.\n");
        fclose(file);
        exit(1);
      }
    }
    buffer[i] = character;
    i++;
  }

  buffer[i] = '\0';

  fclose(file);

  return buffer;
}

void print_f(float v) { printf("%f \n", v); }

void print_i(int v) { printf("%i \n", v); }

void print_d(double v) { printf("%lf \n", v); }

void print_c(char v) { printf("%c \n", v); }

void print_s(char *v) { printf("%s \n", v); }
