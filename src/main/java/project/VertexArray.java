package project;

import java.util.*;

import static org.lwjgl.opengl.GL43.*;

public class VertexArray {
  private int Id;
  private ArrayList<Integer> elements;

  public VertexArray() {
    Id = glGenVertexArrays();
    elements = new ArrayList<Integer>();
  }

  public void push(int amount) {
    elements.add(amount);
  }

  public void enable() {
    int total = 0;
    for (int num : elements)
      total += num;
    
    int beforeTotal = 0;
    for (int i = 0; i < elements.size(); i++) {
      glEnableVertexAttribArray(i);
      glVertexAttribPointer(i, elements.get(i), GL_FLOAT, false, total * 4, (beforeTotal) * 4);
      beforeTotal += elements.get(i);
    }
  }

  public void bind() {
    glBindVertexArray(Id);
  }

  public void unbind() {
    glBindVertexArray(0);
  }
}
