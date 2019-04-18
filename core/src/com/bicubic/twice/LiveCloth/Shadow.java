package com.bicubic.twice.LiveCloth;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Shadow {

    Mesh mesh;
    float points[] = new float[Cloth.CLOTH_NUM_POINTS * (Cloth.VERTEX_NUM_COMPONENTS + Cloth.COLOR_NUM_COMPONENTS)];

    private float clothPoints[];
    private Quaternion groundPlane = new Quaternion();
    private Vector3 lightDirection = new Vector3();
    private Matrix4 projectionMatrix = new Matrix4();

    private Quaternion tempQuaternion = new Quaternion();

    void initialize(float[] clothPoints, short[] clothLines) {
        this.clothPoints = clothPoints;

        Vector3 v = new Vector3(0, 1, 0);
        groundPlane.set(v.x, v.y, v.z, -1.3f);
        lightDirection.set(0.0f, -1.0f, 0.0f).nor();

        float dot = groundPlane.x * lightDirection.x + groundPlane.y * lightDirection.y + groundPlane.z * lightDirection.z + groundPlane.w * 1;

        projectionMatrix.val[Matrix4.M00] = dot - lightDirection.x * groundPlane.x;
        projectionMatrix.val[Matrix4.M10] = -lightDirection.x * groundPlane.y;
        projectionMatrix.val[Matrix4.M20] = -lightDirection.x * groundPlane.z;
        projectionMatrix.val[Matrix4.M30] = -lightDirection.x * groundPlane.w;

        projectionMatrix.val[Matrix4.M01] = -lightDirection.y * groundPlane.x;
        projectionMatrix.val[Matrix4.M11] = dot - lightDirection.y * groundPlane.y;
        projectionMatrix.val[Matrix4.M21] = -lightDirection.y * groundPlane.z;
        projectionMatrix.val[Matrix4.M31] = -lightDirection.y * groundPlane.w;

        projectionMatrix.val[Matrix4.M02] = -lightDirection.z * groundPlane.x;
        projectionMatrix.val[Matrix4.M12] = -lightDirection.z * groundPlane.y;
        projectionMatrix.val[Matrix4.M22] = dot - lightDirection.z * groundPlane.z;
        projectionMatrix.val[Matrix4.M32] = -lightDirection.z * groundPlane.w;

        projectionMatrix.val[Matrix4.M03] = -1 * groundPlane.x;
        projectionMatrix.val[Matrix4.M13] = -1 * groundPlane.y;
        projectionMatrix.val[Matrix4.M23] = -1 * groundPlane.z;
        projectionMatrix.val[Matrix4.M33] = dot - 1 * groundPlane.w;

        /*projectionMatrix.val[Matrix4.M00] = groundPlane.y * lightDirection.y + groundPlane.z * lightDirection.z;
        projectionMatrix.val[Matrix4.M10] = -groundPlane.x * lightDirection.y;
        projectionMatrix.val[Matrix4.M20] = -groundPlane.x * lightDirection.z;
        projectionMatrix.val[Matrix4.M30] = 0.0f;

        projectionMatrix.val[Matrix4.M01] = -groundPlane.y * lightDirection.x;
        projectionMatrix.val[Matrix4.M11] = groundPlane.x * lightDirection.x + groundPlane.z * lightDirection.z;
        projectionMatrix.val[Matrix4.M21] = -groundPlane.y * lightDirection.z;
        projectionMatrix.val[Matrix4.M31] = 0.0f;

        projectionMatrix.val[Matrix4.M02] = -groundPlane.z * lightDirection.x;
        projectionMatrix.val[Matrix4.M12] = -groundPlane.z * lightDirection.y;
        projectionMatrix.val[Matrix4.M22] = groundPlane.x * lightDirection.x + groundPlane.y * lightDirection.y;
        projectionMatrix.val[Matrix4.M32] = 0.0f;

        projectionMatrix.val[Matrix4.M03] = -groundPlane.w * lightDirection.x;
        projectionMatrix.val[Matrix4.M13] = -groundPlane.w * lightDirection.y;
        projectionMatrix.val[Matrix4.M23] = -groundPlane.w * lightDirection.z;
        projectionMatrix.val[Matrix4.M33] = groundPlane.x * lightDirection.x + groundPlane.y * lightDirection.y + groundPlane.z * lightDirection.z;*/

        mesh = new Mesh(false, Cloth.CLOTH_NUM_POINTS, clothLines.length,
                new VertexAttribute(VertexAttributes.Usage.Position, Cloth.VERTEX_NUM_COMPONENTS, "a_Position"),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, Cloth.COLOR_NUM_COMPONENTS, "a_Color"));
        mesh.setVertices(points);
        mesh.setIndices(clothLines);

        update();
    }

    void update() {
        for (int i = 0; i < Cloth.CLOTH_NUM_POINTS; i++) {
            int index = i * (Cloth.VERTEX_NUM_COMPONENTS + Cloth.COLOR_NUM_COMPONENTS);

            tempQuaternion.x = clothPoints[index];
            tempQuaternion.y = clothPoints[index + 1];
            tempQuaternion.z = clothPoints[index + 2];
            tempQuaternion.w = 1.0f;

            Math.mulMatrixVec4(projectionMatrix, tempQuaternion);

            points[index] = tempQuaternion.x;
            points[index + 1] = tempQuaternion.y;
            points[index + 2] = tempQuaternion.z;
            points[index + 3] = 1.0f;

            float distance = Vector3.dst(points[index], points[index + 1], points[index + 2], clothPoints[index], clothPoints[index + 1], clothPoints[index + 2]);
            distance /= 2.5f;
            if (distance > 1.0f) distance = 1.0f;

            index += Cloth.VERTEX_NUM_COMPONENTS;
            float value = 0.3f + 0.55f * distance;
            points[index] = value;
            points[index + 1] = value;
            points[index + 2] = value;
            points[index + 3] = 1.0f;
        }

        mesh.updateVertices(0, points);
    }
}
