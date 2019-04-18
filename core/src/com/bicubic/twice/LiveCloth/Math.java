package com.bicubic.twice.LiveCloth;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;

import java.util.Random;

public class Math {
    public static void mulMatrixVec4(Matrix4 m, Quaternion v) {
        float x = m.val[Matrix4.M00] * v.x + m.val[Matrix4.M01] * v.y + m.val[Matrix4.M02] * v.z + m.val[Matrix4.M03] * v.w;
        float y = m.val[Matrix4.M10] * v.x + m.val[Matrix4.M11] * v.y + m.val[Matrix4.M12] * v.z + m.val[Matrix4.M13] * v.w;
        float z = m.val[Matrix4.M20] * v.x + m.val[Matrix4.M21] * v.y + m.val[Matrix4.M22] * v.z + m.val[Matrix4.M23] * v.w;
        float w = m.val[Matrix4.M30] * v.x + m.val[Matrix4.M31] * v.y + m.val[Matrix4.M32] * v.z + m.val[Matrix4.M33] * v.w;
        v.x = x;
        v.y = y;
        v.z = z;
        v.w = w;
    }

    public static float random(Random rnd, float min, float max) {
        return (float)(rnd.nextDouble() * (double)(max - min) + (double)min);
    }

    public static float mix(float v1, float v2, float t) {
        float delta = v2 - v1;
        return v1 + delta * t;
    }
}
