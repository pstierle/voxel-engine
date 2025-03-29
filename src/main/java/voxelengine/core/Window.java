package voxelengine.core;

import org.joml.Vector2d;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
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
    private long handle;
    private int width = 1600;
    private int height = 900;
    private final Vector2d mousePosition = new Vector2d((double) this.width / 2, (double) this.height / 2);
    private boolean firstMouseMoveHandled = false;
    private boolean cursorDisabled = true;

    public boolean wPressed = false;
    public boolean aPressed = false;
    public boolean sPressed = false;
    public boolean dPressed = false;
    public boolean spacePressed = false;

    public boolean isCursorDisabled() {
        return cursorDisabled;
    }

    public long getHandle() {
        return handle;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void toggleCursor() {
        cursorDisabled = !cursorDisabled;
        glfwSetInputMode(this.handle, GLFW_CURSOR, cursorDisabled ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
    }

    public void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        this.handle = glfwCreateWindow(this.width, this.height, "Voxel Engine", NULL, NULL);

        if (this.handle == NULL) {
            throw new IllegalStateException("Unable to create the GLFW window");
        }

        glfwMakeContextCurrent(this.handle);
        GL.createCapabilities();
        glfwSetInputMode(this.handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetInputMode(this.handle, GLFW_LOCK_KEY_MODS, GLFW_TRUE);

        glfwSetCursorPosCallback(this.handle, (long window, double xpos, double ypos) -> {
            if (!this.firstMouseMoveHandled) {
                this.mousePosition.set(xpos, ypos);
                this.firstMouseMoveHandled = true;
            }

            // Only update camera if cursor is disabled (in game mode)
            if (cursorDisabled) {
                State.camera.handleMouseMove(this.mousePosition.x, xpos, this.mousePosition.y, ypos);
            }

            this.mousePosition.set(xpos, ypos);
        });

        glfwSetKeyCallback(this.handle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
            if (key == GLFW_KEY_U && action == GLFW_RELEASE) {
                State.renderer.toggleWireframe();
            }
            if (key == GLFW_KEY_O && action == GLFW_RELEASE) {
                State.physics.togglePhysics();
            }
            if (key == GLFW_KEY_P && action == GLFW_RELEASE) {
                toggleCursor();
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

        // Only process movement inputs if cursor is disabled (in game mode)
        if (cursorDisabled) {
            if (glfwGetKey(this.handle, GLFW_KEY_W) == GLFW_PRESS) {
                this.wPressed = true;
            }
            if (glfwGetKey(this.handle, GLFW_KEY_S) == GLFW_PRESS) {
                this.sPressed = true;
            }
            if (glfwGetKey(this.handle, GLFW_KEY_A) == GLFW_PRESS) {
                this.aPressed = true;
            }
            if (glfwGetKey(this.handle, GLFW_KEY_D) == GLFW_PRESS) {
                this.dPressed = true;
            }
            if (glfwGetKey(this.handle, GLFW_KEY_SPACE) == GLFW_PRESS) {
                this.spacePressed = true;
            }

            if (glfwGetKey(this.handle, GLFW_KEY_W) == GLFW_RELEASE) {
                this.wPressed = false;
            }
            if (glfwGetKey(this.handle, GLFW_KEY_S) == GLFW_RELEASE) {
                this.sPressed = false;
            }
            if (glfwGetKey(this.handle, GLFW_KEY_A) == GLFW_RELEASE) {
                this.aPressed = false;
            }
            if (glfwGetKey(this.handle, GLFW_KEY_D) == GLFW_RELEASE) {
                this.dPressed = false;
            }
            if (glfwGetKey(this.handle, GLFW_KEY_SPACE) == GLFW_RELEASE) {
                this.spacePressed = false;
            }
        } else {
            this.wPressed = false;
            this.sPressed = false;
            this.aPressed = false;
            this.dPressed = false;
            this.spacePressed = false;
        }
    }

    public void destroy() {
        glfwFreeCallbacks(this.handle);
        glfwDestroyWindow(this.handle);
        glfwTerminate();
    }
}
