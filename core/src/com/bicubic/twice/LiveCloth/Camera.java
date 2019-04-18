package com.bicubic.twice.LiveCloth;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Camera {
    Matrix4 viewMatrix;
    Matrix4 projectionMatrix;
    Matrix4 viewProjectionMatrix;
    Matrix4 clearMatrix;

    private float fov;
    private float width, height;
    private float near, far;
    private float viewAngle;

    private Matrix4 tempMatrix1 = new Matrix4();
    private Quaternion tempQuaternion = new Quaternion();

    public Camera(float viewAngle) {
        viewMatrix = new Matrix4();
        projectionMatrix = new Matrix4();
        viewProjectionMatrix = new Matrix4();
        clearMatrix = new Matrix4();
        clearMatrix.val[Matrix4.M00] = 1;
        clearMatrix.val[Matrix4.M11] = 1;
        clearMatrix.val[Matrix4.M22] = 1;
        clearMatrix.val[Matrix4.M33] = 1;
        this.viewAngle = viewAngle;
    }

    public void update() {
        viewMatrix.set(clearMatrix);
        viewMatrix.translate(0, 0, -4.0f);
        viewMatrix.rotate(0, 1, 0, viewAngle);
        viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix);
    }

    public void setProjection(float angle, int width, int height, float near, float far) {
        this.fov = angle;
        this.width = (float)width;
        this.height = (float)height;
        this.near = near;
        this.far = far;
        projectionMatrix.setToProjection(near, far, angle, (float) width / (float) height);
        viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix);
    }

    public void changeViewAngle(float delta) {
        viewAngle -= delta;
        update();
    }

    public void unProject(float x, float y, float z, Vector3 result) {
        tempMatrix1.set(viewProjectionMatrix).inv();
        tempQuaternion.set(x, y, z, 1.0f);
        tempQuaternion.x = tempQuaternion.x / width;
        tempQuaternion.y = tempQuaternion.y / height;
        tempQuaternion.x = tempQuaternion.x * 2.0f - 1.0f;
        tempQuaternion.y = tempQuaternion.y * 2.0f - 1.0f;
        tempQuaternion.z = tempQuaternion.z * 2.0f - 1.0f;
        Math.mulMatrixVec4(tempMatrix1, tempQuaternion);
        tempQuaternion.w = 1.0f / tempQuaternion.w;
        result.x = tempQuaternion.x * tempQuaternion.w;
        result.y = tempQuaternion.y * tempQuaternion.w;
        result.z = tempQuaternion.z * tempQuaternion.w;
    }
}
