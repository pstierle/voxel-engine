package voxelengine.core;

import org.joml.Vector3d;
import voxelengine.examples.World;
import voxelengine.util.Chunk;
import voxelengine.util.Vector3Key;

public class Physics {
    private static final double GRAVITY = -25;
    private static final double TERMINAL_VELOCITY = -150.0;

    private static final double PLAYER_HEIGHT = 8.0;
    private static final double PLAYER_WIDTH = 2;

    private static final double MAX_PLAYER_SPEED = 25.0;
    private static final double ACCELERATION = 150.0;
    private static final double DECELERATION = 40.0;
    private static final double JUMP_STRENGTH = 20.0;
    private static final double AIR_CONTROL_FACTOR = 0.8;

    private final Vector3d velocity = new Vector3d(0, 0, 0);
    private final Vector3d currentVelocity = new Vector3d(0, 0, 0);
    private final Vector3d moveDirection = new Vector3d();
    private boolean isPhysicsEnabled = false;
    private boolean isGrounded = false;


    public boolean isPhysicsEnabled() {
        return isPhysicsEnabled;
    }

    public void togglePhysics() {
        isPhysicsEnabled = !isPhysicsEnabled;
        if (isPhysicsEnabled) {
            velocity.set(0, 0, 0);
            currentVelocity.set(0, 0, 0);
        }
    }

    private void applyMovement(Vector3d inputDirection) {
        if (!isPhysicsEnabled) return;

        double deltaTime = State.renderer.getDeltaTime();
        double speedFactor = isGrounded ? 1.0 : AIR_CONTROL_FACTOR;

        Vector3d normalizedInput = new Vector3d(inputDirection);
        if (normalizedInput.length() > 1.0) {
            normalizedInput.normalize();
        }

        Vector3d targetVelocity = new Vector3d(normalizedInput).mul(MAX_PLAYER_SPEED * speedFactor);

        Vector3d velocityDiff = new Vector3d(targetVelocity).sub(currentVelocity);
        double accelerationThisFrame = isGrounded ? ACCELERATION : (ACCELERATION * AIR_CONTROL_FACTOR);

        if (velocityDiff.length() > 0) {
            velocityDiff.normalize().mul(accelerationThisFrame * deltaTime);
            currentVelocity.add(velocityDiff);
        }

        if (inputDirection.length() == 0) {
            double decelerationThisFrame = isGrounded ? DECELERATION : (DECELERATION * AIR_CONTROL_FACTOR);
            currentVelocity.mul(Math.pow(1 - (decelerationThisFrame * deltaTime), 60 * deltaTime));
        }

        if (currentVelocity.length() > MAX_PLAYER_SPEED) {
            currentVelocity.normalize().mul(MAX_PLAYER_SPEED);
        }

        Vector3d moveAttempt = new Vector3d(currentVelocity).mul(deltaTime);
        Vector3d newPosition = new Vector3d(State.camera.getPosition());
        newPosition.add(moveAttempt);

        if (!isColliding(newPosition)) {
            State.camera.getPosition().add(moveAttempt);
        } else {
            handleCollisionSliding(moveAttempt);
        }
    }

    private void handleCollisionSliding(Vector3d attemptedMove) {
        Vector3d xMove = new Vector3d(attemptedMove.x, 0, 0);
        Vector3d zMove = new Vector3d(0, 0, attemptedMove.z);
        Vector3d currentPos = State.camera.getPosition();

        Vector3d xTestPos = new Vector3d(currentPos).add(xMove);
        if (!isColliding(xTestPos)) {
            currentPos.add(xMove);
        }

        Vector3d zTestPos = new Vector3d(currentPos).add(zMove);
        if (!isColliding(zTestPos)) {
            currentPos.add(zMove);
        }
    }

    public void jump(double jumpStrength) {
        if (!isPhysicsEnabled || !isGrounded) return;

        velocity.y = jumpStrength;
    }

