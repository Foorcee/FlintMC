package net.labyfy.internal.component.render.v1_15_2;


import net.labyfy.component.render.AdvancedVertexBuffer;
import net.labyfy.component.render.VertexBuffer;
import net.labyfy.component.render.VertexFormat;
import net.minecraft.client.renderer.BufferBuilder;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class VertexBufferImpl implements AdvancedVertexBuffer, VertexBuffer {
  private final static MethodHandle byteBufferFieldGetter;
  private final static MethodHandle byteBufferFieldSetter;
  private final static MethodHandle vertexCountFieldSetter;

  static {
    MethodHandle byteBufferFieldGetterTmp = null;
    MethodHandle byteBufferFieldSetterTmp = null;
    MethodHandle vertexCountFieldSetterTmp = null;
    try {
      Field byteBuffer = BufferBuilder.class.getDeclaredField("byteBuffer");
      byteBuffer.setAccessible(true);
      byteBufferFieldGetterTmp = MethodHandles.lookup().unreflectGetter(byteBuffer);
      byteBufferFieldSetterTmp = MethodHandles.lookup().unreflectSetter(byteBuffer);

      Field vertexCount = BufferBuilder.class.getDeclaredField("vertexCount");
      vertexCount.setAccessible(true);
      vertexCountFieldSetterTmp = MethodHandles.lookup().unreflectSetter(vertexCount);

    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
    byteBufferFieldGetter = byteBufferFieldGetterTmp;
    byteBufferFieldSetter = byteBufferFieldSetterTmp;
    vertexCountFieldSetter = vertexCountFieldSetterTmp;
  }

  private final BufferBuilder buffer;
  private final VertexFormat vertexFormat;
  private Matrix4f worldContext;
  private Matrix3f normalContext;
  private ByteBuffer byteBuffer;
  private int vertexCount;
  private int writtenBytes;

  public VertexBufferImpl(BufferBuilder buffer, VertexFormat vertexFormat) throws Throwable {
    this.byteBuffer = (ByteBuffer) byteBufferFieldGetter.invoke(buffer);
    this.buffer = buffer;
    this.vertexFormat = vertexFormat;
    this.worldContext = new Matrix4f();
    this.normalContext = new Matrix3f();
  }

  public VertexBufferImpl pos(float x, float y, float z) {
    Vector3f vector3f = new Vector3f(x, y, z);
    if (this.worldContext != null) {
      vector3f.mulPosition(this.worldContext);
    }
    return this.pushFloats("position", vector3f.x, vector3f.y, vector3f.z);
  }

  public VertexBuffer color(int r, int g, int b, int alpha) {
    this.pushBytes("color", ((byte) r), ((byte) g), ((byte) b), ((byte) alpha));
    return this;
  }

  public VertexBuffer pos(Vector3f position) {
    return this.pos(position.x, position.y, position.z);
  }

  public VertexBufferImpl normal(float x, float y, float z) {
    Vector3f vector3f = new Vector3f(x, y, z);
    if (this.worldContext != null) {
      vector3f.mulPosition(this.worldContext);
    }
    return this.pushFloats("normal", vector3f.x, vector3f.y, vector3f.z);
  }

  public VertexBuffer normal(Vector3f normal) {
    return this.normal(normal.x, normal.y, normal.z);
  }

  public VertexBufferImpl end() {
    this.vertexCount++;
    if (this.writtenBytes != this.vertexCount * this.vertexFormat.getBytes()) {
      throw new IllegalStateException("Not all or too many vertex elements have been written.");
    }
    try {
      vertexCountFieldSetter.invoke(this.buffer, this.vertexCount);
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    return this;
  }

  public VertexBufferImpl lightmap(short sky, short ground) {
    this.pushShorts("lightmap", sky, ground);
    return this;
  }

  public VertexBufferImpl texture(float x, float y) {
    this.pushFloats("texture", x, y);
    return this;
  }

  public VertexBufferImpl pushFloats(String name, float... floats) {
    this.growBufferEventually(((this.vertexCount + 1) * this.vertexFormat.getBytes()));
    vertexFormat.pushFloats(this.byteBuffer, this, name, floats);
    this.writtenBytes += floats.length * Float.BYTES;
    return this;
  }

  public VertexBufferImpl pushBytes(String name, byte... bytes) {
    this.growBufferEventually(((this.vertexCount + 1) * this.vertexFormat.getBytes()));
    vertexFormat.pushBytes(this.byteBuffer, this, name, bytes);
    this.writtenBytes += bytes.length;
    return this;
  }

  public AdvancedVertexBuffer pushShorts(String name, short... shorts) {
    this.growBufferEventually(((this.vertexCount + 1) * this.vertexFormat.getBytes()));
    vertexFormat.pushShorts(this.byteBuffer, this, name, shorts);
    this.writtenBytes += shorts.length * Short.BYTES;
    return this;
  }

  public AdvancedVertexBuffer incrementVertexCount(int count) {
    this.vertexCount += count;
    try {
      vertexCountFieldSetter.invoke(this.buffer, this.vertexCount);
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    return this;
  }

  public VertexBufferImpl growBufferEventually(int targetSize) {
    if (this.byteBuffer.limit() < targetSize) {
      ByteBuffer oldBuffer = this.byteBuffer;
      this.byteBuffer = ByteBuffer.allocateDirect(targetSize);
      this.byteBuffer.put(oldBuffer);
      try {
        byteBufferFieldSetter.invoke(this.buffer, byteBuffer);
      } catch (Throwable throwable) {
        throwable.printStackTrace();
      }
    }
    return this;
  }

  public VertexFormat getFormat() {
    return this.vertexFormat;
  }

  public VertexBufferImpl box(float x, float y, float z, float width, float height, float depth) {
    this
        .quad(
            x, y, z,
            x + width, y, z,
            x + width, y + height, z,
            x, y + height, z
        )
        .quad(
            x, y, z + depth,
            x + width, y, z + depth,
            x + width, y + height, z + depth,
            x, y + height, z + depth
        )
        .quad(
            x, y, z,
            x, y, z + depth,
            x + width, y, z + depth,
            x + width, y, z
        )
        .quad(
            x, y + height, z,
            x, y + height, z + depth,
            x + width, y + height, z + depth,
            x + width, y + height, z
        )
        .quad(
            x, y, z,
            x, y, z + depth,
            x, y + height, z + depth,
            x, y + height, z
        )
        .quad(
            x + width, y, z,
            x + width, y, z + depth,
            x + width, y + height, z + depth,
            x + width, y + height, z
        );

    return this;
  }

  public VertexBuffer box(float x, float y, float z, float width, float height, float depth, short lightmapGround, short lightmapSky) {
    return this
        .quad(
            x, y, z,
            x + width, y, z,
            x + width, y + height, z,
            x, y + height, z)
        .quad(
            x, y, z + depth,
            x + width, y, z + depth,
            x + width, y + height, z + depth,
            x, y + height, z + depth,
            lightmapGround, lightmapSky
        )
        .quad(
            x, y, z,
            x, y, z + depth,
            x + width, y, z + depth,
            x + width, y, z,
            lightmapGround, lightmapSky
        )
        .quad(
            x, y + height, z,
            x, y + height, z + depth,
            x + width, y + height, z + depth,
            x + width, y + height, z,
            lightmapGround, lightmapSky
        )
        .quad(
            x, y, z,
            x, y, z + depth,
            x, y + height, z + depth,
            x, y + height, z,
            lightmapGround, lightmapSky
        )
        .quad(
            x + width, y, z,
            x + width, y, z + depth,
            x + width, y + height, z + depth,
            x + width, y + height, z,
            lightmapGround, lightmapSky
        );
  }

  public VertexBufferImpl box(float x, float y, float z, float width, float height, float depth, int r, int g, int b, int alpha) {
    this
        .quad(
            x, y, z,
            x + width, y, z,
            x + width, y + height, z,
            x, y + height, z,
            r, g, b, alpha
        )
        .quad(
            x, y, z + depth,
            x + width, y, z + depth,
            x + width, y + height, z + depth,
            x, y + height, z + depth,
            r, g, b, alpha
        )
        .quad(
            x, y, z,
            x, y, z + depth,
            x + width, y, z + depth,
            x + width, y, z,
            r, g, b, alpha
        )
        .quad(
            x, y + height, z,
            x, y + height, z + depth,
            x + width, y + height, z + depth,
            x + width, y + height, z,
            r, g, b, alpha
        )
        .quad(
            x, y, z,
            x, y, z + depth,
            x, y + height, z + depth,
            x, y + height, z,
            r, g, b, alpha
        )
        .quad(
            x + width, y, z,
            x + width, y, z + depth,
            x + width, y + height, z + depth,
            x + width, y + height, z,
            r, g, b, alpha
        );

    return this;
  }

  public VertexBuffer box(float x, float y, float z, float width, float height, float depth, int r, int g, int b, int alpha, short lightmapGround, short lightmapSky) {
    return this
        .quad(
            x, y, z,
            x + width, y, z,
            x + width, y + height, z,
            x, y + height, z,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        )
        .quad(
            x, y, z + depth,
            x + width, y, z + depth,
            x + width, y + height, z + depth,
            x, y + height, z + depth,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        )
        .quad(
            x, y, z,
            x, y, z + depth,
            x + width, y, z + depth,
            x + width, y, z,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        )
        .quad(
            x, y + height, z,
            x, y + height, z + depth,
            x + width, y + height, z + depth,
            x + width, y + height, z,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        )
        .quad(
            x, y, z,
            x, y, z + depth,
            x, y + height, z + depth,
            x, y + height, z,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        )
        .quad(
            x + width, y, z,
            x + width, y, z + depth,
            x + width, y + height, z + depth,
            x + width, y + height, z,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        );
  }


  public VertexBuffer box(float x, float y, float z, float width, float height, float depth, float textureDensityX, float textureDensityY) {
    return this.box(x, y, z, width, height, depth, textureDensityX, textureDensityY, 0, 0);
  }

  public VertexBuffer box(float x, float y, float z, float width, float height, float depth, float textureDensityX, float textureDensityY, short lightmapGround, short lightmapSky) {
    return this.box(x, y, z, width, height, depth, textureDensityX, textureDensityY, 0f, 0f, lightmapGround, lightmapSky);
  }

  public VertexBuffer box(float x, float y, float z, float width, float height, float depth, float textureDensityX, float textureDensityY, float textureOffsetX, float textureOffsetY) {
    this
        .quad(
            x, y + height, z, textureOffsetX + depth / textureDensityX, textureOffsetY,
            x, y + height, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y + height, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y + height, z, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY
        )
        .quad(
            x, y, z, textureOffsetX + (width + depth) / textureDensityX, textureOffsetY,
            x, y, z + depth, textureOffsetX + (width + depth) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y, z + depth, textureOffsetX + (depth + width + width) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y, z, textureOffsetX + (depth + width + width) / textureDensityX, textureOffsetY
        )
        .quad(
            x, y + height, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + depth / textureDensityY,
            x, y, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y + height, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + depth / textureDensityY
        )
        .quad(
            x + width, y, z, textureOffsetX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y + height, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y + height, z, textureOffsetX, textureOffsetY + depth / textureDensityY
        )
        .quad(
            x, y, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x, y, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x, y + height, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x, y + height, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + depth / textureDensityY
        )
        .quad(
            x, y, z, textureOffsetX + (depth + width + depth + width) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y + height, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + (depth) / textureDensityY,
            x, y + height, z, textureOffsetX + (depth + width + depth + width) / textureDensityX, textureOffsetY + (depth) / textureDensityY
        );
    return this;
  }

  public VertexBuffer box(float x, float y, float z, float width, float height, float depth, float textureDensityX, float textureDensityY, float textureOffsetX, float textureOffsetY, short lightmapGround, short lightmapSky) {
    this
        .quad(
            x, y + height, z, textureOffsetX + depth / textureDensityX, textureOffsetY,
            x, y + height, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y + height, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y + height, z, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY
        )
        .quad(
            x, y, z, textureOffsetX + (width + depth) / textureDensityX, textureOffsetY,
            x, y, z + depth, textureOffsetX + (width + depth) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y, z + depth, textureOffsetX + (depth + width + width) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y, z, textureOffsetX + (depth + width + width) / textureDensityX, textureOffsetY,
            lightmapGround, lightmapSky
        )
        .quad(
            x, y + height, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + depth / textureDensityY,
            x, y, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y + height, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + depth / textureDensityY
        )
        .quad(
            x + width, y, z, textureOffsetX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y + height, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y + height, z, textureOffsetX, textureOffsetY + depth / textureDensityY
        )
        .quad(
            x, y, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x, y, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x, y + height, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x, y + height, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + depth / textureDensityY
        )
        .quad(
            x, y, z, textureOffsetX + (depth + width + depth + width) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y + height, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + (depth) / textureDensityY,
            x, y + height, z, textureOffsetX + (depth + width + depth + width) / textureDensityX, textureOffsetY + (depth) / textureDensityY
        );
    return this;
  }

  public VertexBuffer box(float x, float y, float z, float width, float height, float depth, float textureDensityX, float textureDensityY, int r, int g, int b, int alpha) {
    return this.box(x, y, z, width, height, depth, textureDensityX, textureDensityY, 0, 0, r, g, b, alpha);
  }

  public VertexBuffer box(float x, float y, float z, float width, float height, float depth, float textureDensityX, float textureDensityY, int r, int g, int b, int alpha, short lightmapGround, short lightmapSky) {
    return this.box(x, y, z, width, height, depth, textureDensityX, textureDensityY, 0, 0, r, g, b, alpha, lightmapGround, lightmapSky);
  }

  public VertexBuffer box(float x, float y, float z, float width, float height, float depth, float textureDensityX, float textureDensityY, float textureOffsetX, float textureOffsetY, int r, int g, int b, int alpha) {
    this
        .quad(
            x, y + height, z, textureOffsetX + depth / textureDensityX, textureOffsetY,
            x, y + height, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y + height, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y + height, z, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY,
            r, g, b, alpha
        )
        .quad(
            x, y, z, textureOffsetX + (width + depth) / textureDensityX, textureOffsetY,
            x, y, z + depth, textureOffsetX + (width + depth) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y, z + depth, textureOffsetX + (depth + width + width) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y, z, textureOffsetX + (depth + width + width) / textureDensityX, textureOffsetY,
            r, g, b, alpha
        )
        .quad(
            x, y + height, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + depth / textureDensityY,
            x, y, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y + height, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + depth / textureDensityY,
            r, g, b, alpha
        )
        .quad(
            x + width, y, z, textureOffsetX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y + height, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y + height, z, textureOffsetX, textureOffsetY + depth / textureDensityY,
            r, g, b, alpha
        )
        .quad(
            x, y, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x, y, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x, y + height, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x, y + height, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + depth / textureDensityY,
            r, g, b, alpha
        )
        .quad(
            x, y, z, textureOffsetX + (depth + width + depth + width) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y + height, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + (depth) / textureDensityY,
            x, y + height, z, textureOffsetX + (depth + width + depth + width) / textureDensityX, textureOffsetY + (depth) / textureDensityY,
            r, g, b, alpha
        );
    return this;
  }

  public VertexBuffer box(float x, float y, float z, float width, float height, float depth, float textureDensityX, float textureDensityY, float textureOffsetX, float textureOffsetY, int r, int g, int b, int alpha, short lightmapGround, short lightmapSky) {
    return  this
        .quad(
            x, y + height, z, textureOffsetX + depth / textureDensityX, textureOffsetY,
            x, y + height, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y + height, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y + height, z, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        )
        .quad(
            x, y, z, textureOffsetX + (width + depth) / textureDensityX, textureOffsetY,
            x, y, z + depth, textureOffsetX + (width + depth) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y, z + depth, textureOffsetX + (depth + width + width) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y, z, textureOffsetX + (depth + width + width) / textureDensityX, textureOffsetY,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        )
        .quad(
            x, y + height, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + depth / textureDensityY,
            x, y, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y + height, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + depth / textureDensityY,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        )
        .quad(
            x + width, y, z, textureOffsetX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y + height, z + depth, textureOffsetX + depth / textureDensityX, textureOffsetY + depth / textureDensityY,
            x + width, y + height, z, textureOffsetX, textureOffsetY + depth / textureDensityY,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        )
        .quad(
            x, y, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x, y, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x, y + height, z + depth, textureOffsetX + (depth + width) / textureDensityX, textureOffsetY + depth / textureDensityY,
            x, y + height, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + depth / textureDensityY,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        )
        .quad(
            x, y, z, textureOffsetX + (depth + width + depth + width) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + (depth + height) / textureDensityY,
            x + width, y + height, z, textureOffsetX + (depth + width + depth) / textureDensityX, textureOffsetY + (depth) / textureDensityY,
            x, y + height, z, textureOffsetX + (depth + width + depth + width) / textureDensityX, textureOffsetY + (depth) / textureDensityY,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        );
  }

  public VertexBuffer quad(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
    this
        .triangle(
            x1, y1, z1,
            x2, y2, z2,
            x3, y3, z3
        )
        .triangle(
            x3, y3, z3,
            x1, y1, z1,
            x4, y4, z4
        );

    return this;
  }

  public VertexBuffer quad(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, short lightmapGround, short lightmapSky) {
    return this
        .triangle(
            x1, y1, z1,
            x2, y2, z2,
            x3, y3, z3,
            lightmapGround, lightmapSky
        )
        .triangle(
            x3, y3, z3,
            x1, y1, z1,
            x4, y4, z4,
            lightmapGround, lightmapSky
        );
  }

  public VertexBufferImpl quad(
      float x1, float y1, float z1,
      float x2, float y2, float z2,
      float x3, float y3, float z3,
      float x4, float y4, float z4,
      int r, int g, int b, int alpha
  ) {
    this
        .triangle(
            x1, y1, z1,
            x2, y2, z2,
            x3, y3, z3,
            r, g, b, alpha
        )
        .triangle(
            x3, y3, z3,
            x1, y1, z1,
            x4, y4, z4,
            r, g, b, alpha
        );

    return this;
  }

  public VertexBuffer quad(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, int r, int g, int b, int alpha, short lightmapGround, short lightmapSky) {
    return this
        .triangle(
            x1, y1, z1,
            x2, y2, z2,
            x3, y3, z3,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        )
        .triangle(
            x3, y3, z3,
            x1, y1, z1,
            x4, y4, z4,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        );
  }

  public VertexBufferImpl quad(
      float x1, float y1, float z1, float texU1, float texV1,
      float x2, float y2, float z2, float texU2, float texV2,
      float x3, float y3, float z3, float texU3, float texV3,
      float x4, float y4, float z4, float texU4, float texV4
  ) {
    this
        .triangle(
            x1, y1, z1, texU1, texV1,
            x2, y2, z2, texU2, texV2,
            x3, y3, z3, texU3, texV3
        )
        .triangle(
            x3, y3, z3, texU3, texV3,
            x1, y1, z1, texU1, texV1,
            x4, y4, z4, texU4, texV4
        );

    return this;
  }

  public VertexBufferImpl quad(
      float x1, float y1, float z1, float texU1, float texV1,
      float x2, float y2, float z2, float texU2, float texV2,
      float x3, float y3, float z3, float texU3, float texV3,
      float x4, float y4, float z4, float texU4, float texV4,
      short lightmapGround, short lightmapSky
  ) {
    this
        .triangle(
            x1, y1, z1, texU1, texV1,
            x2, y2, z2, texU2, texV2,
            x3, y3, z3, texU3, texV3,
            lightmapGround, lightmapSky
        )
        .triangle(
            x3, y3, z3, texU3, texV3,
            x1, y1, z1, texU1, texV1,
            x4, y4, z4, texU4, texV4,
            lightmapGround, lightmapSky
        );

    return this;
  }

  public VertexBuffer quad(
      float x1, float y1, float z1, float texU1, float texV1,
      float x2, float y2, float z2, float texU2, float texV2,
      float x3, float y3, float z3, float texU3, float texV3,
      float x4, float y4, float z4, float texU4, float texV4,
      int r, int g, int b, int alpha
  ) {
    this
        .triangle(
            x1, y1, z1, texU1, texV1,
            x2, y2, z2, texU2, texV2,
            x3, y3, z3, texU3, texV3,
            r, g, b, alpha
        )
        .triangle(
            x3, y3, z3, texU3, texV3,
            x1, y1, z1, texU1, texV1,
            x4, y4, z4, texU4, texV4,
            r, g, b, alpha
        );

    return this;
  }

  public VertexBuffer quad(float x1, float y1, float z1, float texU1, float texV1, float x2, float y2, float z2, float texU2, float texV2, float x3, float y3, float z3, float texU3, float texV3, float x4, float y4, float z4, float texU4, float texV4, int r, int g, int b, int alpha, short lightmapGround, short lightmapSky) {
    this
        .triangle(
            x1, y1, z1, texU1, texV1,
            x2, y2, z2, texU2, texV2,
            x3, y3, z3, texU3, texV3,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        )
        .triangle(
            x3, y3, z3, texU3, texV3,
            x1, y1, z1, texU1, texV1,
            x4, y4, z4, texU4, texV4,
            r, g, b, alpha,
            lightmapGround, lightmapSky
        );

    return this;

  }

  public VertexBufferImpl triangle(
      float x1, float y1, float z1,
      float x2, float y2, float z2,
      float x3, float y3, float z3
  ) {
    this
        .pos(x1, y1, z1)
        .end()
        .pos(x2, y2, z2)
        .end()
        .pos(x3, y3, z3)
        .end();

    return this;
  }

  public VertexBuffer triangle(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, short lightmapGround, short lightmapSky) {
    return
        this
            .pos(x1, y1, z1)
            .lightmap(lightmapSky, lightmapGround)
            .end()
            .pos(x2, y2, z2)
            .lightmap(lightmapSky, lightmapGround)
            .end()
            .pos(x3, y3, z3)
            .lightmap(lightmapSky, lightmapGround)
            .end();
  }

  public VertexBuffer triangle(
      float x1, float y1, float z1, float texU1, float texV1,
      float x2, float y2, float z2, float texU2, float texV2,
      float x3, float y3, float z3, float texU3, float texV3
  ) {
    return this
        .pos(x1, y1, z1)
        .texture(texU1, texV1)
        .end()
        .pos(x2, y2, z2)
        .texture(texU2, texV2)
        .end()
        .pos(x3, y3, z3)
        .texture(texU3, texV3)
        .end();
  }

  public VertexBuffer triangle(float x1, float y1, float z1, float texU1, float texV1, float x2, float y2, float z2, float texU2, float texV2, float x3, float y3, float z3, float texU3, float texV3, short lightmapGround, short lightmapSky) {
    return this
        .pos(x1, y1, z1)
        .texture(texU1, texV1)
        .lightmap(lightmapSky, lightmapGround)
        .end()
        .pos(x2, y2, z2)
        .texture(texU2, texV2)
        .lightmap(lightmapSky, lightmapGround)
        .end()
        .pos(x3, y3, z3)
        .texture(texU3, texV3)
        .lightmap(lightmapSky, lightmapGround)
        .end();
  }

  public VertexBuffer triangle(
      float x1, float y1, float z1, float texU1, float texV1,
      float x2, float y2, float z2, float texU2, float texV2,
      float x3, float y3, float z3, float texU3, float texV3,
      int r, int g, int b, int alpha
  ) {
    return this
        .pos(x1, y1, z1)
        .texture(texU1, texV1)
        .color(r, g, b, alpha)
        .end()
        .pos(x2, y2, z2)
        .texture(texU2, texV2)
        .color(r, g, b, alpha)
        .end()
        .pos(x3, y3, z3)
        .texture(texU3, texV3)
        .color(r, g, b, alpha)
        .end();
  }

  public VertexBuffer triangle(float x1, float y1, float z1, float texU1, float texV1, float x2, float y2, float z2, float texU2, float texV2, float x3, float y3, float z3, float texU3, float texV3, int r, int g, int b, int alpha, short lightmapGround, short lightmapSky) {
    return this
        .pos(x1, y1, z1)
        .texture(texU1, texV1)
        .lightmap(lightmapSky, lightmapGround)
        .color(r, g, b, alpha)
        .end()
        .pos(x2, y2, z2)
        .texture(texU2, texV2)
        .lightmap(lightmapSky, lightmapGround)
        .color(r, g, b, alpha)
        .end()
        .pos(x3, y3, z3)
        .texture(texU3, texV3)
        .lightmap(lightmapSky, lightmapGround)
        .color(r, g, b, alpha)
        .end();
  }

  public VertexBufferImpl triangle(
      float x1, float y1, float z1,
      float x2, float y2, float z2,
      float x3, float y3, float z3,
      int r, int g, int b, int alpha
  ) {
    this
        .pos(x1, y1, z1)
        .color(r, g, b, alpha)
        .end()
        .pos(x2, y2, z2)
        .color(r, g, b, alpha)
        .end()
        .pos(x3, y3, z3)
        .color(r, g, b, alpha)
        .end();

    return this;
  }

  public Matrix4f getWorldContext() {
    return new Matrix4f(this.worldContext);
  }

  public VertexBuffer setWorldContext(Matrix4f matrix) {
    if (matrix == null) matrix = new Matrix4f();
    this.worldContext = new Matrix4f(matrix);
    return this;
  }

  public VertexBuffer setNormalContext(Matrix3f normalContext) {
    if (normalContext == null) normalContext = new Matrix3f();
    this.normalContext = new Matrix3f(normalContext);
    return this;
  }

  public Matrix3f getNormalContext() {
    return new Matrix3f(normalContext);
  }

  public AdvancedVertexBuffer advanced() {
    return this;
  }

  public VertexBuffer simple() {
    return this;
  }

  public ByteBuffer getByteBuffer() {
    return this.byteBuffer;
  }

  public AdvancedVertexBuffer setByteBuffer(ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
    return this;
  }

  public int getVertexCount() {
    return this.vertexCount;
  }

}
