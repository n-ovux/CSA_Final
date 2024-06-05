package project;

import org.joml.*;

import java.lang.Math;

import static org.lwjgl.opengl.GL43.*;

public class Perlin {
  private int[] permutations;

  public Perlin(int permutations) {
    this.permutations = new int[permutations];
    for (int i = 0; i < permutations; i++)
      this.permutations[i] = i;

    for (int i = 0; i < this.permutations.length; i++) {
      int temp = this.permutations[i];
      int randomIndex = (int) (this.permutations.length * Math.random());
      this.permutations[i] = this.permutations[randomIndex];
      this.permutations[randomIndex] = temp;
    }

    int[] doubledPermutations = new int[2 * permutations];
    for (int i = 0; i < permutations; i++) {
      doubledPermutations[i] = this.permutations[i];
      doubledPermutations[permutations + i] = this.permutations[i];
    }

    this.permutations = doubledPermutations;
  }

  private Vector2f getConstantVector(int permutation) {
    int wrap = permutation % 4;
    if (wrap == 0)
      return new Vector2f(1.0f, 1.0f);
    else if (wrap == 1)
      return new Vector2f(-1.0f, 1.0f);
    else if (wrap == 2)
      return new Vector2f(-1.0f, -1.0f);
    else if (wrap == 3)
      return new Vector2f(1.0f, -1.0f);
    // this will never be reached but compiler needs it
    return null;
  }

  private float smoothStep(float t) {
    return ((6 * t - 15) * t + 10) * t * t * t;
  }

  private float lerp(float t, float value1, float value2) {
    return value1 + t * (value2 - value1);
  }

  private float sample(float x, float y) {
    int X = (int) (Math.floor(x) % (permutations.length / 2));
    int Y = (int) (Math.floor(y) % (permutations.length / 2));

    float xFractional = x - (float) Math.floor(x);
    float yFractional = y - (float) Math.floor(y);

    Vector2f topRight = new Vector2f(xFractional - 1.0f, yFractional - 1.0f);
    Vector2f topLeft = new Vector2f(xFractional, yFractional - 1.0f);
    Vector2f bottomRight = new Vector2f(xFractional - 1.0f, yFractional);
    Vector2f bottomLeft = new Vector2f(xFractional, yFractional);

    int valueTopRight = permutations[permutations[X + 1] + Y + 1];
    int valueTopLeft = permutations[permutations[X] + Y + 1];
    int valueBottomRight = permutations[permutations[X + 1] + Y];
    int valueBottomLeft = permutations[permutations[X] + Y];

    float dotTopRight = topRight.dot(getConstantVector(valueTopRight));
    float dotTopLeft = topLeft.dot(getConstantVector(valueTopLeft));
    float dotBottomRight = bottomRight.dot(getConstantVector(valueBottomRight));
    float dotBottomLeft = bottomLeft.dot(getConstantVector(valueBottomLeft));

    float u = smoothStep(xFractional);
    float v = smoothStep(yFractional);

    return lerp(u, lerp(v, dotBottomLeft, dotTopLeft), lerp(v, dotBottomRight, dotTopRight));
  }

  private float fractalBrownianMotion(float x, float y, float freq, int octaves) {
    float amplitude = 1.0f;
    float result = 0.0f;

    for (int i = 0; i < octaves; i++) {
      result += amplitude * sample(x * freq, y * freq);

      amplitude /= 2.f;
      freq *= 2.0f;
    }

    return result;
  }

  public Texture generateTexture(int size, float freq, int octaves) {
    int components = 4;
    float[] pixels = new float[size * size * components];
    for (int column = 0; column < size * components; column += components) {
      for (int row = 0; row < size * components; row += components) {
        float value = fractalBrownianMotion(row, column, freq, octaves);
        value = (value + 1) / 2.0f;
        pixels[column * size + row] = value;
        pixels[column * size + row + 1] = value;
        pixels[column * size + row + 2] = value;
        pixels[column * size + row + 3] = 1.0f;
      }
    }

    return new Texture(size, size, GL_RGBA32F, GL_RGBA, GL_FLOAT, pixels, GL_LINEAR);
  }
}
