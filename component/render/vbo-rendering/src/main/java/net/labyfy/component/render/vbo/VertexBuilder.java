package net.labyfy.component.render.vbo;

import net.labyfy.component.inject.assisted.AssistedFactory;

import java.nio.FloatBuffer;

public interface VertexBuilder {

  VertexBuilder position(float x, float y, float z);

  VertexBuilder position(float x, float y, float z, float w);

  VertexBuilder normal(float x, float y, float z);

  VertexBuilder color(float r, float g, float b);

  VertexBuilder color(float r, float g, float b, float alpha);

  VertexBuilder texture(float u, float v);

  VertexBuilder custom(float... values);

  VertexBuilder next();

  void write(FloatBuffer buffer);

  @AssistedFactory(VertexBuilder.class)
  interface Factory {

    VertexBuilder create(VertexBufferObject format);
  }
}
