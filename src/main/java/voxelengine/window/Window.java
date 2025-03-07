package voxelengine.window;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import voxelengine.core.Camera;
import voxelengine.core.Renderer;
import voxelengine.window.input.Keyboard;
import voxelengine.window.input.Mouse;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_U;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_LOCK_KEY_MODS;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL46.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL46.glEnable;
import static org.lwjgl.opengl.GL46.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    public long handle;
    public int width = 1600;
    public int height = 900;
    public Mouse mouse = new Mouse(this.width / 2, this.height / 2);
    public Keyboard keyboard = new Keyboard();
    public Camera camera;
    public Renderer renderer;

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        this.handle = glfwCreateWindow(this.width, this.height, "Voxel Engine", NULL, NULL);

        if (this.handle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(this.handle);
        GL.createCapabilities();
        glfwSetInputMode(this.handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetInputMode(this.handle, GLFW_LOCK_KEY_MODS, GLFW_TRUE);

        glfwSetCursorPosCallback(this.handle, (long window, double xpos, double ypos) -> {
            if (this.mouse.firstMouseMoveHandled == false) {
                this.mouse.position.x = xpos;
                this.mouse.position.y = ypos;
                this.mouse.firstMouseMoveHandled = true;
            }

            this.camera.handleMouseMove(this.mouse.position.x, xpos, this.mouse.position.y, ypos);

            this.mouse.position.x = xpos;
            this.mouse.position.y = ypos;
        });

        glfwSetKeyCallback(this.handle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
            if (key == GLFW_KEY_U && action == GLFW_RELEASE) {
                this.renderer.toggleWireframe();
            }
        });

        glfwSetFramebufferSizeCallback(this.handle, (long windowHandle, int newWidth, int newHeight) -> {
            this.width = newWidth;
            this.height = newHeight;
            glViewport(0, 0, this.width, this.height);
        });

        glfwSwapInterval(0);
        glEnable(GL_DEPTH_TEST);
    }

    public void input() {
        glfwPollEvents();

        if (glfwGetKey(this.handle, GLFW_KEY_W) == GLFW_PRESS) {
            this.keyboard.wPressed = true;
        }
        if (glfwGetKey(this.handle, GLFW_KEY_S) == GLFW_PRESS) {
            this.keyboard.sPressed = true;
        }
        if (glfwGetKey(this.handle, GLFW_KEY_A) == GLFW_PRESS) {
            this.keyboard.aPressed = true;
        }
        if (glfwGetKey(this.handle, GLFW_KEY_D) == GLFW_PRESS) {
            this.keyboard.dPressed = true;
        }

        if (glfwGetKey(this.handle, GLFW_KEY_W) == GLFW_RELEASE) {
            this.keyboard.wPressed = false;
        }
        if (glfwGetKey(this.handle, GLFW_KEY_S) == GLFW_RELEASE) {
            this.keyboard.sPressed = false;
        }
        if (glfwGetKey(this.handle, GLFW_KEY_A) == GLFW_RELEASE) {
            this.keyboard.aPressed = false;
        }
        if (glfwGetKey(this.handle, GLFW_KEY_D) == GLFW_RELEASE) {
            this.keyboard.dPressed = false;
        }
    }

    public void destroy() {
        glfwFreeCallbacks(this.handle);
        glfwDestroyWindow(this.handle);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
