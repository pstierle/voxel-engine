package voxelengine.window.input;

public class Keyboard {
    private boolean wPressed = false;
    private boolean aPressed = false;
    private boolean sPressed = false;
    private boolean dPressed = false;

    public boolean isWPressed() {
        return wPressed;
    }

    public void setWPressed(boolean wPressed) {
        this.wPressed = wPressed;
    }

    public boolean isAPressed() {
        return aPressed;
    }

    public void setAPressed(boolean aPressed) {
        this.aPressed = aPressed;
    }

    public boolean isSPressed() {
        return sPressed;
    }

    public void setSPressed(boolean sPressed) {
        this.sPressed = sPressed;
    }

    public boolean isDPressed() {
        return dPressed;
    }

    public void setDPressed(boolean dPressed) {
        this.dPressed = dPressed;
    }
}
