package org.admany.taczt.platform.fabric.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.model.bedrock.BedrockPolygon;
import com.tacz.guns.client.model.bedrock.BedrockVertex;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class BedrockCubeCompiler {
   private static volatile Mode mode = defaultMode();
   private static final boolean PROFILE_CUBES = Boolean.getBoolean("taczt.profile.render") || Boolean.getBoolean("taczt.profile.bullets");
   private static final int MAX_COMPILED_GEOMETRIES = 4096;
   private static final Map<BedrockPolygon[], CompiledGeometry> GEOMETRY_CACHE = new ConcurrentHashMap();
   private static final ThreadLocal<GeometryLookup> LAST_GEOMETRY = ThreadLocal.withInitial(GeometryLookup::new);

   private BedrockCubeCompiler() {
   }

   public static Mode mode() {
      return mode;
   }

   public static void use(Mode requestedMode) {
      mode = requestedMode;
   }

   public static void clear() {
      GEOMETRY_CACHE.clear();
      LAST_GEOMETRY.remove();
   }

   public static void compile(BedrockPolygon[] polygons, PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
      compile(geometry(polygons), pose, consumer, light, overlay, red, green, blue, alpha);
   }

   public static CompiledGeometry prepare(BedrockPolygon[] polygons) {
      return geometry(polygons);
   }

   public static void compile(CompiledGeometry geometry, PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
      Mode activeMode = mode;
      if (!PROFILE_CUBES) {
         if (activeMode == BedrockCubeCompiler.Mode.SCALAR) {
            compileScalar(geometry, pose, consumer, light, overlay, red, green, blue, alpha);
         } else {
            compileVanilla(geometry, pose, consumer, light, overlay, red, green, blue, alpha);
         }

      } else {
         long started = GunRenderProfiler.begin();
         if (activeMode == BedrockCubeCompiler.Mode.SCALAR) {
            compileScalar(geometry, pose, consumer, light, overlay, red, green, blue, alpha);
            GunRenderProfiler.endCubeScalar(started);
         } else {
            compileVanilla(geometry, pose, consumer, light, overlay, red, green, blue, alpha);
            GunRenderProfiler.endCubeVanilla(started);
         }

      }
   }

   private static void compileVanilla(CompiledGeometry geometry, PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
      Matrix4f transform = pose.pose();
      Matrix3f normalTransform = pose.normal();
      float n00 = normalTransform.m00();
      float n01 = normalTransform.m01();
      float n02 = normalTransform.m02();
      float n10 = normalTransform.m10();
      float n11 = normalTransform.m11();
      float n12 = normalTransform.m12();
      float n20 = normalTransform.m20();
      float n21 = normalTransform.m21();
      float n22 = normalTransform.m22();
      float m00 = transform.m00();
      float m01 = transform.m01();
      float m02 = transform.m02();
      float m10 = transform.m10();
      float m11 = transform.m11();
      float m12 = transform.m12();
      float m20 = transform.m20();
      float m21 = transform.m21();
      float m22 = transform.m22();
      float m30 = transform.m30();
      float m31 = transform.m31();
      float m32 = transform.m32();
      int vertexOffset = 0;

      for(int polygon = 0; polygon < geometry.polygonCount; ++polygon) {
         float sourceNormalX = geometry.normals[polygon * 3];
         float sourceNormalY = geometry.normals[polygon * 3 + 1];
         float sourceNormalZ = geometry.normals[polygon * 3 + 2];
         float normalX = sourceNormalX * n00 + sourceNormalY * n10 + sourceNormalZ * n20;
         float normalY = sourceNormalX * n01 + sourceNormalY * n11 + sourceNormalZ * n21;
         float normalZ = sourceNormalX * n02 + sourceNormalY * n12 + sourceNormalZ * n22;
         int vertexCount = geometry.polygonVertexCounts[polygon];

         for(int vertex = 0; vertex < vertexCount; ++vertex) {
            int index = (vertexOffset + vertex) * 3;
            float x = geometry.positions[index];
            float y = geometry.positions[index + 1];
            float z = geometry.positions[index + 2];
            float positionX = x * m00 + y * m10 + z * m20 + m30;
            float positionY = x * m01 + y * m11 + z * m21 + m31;
            float positionZ = x * m02 + y * m12 + z * m22 + m32;
            int uvIndex = (vertexOffset + vertex) * 2;
            consumer.addVertex(positionX, positionY, positionZ, net.minecraft.util.FastColor.ARGB32.colorFromFloat(alpha, red, green, blue), geometry.uvs[uvIndex], geometry.uvs[uvIndex + 1], overlay, light, normalX, normalY, normalZ);
         }

         vertexOffset += vertexCount;
      }

   }

   private static void compileScalar(CompiledGeometry geometry, PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
      Matrix4f transform = pose.pose();
      Matrix3f normalTransform = pose.normal();
      float n00 = normalTransform.m00();
      float n01 = normalTransform.m01();
      float n02 = normalTransform.m02();
      float n10 = normalTransform.m10();
      float n11 = normalTransform.m11();
      float n12 = normalTransform.m12();
      float n20 = normalTransform.m20();
      float n21 = normalTransform.m21();
      float n22 = normalTransform.m22();
      float m00 = transform.m00();
      float m01 = transform.m01();
      float m02 = transform.m02();
      float m10 = transform.m10();
      float m11 = transform.m11();
      float m12 = transform.m12();
      float m20 = transform.m20();
      float m21 = transform.m21();
      float m22 = transform.m22();
      float m30 = transform.m30();
      float m31 = transform.m31();
      float m32 = transform.m32();
      if (n00 == 1.0F && n01 == 0.0F && n02 == 0.0F && n10 == 0.0F && n11 == 1.0F && n12 == 0.0F && n20 == 0.0F && n21 == 0.0F && n22 == 1.0F && m00 == 1.0F && m01 == 0.0F && m02 == 0.0F && m10 == 0.0F && m11 == 1.0F && m12 == 0.0F && m20 == 0.0F && m21 == 0.0F && m22 == 1.0F) {
         if (m30 == 0.0F && m31 == 0.0F && m32 == 0.0F) {
            compileRaw(geometry, consumer, light, overlay, red, green, blue, alpha);
         } else {
            compileTranslated(geometry, consumer, light, overlay, red, green, blue, alpha, m30, m31, m32);
         }

      } else {
         int vertexOffset = 0;

         for(int polygon = 0; polygon < geometry.polygonCount; ++polygon) {
            float sourceNormalX = geometry.normals[polygon * 3];
            float sourceNormalY = geometry.normals[polygon * 3 + 1];
            float sourceNormalZ = geometry.normals[polygon * 3 + 2];
            float normalX = Math.fma(n00, sourceNormalX, Math.fma(n10, sourceNormalY, n20 * sourceNormalZ));
            float normalY = Math.fma(n01, sourceNormalX, Math.fma(n11, sourceNormalY, n21 * sourceNormalZ));
            float normalZ = Math.fma(n02, sourceNormalX, Math.fma(n12, sourceNormalY, n22 * sourceNormalZ));
            int vertexCount = geometry.polygonVertexCounts[polygon];

            for(int vertex = 0; vertex < vertexCount; ++vertex) {
               int index = (vertexOffset + vertex) * 3;
               float x = geometry.positions[index];
               float y = geometry.positions[index + 1];
               float z = geometry.positions[index + 2];
               float transformedX = Math.fma(m00, x, Math.fma(m10, y, Math.fma(m20, z, m30)));
               float transformedY = Math.fma(m01, x, Math.fma(m11, y, Math.fma(m21, z, m31)));
               float transformedZ = Math.fma(m02, x, Math.fma(m12, y, Math.fma(m22, z, m32)));
               int uvIndex = (vertexOffset + vertex) * 2;
               consumer.addVertex(transformedX, transformedY, transformedZ, net.minecraft.util.FastColor.ARGB32.colorFromFloat(alpha, red, green, blue), geometry.uvs[uvIndex], geometry.uvs[uvIndex + 1], overlay, light, normalX, normalY, normalZ);
            }

            vertexOffset += vertexCount;
         }

      }
   }

   private static void compileTranslated(CompiledGeometry geometry, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha, float xOffset, float yOffset, float zOffset) {
      int vertexOffset = 0;

      for(int polygon = 0; polygon < geometry.polygonCount; ++polygon) {
         float normalX = geometry.normals[polygon * 3];
         float normalY = geometry.normals[polygon * 3 + 1];
         float normalZ = geometry.normals[polygon * 3 + 2];
         int vertexCount = geometry.polygonVertexCounts[polygon];

         for(int vertex = 0; vertex < vertexCount; ++vertex) {
            int index = (vertexOffset + vertex) * 3;
            int uvIndex = (vertexOffset + vertex) * 2;
            consumer.addVertex(geometry.positions[index] + xOffset, geometry.positions[index + 1] + yOffset, geometry.positions[index + 2] + zOffset, net.minecraft.util.FastColor.ARGB32.colorFromFloat(alpha, red, green, blue), geometry.uvs[uvIndex], geometry.uvs[uvIndex + 1], overlay, light, normalX, normalY, normalZ);
         }

         vertexOffset += vertexCount;
      }

   }

   private static void compileRaw(CompiledGeometry geometry, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
      int vertexOffset = 0;

      for(int polygon = 0; polygon < geometry.polygonCount; ++polygon) {
         int normalIndex = polygon * 3;
         float normalX = geometry.normals[normalIndex];
         float normalY = geometry.normals[normalIndex + 1];
         float normalZ = geometry.normals[normalIndex + 2];
         int vertexCount = geometry.polygonVertexCounts[polygon];

         for(int vertex = 0; vertex < vertexCount; ++vertex) {
            int positionIndex = (vertexOffset + vertex) * 3;
            int uvIndex = (vertexOffset + vertex) * 2;
            consumer.addVertex(geometry.positions[positionIndex], geometry.positions[positionIndex + 1], geometry.positions[positionIndex + 2], net.minecraft.util.FastColor.ARGB32.colorFromFloat(alpha, red, green, blue), geometry.uvs[uvIndex], geometry.uvs[uvIndex + 1], overlay, light, normalX, normalY, normalZ);
         }

         vertexOffset += vertexCount;
      }

   }

   private static Mode defaultMode() {
      return BedrockCubeCompiler.Mode.SCALAR;
   }

   private static CompiledGeometry geometry(BedrockPolygon[] polygons) {
      GeometryLookup last = (GeometryLookup)LAST_GEOMETRY.get();
      if (last.polygons == polygons) {
         return last.geometry;
      } else {
         CompiledGeometry cached = (CompiledGeometry)GEOMETRY_CACHE.get(polygons);
         if (cached != null) {
            last.polygons = polygons;
            last.geometry = cached;
            return cached;
         } else {
            CompiledGeometry compiled = BedrockCubeCompiler.CompiledGeometry.create(polygons);
            if (GEOMETRY_CACHE.size() >= 4096) {
               GEOMETRY_CACHE.clear();
            }

            CompiledGeometry raced = (CompiledGeometry)GEOMETRY_CACHE.putIfAbsent(polygons, compiled);
            CompiledGeometry result = raced == null ? compiled : raced;
            last.polygons = polygons;
            last.geometry = result;
            return result;
         }
      }
   }

   private static final class GeometryLookup {
      private BedrockPolygon[] polygons;
      private CompiledGeometry geometry;
   }

   public static final class CompiledGeometry {
      private final int polygonCount;
      private final int[] polygonVertexCounts;
      private final float[] positions;
      private final float[] uvs;
      private final float[] normals;

      private CompiledGeometry(int polygonCount, int[] polygonVertexCounts, float[] positions, float[] uvs, float[] normals) {
         this.polygonCount = polygonCount;
         this.polygonVertexCounts = polygonVertexCounts;
         this.positions = positions;
         this.uvs = uvs;
         this.normals = normals;
      }

      private static CompiledGeometry create(BedrockPolygon[] polygons) {
         int vertexTotal = 0;

         for(BedrockPolygon polygon : polygons) {
            vertexTotal += polygon.vertices.length;
         }

         int[] vertexCounts = new int[polygons.length];
         float[] positions = new float[vertexTotal * 3];
         float[] uvs = new float[vertexTotal * 2];
         float[] normals = new float[polygons.length * 3];
         int vertexOffset = 0;

         for(int polygonIndex = 0; polygonIndex < polygons.length; ++polygonIndex) {
            BedrockPolygon polygon = polygons[polygonIndex];
            vertexCounts[polygonIndex] = polygon.vertices.length;
            normals[polygonIndex * 3] = polygon.normal.x;
            normals[polygonIndex * 3 + 1] = polygon.normal.y;
            normals[polygonIndex * 3 + 2] = polygon.normal.z;

            for(BedrockVertex vertex : polygon.vertices) {
               int positionIndex = vertexOffset * 3;
               positions[positionIndex] = vertex.pos.x * 0.0625F;
               positions[positionIndex + 1] = vertex.pos.y * 0.0625F;
               positions[positionIndex + 2] = vertex.pos.z * 0.0625F;
               int uvIndex = vertexOffset * 2;
               uvs[uvIndex] = vertex.u;
               uvs[uvIndex + 1] = vertex.v;
               ++vertexOffset;
            }
         }

         return new CompiledGeometry(polygons.length, vertexCounts, positions, uvs, normals);
      }
   }

   public static enum Mode {
      VANILLA,
      SCALAR;
   }
}
