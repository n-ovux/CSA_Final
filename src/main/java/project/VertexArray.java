package project;

import java.util.*;

import static org.lwjgl.opengl.GL43.*;

public class VertexArray {
  private int Id;
  private ArrayList<Integer> elements;
  private ArrayList<Integer> types;

  public VertexArray() {
    Id = glGenVertexArrays();
    elements = new ArrayList<Integer>();
    types = new ArrayList<Integer>();
  }

  public void push(int amount, int type) {
    elements.add(amount);
    types.add(type);
  }

  public void enable() {
    int total = 0;
    for (int i = 0; i < elements.size(); i++)
      total += elements.get(i) * typeToSize(types.get(i));

    int beforeTotal = 0;
    for (int i = 0; i < elements.size(); i++) {
      glEnableVertexAttribArray(i);
      glVertexAttribPointer(i, elements.get(i), types.get(i), false, total, beforeTotal);
      beforeTotal += elements.get(i)*typeToSize(types.get(i));
    }
  }

  private int typeToSize(int type) {
    switch (type) {
      case GL_FLOAT:
        return 4;
      case GL_INT:
        return 4;
      case GL_UNSIGNED_INT:
        return 4;
      case GL_BYTE:
        return 1;
      case GL_UNSIGNED_BYTE:
        return 1;
      default:
        Logger.error("failed to get size of type");
    }
    return -1; // this will never be reached but my ide is complaining
  }

  public void bind() {
    glBindVertexArray(Id);
  }

  public void unbind() {
    glBindVertexArray(0);
  }
}
