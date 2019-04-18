package com.bicubic.twice.LiveCloth;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Touches {
    static final int TOUCHES_NUM_MAX = 2;

    static int[] pointers = new int[TOUCHES_NUM_MAX];
    static int[] pointIndices = new int[TOUCHES_NUM_MAX];
    static Vector3[] positions3D = new Vector3[TOUCHES_NUM_MAX];
    static Vector2[] positions2D = new Vector2[TOUCHES_NUM_MAX];
}
