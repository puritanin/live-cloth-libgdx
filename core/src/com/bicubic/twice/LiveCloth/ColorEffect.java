package com.bicubic.twice.LiveCloth;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.Random;

import static com.bicubic.twice.LiveCloth.Math.*;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class ColorEffect {

    final static int KEY_POINT_INTERVAL = 5;
    final static int MODE1 = 1;
    final static int MODE2 = 2;

    Random rnd = new Random();

    private int mode;
    private float[] clothPoints;
    private Vector3 baseColor;

    private ColorPoint[] colorPoints = new ColorPoint[Cloth.CLOTH_NUM_POINTS];
    ArrayList<Integer> keyIndices = new ArrayList();
    ArrayList<Integer> midIndices = new ArrayList();


    void initialize(float[] clothPoints, Vector3 baseColor) {
        this.clothPoints = clothPoints;
        this.baseColor = baseColor;

        mode = MODE1;

        for (int j = 0; j < Cloth.CLOTH_POINTS_Y; j++) {
            for (int i = 0; i < Cloth.CLOTH_POINTS_X; i++) {
                int index = j * Cloth.CLOTH_POINTS_X + i;

                colorPoints[index] = new ColorPoint();
                colorPoints[index].x = i;
                colorPoints[index].y = j;

                if (j % KEY_POINT_INTERVAL == 0 && i % KEY_POINT_INTERVAL == 0) {
                    // key point
                    colorPoints[index].t = 0;
                    colorPoints[index].direction = random(rnd, 0.0f, 2.0f) < 1.0f ? 1.0f : -1.0f;
                    colorPoints[index].beginValue = 0.5f - random(rnd, 0.0f, 0.3f) * colorPoints[index].direction;
                    colorPoints[index].endValue = 0.5f + random(rnd, 0.0f, 0.3f) * colorPoints[index].direction;
                    colorPoints[index].value = colorPoints[index].beginValue;

                    keyIndices.add(index);
                } else {
                    // mid point
                    int keyX = i / KEY_POINT_INTERVAL;
                    int keyY = j / KEY_POINT_INTERVAL;

                    colorPoints[index].localX = (float) (i - keyX * KEY_POINT_INTERVAL) / KEY_POINT_INTERVAL;
                    colorPoints[index].localY = (float) (j - keyY * KEY_POINT_INTERVAL) / KEY_POINT_INTERVAL;

                    int x0 = keyX * KEY_POINT_INTERVAL;
                    int y0 = keyY * KEY_POINT_INTERVAL;
                    int indexSafe = y0 * Cloth.CLOTH_POINTS_X + x0;
                    colorPoints[index].keyIndex0 = indexSafe;

                    // warning: key indices 1,2,3 maybe greater than size of points array

                    x0 = (keyX + 1) * KEY_POINT_INTERVAL;
                    y0 = keyY * KEY_POINT_INTERVAL;
                    int indexNotSafe = y0 * Cloth.CLOTH_POINTS_X + x0;
                    colorPoints[index].keyIndex1 = (x0 < Cloth.CLOTH_POINTS_X && y0 < Cloth.CLOTH_POINTS_Y) ? indexNotSafe : indexSafe;

                    x0 = keyX * KEY_POINT_INTERVAL;
                    y0 = (keyY + 1) * KEY_POINT_INTERVAL;
                    indexNotSafe = y0 * Cloth.CLOTH_POINTS_X + x0;
                    colorPoints[index].keyIndex2 = (x0 < Cloth.CLOTH_POINTS_X && y0 < Cloth.CLOTH_POINTS_Y) ? indexNotSafe : indexSafe;

                    x0 = (keyX + 1) * KEY_POINT_INTERVAL;
                    y0 = (keyY + 1) * KEY_POINT_INTERVAL;
                    indexNotSafe = y0 * Cloth.CLOTH_POINTS_X + x0;
                    colorPoints[index].keyIndex3 = (x0 < Cloth.CLOTH_POINTS_X && y0 < Cloth.CLOTH_POINTS_Y) ? indexNotSafe : indexSafe;

                    midIndices.add(index);
                }
            }
        }
    }

    void update() {
        switch (mode) {
            case MODE1:
                for (int index : keyIndices) {
                    colorPoints[index].t += 0.0167f; // [1/60]

                    if (colorPoints[index].t > 1.0f) {
                        colorPoints[index].t = 0;
                        colorPoints[index].direction = -colorPoints[index].direction;
                        colorPoints[index].beginValue = colorPoints[index].endValue;
                        colorPoints[index].endValue = 0.5f + random(rnd, 0.0f, 0.3f) * colorPoints[index].direction;
                    }

                    float brightness = (colorPoints[index].endValue - colorPoints[index].beginValue) * colorPoints[index].t + colorPoints[index].beginValue;
                    colorPoints[index].value = brightness;

                    int colorIndex = index * (Cloth.VERTEX_NUM_COMPONENTS + Cloth.COLOR_NUM_COMPONENTS) + Cloth.VERTEX_NUM_COMPONENTS;
                    clothPoints[colorIndex] = baseColor.x * brightness;
                    clothPoints[colorIndex + 1] = baseColor.y * brightness;
                    clothPoints[colorIndex + 2] = baseColor.z * brightness;
                }

                for (int index : midIndices) {
                    int index0 = colorPoints[index].keyIndex0;
                    int index1 = colorPoints[index].keyIndex1;
                    int index2 = colorPoints[index].keyIndex2;
                    int index3 = colorPoints[index].keyIndex3;

                    float brightness01 = mix(colorPoints[index0].value, colorPoints[index1].value, colorPoints[index].localX);
                    float brightness23 = mix(colorPoints[index2].value, colorPoints[index3].value, colorPoints[index].localX);
                    float brightness = mix(brightness01, brightness23, colorPoints[index].localY);
                    colorPoints[index].value = brightness;

                    int colorIndex = index * (Cloth.VERTEX_NUM_COMPONENTS + Cloth.COLOR_NUM_COMPONENTS) + Cloth.VERTEX_NUM_COMPONENTS;
                    clothPoints[colorIndex] = baseColor.x * brightness;
                    clothPoints[colorIndex + 1] = baseColor.y * brightness;
                    clothPoints[colorIndex + 2] = baseColor.z * brightness;
                }
                break;

            case MODE2:
                for (int i = 0; i < colorPoints.length; i++) {
                    if (colorPoints[i].t < 1.0f) {
                        colorPoints[i].t += 0.0333f; // [1/30]

                        float brightness = (colorPoints[i].endValue - colorPoints[i].beginValue) * colorPoints[i].t + colorPoints[i].beginValue;
                        colorPoints[i].value = brightness;

                        int colorIndex = i * (Cloth.VERTEX_NUM_COMPONENTS + Cloth.COLOR_NUM_COMPONENTS) + Cloth.VERTEX_NUM_COMPONENTS;
                        clothPoints[colorIndex] = baseColor.x * brightness;
                        clothPoints[colorIndex + 1] = baseColor.y * brightness;
                        clothPoints[colorIndex + 2] = baseColor.z * brightness;
                    }
                }
                break;

            default:
                break;
        }
    }

    void setMode1() {
        if (mode == MODE1) return;
        mode = MODE1;
        for (int index : keyIndices) {
            colorPoints[index].beginValue = colorPoints[index].value;
            colorPoints[index].direction = colorPoints[index].beginValue < 0.5f ? 1.0f : -1.0f;
            colorPoints[index].endValue = 0.5f + random(rnd, 0.0f, 0.3f) * colorPoints[index].direction;
            colorPoints[index].t = 0;
        }
    }

    void setMode2() {
        mode = MODE2;

        // init
        for (int index : midIndices) {
            colorPoints[index].beginValue = colorPoints[index].value;
            colorPoints[index].endValue = 0;
            colorPoints[index].t = 0;
        }

        for (int index : keyIndices) {
            colorPoints[index].beginValue = colorPoints[index].value;
            colorPoints[index].endValue = 0;
            colorPoints[index].t = 0;
        }

        // fill
        for (int i = 0; i < Touches.TOUCHES_NUM_MAX; i++) {
            if (Touches.pointers[i] != -1) {
                Vector2 p = Touches.positions2D[i];

                double length1 = sqrt(pow(p.x, 2) + pow(p.y, 2));
                double length2 = sqrt(pow(p.x - Cloth.CLOTH_POINTS_X, 2) + pow(p.y, 2));
                double length3 = sqrt(pow(p.x, 2) + pow(p.y - Cloth.CLOTH_POINTS_Y, 2));
                double length4 = sqrt(pow(p.x - Cloth.CLOTH_POINTS_X, 2) + pow(p.y - Cloth.CLOTH_POINTS_Y, 2));
                float lengthMax = (float) max(max(length1, length2), max(length3, length4));

                for (int index : midIndices) {
                    float endValue = 1.0f - (float) sqrt(pow(p.x - colorPoints[index].x, 2) + pow(p.y - colorPoints[index].y, 2)) / lengthMax;
                    colorPoints[index].endValue = max(colorPoints[index].endValue, endValue);
                }

                for (int index : keyIndices) {
                    float endValue = 1.0f - (float) sqrt(pow(p.x - colorPoints[index].x, 2) + pow(p.y - colorPoints[index].y, 2)) / lengthMax;
                    colorPoints[index].endValue = max(colorPoints[index].endValue, endValue);
                }
            }
        }
    }


    private class ColorPoint {
        int x;
        int y;

        float t;
        float direction;
        float beginValue;
        float endValue;
        float value;

        float localX;   // [0; 1]
        float localY;   // [0; 1]

        int keyIndex0;  //  0 * * 1
        int keyIndex1;  //  * * * *
        int keyIndex2;  //  * * * *
        int keyIndex3;  //  2 * * 3
    }
}
