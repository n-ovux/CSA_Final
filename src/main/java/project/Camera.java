package project;

import org.joml.*;

import java.lang.Math;

public class Camera {
  private float lastX;
  private float lastY;
  private float yaw;
  private float pitch;
  private float speed;
  private float fov;
  private boolean firstMouse;
  private Vector3f position;
  private Vector3f front;
  private Vector3f up;

  enum Direction {
    FORWARD,
    BACKWARD,
    LEFT,
    RIGHT
  }

  public Camera(float x, float y, float z, float fov) {
    position = new Vector3f(x, y, z);
    front = new Vector3f(0.0f, 0.0f, 0.0f);
    up = new Vector3f(0.0f, 1.0f, 0.0f);
    firstMouse = true;
    yaw = 0;
    pitch = 0;
    speed = 5;
    this.fov = fov;
  }

  public void updateCamera(float mouseX, float mouseY) {
      if (firstMouse) {
        lastX = mouseX;
        lastY = mouseY;
        firstMouse = false;
      }

      float xoffset = mouseX - lastX;
      float yoffset = lastY - mouseY;
      lastX = mouseX;
      lastY = mouseY;

      float sensitivity = 0.1f;
      xoffset *= sensitivity;
      yoffset *= sensitivity;

      yaw += xoffset;
      pitch += yoffset;

      if (pitch > 89.0f)
        pitch = 89.0f;
      if (pitch < -89.0f)
        pitch = -89.0f;

      if (yaw > 360)
        yaw = 0;
      if (yaw < -360)
        yaw = 0;

      Vector3f direction = new Vector3f();
      direction.setComponent(0, (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))));
      direction.setComponent(1, (float) (Math.sin(Math.toRadians(pitch))));
      direction.setComponent(2, (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))));
      front = new Vector3f(direction.normalize());
  }

  public void move(Direction direction, float deltaTime) {
    switch (direction) {
      case FORWARD:
        position.add(new Vector3f(front).mul(speed * deltaTime));
        break;
      case BACKWARD:
        position.sub(new Vector3f(front).mul(speed * deltaTime));
        break;
      case LEFT:
        position.sub(new Vector3f(front).cross(up).normalize().mul(speed * deltaTime));
        break;
      case RIGHT:
        position.add(new Vector3f(front).cross(up).normalize().mul(speed * deltaTime));
        break;
    }
  }

  public void setFirstMouse(boolean firstMouse) {
    this.firstMouse = firstMouse;
  }

  public Matrix4f getLook() {
    return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
  }

  public float getFov() {
    return fov;
  }
}