    public void update() {
        if (!isPhysicsEnabled) return;

        moveDirection.set(0, 0, 0);

        Vector3d right = new Vector3d();
        State.camera.getFront().cross(State.camera.getUp(), right).normalize();

        if (State.window.wPressed) {
            Vector3d forwardMove = new Vector3d(State.camera.getFront());
            forwardMove.y = 0;
            forwardMove.normalize();
            moveDirection.add(forwardMove);
        }
        if (State.window.sPressed) {
            Vector3d backwardMove = new Vector3d(State.camera.getFront());
            backwardMove.y = 0;
            backwardMove.normalize();
            moveDirection.sub(backwardMove);
        }

        if (State.window.dPressed) {
            right.normalize();
            moveDirection.add(right);
        }
        if (State.window.aPressed) {
            right.normalize();
            moveDirection.sub(right);
        }

        applyMovement(moveDirection);

        if (State.window.spacePressed) {
            jump(JUMP_STRENGTH);
        }

        velocity.y += GRAVITY * State.renderer.getDeltaTime();
        velocity.y = Math.max(velocity.y, TERMINAL_VELOCITY);

        Vector3d newPosition = new Vector3d(State.camera.getPosition());
        newPosition.add(velocity.mul(State.renderer.getDeltaTime(), new Vector3d()));

        if (isColliding(newPosition)) {
            if (velocity.y < 0) {
                velocity.y = 0;
                isGrounded = true;
            }
        } else {
            State.camera.getPosition().set(newPosition);
            isGrounded = false;
        }
    }

    private boolean isColliding(Vector3d position) {
        double[][] offsets = {
                // Bottom corners
                {-PLAYER_WIDTH / 2, -PLAYER_HEIGHT, -PLAYER_WIDTH / 2},
                {PLAYER_WIDTH / 2, -PLAYER_HEIGHT, -PLAYER_WIDTH / 2},
                {-PLAYER_WIDTH / 2, -PLAYER_HEIGHT, PLAYER_WIDTH / 2},
                {PLAYER_WIDTH / 2, -PLAYER_HEIGHT, PLAYER_WIDTH / 2},

                // Mid-height corners
                {-PLAYER_WIDTH / 2, PLAYER_HEIGHT / 2, -PLAYER_WIDTH / 2},
                {PLAYER_WIDTH / 2, PLAYER_HEIGHT / 2, -PLAYER_WIDTH / 2},
                {-PLAYER_WIDTH / 2, PLAYER_HEIGHT / 2, PLAYER_WIDTH / 2},
                {PLAYER_WIDTH / 2, PLAYER_HEIGHT / 2, PLAYER_WIDTH / 2},

                // Top corners
                {-PLAYER_WIDTH / 2, PLAYER_HEIGHT, -PLAYER_WIDTH / 2},
                {PLAYER_WIDTH / 2, PLAYER_HEIGHT, -PLAYER_WIDTH / 2},
                {-PLAYER_WIDTH / 2, PLAYER_HEIGHT, PLAYER_WIDTH / 2},
                {PLAYER_WIDTH / 2, PLAYER_HEIGHT, PLAYER_WIDTH / 2},
        };

        for (double[] offset : offsets) {
            Vector3d checkPoint = new Vector3d(
                    position.x + offset[0],
                    position.y + offset[1],
                    position.z + offset[2]
            );

            if (isBlockAtPosition(checkPoint)) {
                return true;
            }
        }

        return false;
    }

    private boolean isBlockAtPosition(Vector3d position) {
        int blockX = (int) Math.floor(position.x);
        int blockY = (int) Math.floor(position.y);
        int blockZ = (int) Math.floor(position.z);

        int chunkX = (int) Math.floor((double) blockX / Chunk.CHUNK_SIZE) * Chunk.CHUNK_SIZE;
        int chunkY = (int) Math.floor((double) blockY / Chunk.CHUNK_SIZE) * Chunk.CHUNK_SIZE;
        int chunkZ = (int) Math.floor((double) blockZ / Chunk.CHUNK_SIZE) * Chunk.CHUNK_SIZE;

        Vector3Key chunkKey = new Vector3Key(chunkX, chunkY, chunkZ);
        Chunk chunk = World.chunks.get(chunkKey);

        if (chunk == null) return false;

        Integer[][][] chunkData = chunk.getData();
        if (chunkData == null) return false;

        int localX = blockX - chunkX;
        int localY = blockY - chunkY;
        int localZ = blockZ - chunkZ;

        if (localX < 0 || localX >= Chunk.CHUNK_SIZE ||
                localY < 0 || localY >= Chunk.CHUNK_SIZE ||
                localZ < 0 || localZ >= Chunk.CHUNK_SIZE) {
            return false;
        }

        return chunkData[localX][localY][localZ] != null;
    }
}