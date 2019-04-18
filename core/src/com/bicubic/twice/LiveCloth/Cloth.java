package com.bicubic.twice.LiveCloth;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector3;

public class Cloth {
    static final int CLOTH_POINTS_X = 25;
    static final int CLOTH_POINTS_Y = 21;
    static final int CLOTH_NUM_POINTS = CLOTH_POINTS_X * CLOTH_POINTS_Y;
    static final int VERTEX_NUM_COMPONENTS = 4;
    static final int COLOR_NUM_COMPONENTS = 4;

    float[] points = new float[CLOTH_NUM_POINTS * (VERTEX_NUM_COMPONENTS + COLOR_NUM_COMPONENTS)];
    float[] lastPoints = new float[CLOTH_NUM_POINTS * (VERTEX_NUM_COMPONENTS + COLOR_NUM_COMPONENTS)];
    short[] lines = new short[2 * ((CLOTH_POINTS_X - 1) * CLOTH_POINTS_Y + (CLOTH_POINTS_Y - 1) * CLOTH_POINTS_X)];

    StiffnessConstraint constraints[] = new StiffnessConstraint[lines.length / 2];

    int[] pinIndices = new int[]{0, 5, 10, 15, 20, 24};
    PinConstraint[] pins = new PinConstraint[pinIndices.length];

    Vector3 gravity;
    float friction;

    Mesh mesh;

    private Vector3 tempVector1 = new Vector3();
    private Vector3 tempVector2 = new Vector3();
    private Vector3 tempVector3 = new Vector3();

    void initialize(Vector3 gravity, float friction) {
        this.gravity = gravity;
        this.friction = friction;

        // points
        int index = 0;
        float y = 1.3f;
        for (int j = 0; j < CLOTH_POINTS_Y; j++) {
            float x = -1.2f;
            for (int i = 0; i < CLOTH_POINTS_X; i++) {
                index = (j * CLOTH_POINTS_X + i) * (VERTEX_NUM_COMPONENTS + COLOR_NUM_COMPONENTS);
                // vertex
                lastPoints[index] = points[index++] = x;
                lastPoints[index] = points[index++] = y;
                lastPoints[index] = points[index++] = 0;
                lastPoints[index] = points[index++] = 1;
                // color
                points[index++] = 0;
                points[index++] = 0;
                points[index++] = 0;
                points[index++] = 1.0f;
                x += 0.1f;
            }
            y -= 0.1f;
        }

        // lines
        index = 0;
        for (int j = 0; j < CLOTH_POINTS_Y; j++) {
            for (int i = 0; i < CLOTH_POINTS_X - 1; i++) {
                lines[index++] = (short) (j * CLOTH_POINTS_X + i);
                lines[index++] = (short) (j * CLOTH_POINTS_X + i + 1);
            }
        }
        for (int j = 0; j < CLOTH_POINTS_Y - 1; j++) {
            for (int i = 0; i < CLOTH_POINTS_X; i++) {
                lines[index++] = (short) (j * CLOTH_POINTS_X + i);
                lines[index++] = (short) ((j + 1) * CLOTH_POINTS_X + i);
            }
        }

        mesh = new Mesh(false, CLOTH_NUM_POINTS, lines.length,
                new VertexAttribute(VertexAttributes.Usage.Position, VERTEX_NUM_COMPONENTS, "a_Position"),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, COLOR_NUM_COMPONENTS, "a_Color"));
        mesh.setVertices(points);
        mesh.setIndices(lines);

        // constraints
        for (int i = 0; i < constraints.length; i++) {
            constraints[i] = new StiffnessConstraint();
            int p1 = lines[i * 2] * (VERTEX_NUM_COMPONENTS + COLOR_NUM_COMPONENTS);
            int p2 = lines[i * 2 + 1] * (VERTEX_NUM_COMPONENTS + COLOR_NUM_COMPONENTS);
            constraints[i].aIndex = p1;
            constraints[i].bIndex = p2;
            constraints[i].distance = 0.98f * Vector3.dst(points[p1], points[p1 + 1], points[p1 + 2], points[p2], points[p2 + 1], points[p2 + 2]);
            constraints[i].stiffness = 0.8f;
        }

        // pins
        for (int i = 0; i < pins.length; i++) {
            pins[i] = new PinConstraint();
            int p1 = pinIndices[i] * (VERTEX_NUM_COMPONENTS + COLOR_NUM_COMPONENTS);
            pins[i].aIndex = p1;
            pins[i].position.x = points[p1];
            pins[i].position.y = points[p1 + 1];
            pins[i].position.z = points[p1 + 2];
        }
    }

    void update() {
        // points
        for (int i = 0; i < CLOTH_NUM_POINTS; i++) {
            int index = i * (VERTEX_NUM_COMPONENTS + COLOR_NUM_COMPONENTS);

            tempVector1.set(points[index], points[index + 1], points[index + 2]);
            tempVector2.set(lastPoints[index], lastPoints[index + 1], lastPoints[index + 2]);
            tempVector3.set(tempVector1).sub(tempVector2).scl(friction);

            // save last good state
            lastPoints[index] = points[index];
            lastPoints[index + 1] = points[index + 1];
            lastPoints[index + 2] = points[index + 2];

            // gravity
            tempVector1.add(gravity);
            // inertia
            tempVector1.add(tempVector3);

            points[index] = tempVector1.x;
            points[index + 1] = tempVector1.y;
            points[index + 2] = tempVector1.z;
        }

        // touches
        for (int i = 0; i < Touches.TOUCHES_NUM_MAX; i++) {
            if (Touches.pointers[i] != -1) {
                int index = Touches.pointIndices[i] * (VERTEX_NUM_COMPONENTS + COLOR_NUM_COMPONENTS);
                points[index] = Touches.positions3D[i].x;
                points[index + 1] = Touches.positions3D[i].y;
                points[index + 2] = Touches.positions3D[i].z;
            }
        }

        // constraints
        float step = 1.0f / 5;
        for (int n = 0; n < 5; n++) {
            for (StiffnessConstraint constraint : constraints) {
                int index1 = constraint.aIndex;
                int index2 = constraint.bIndex;
                tempVector1.set(points[index1], points[index1 + 1], points[index1 + 2]);
                tempVector2.set(points[index2], points[index2 + 1], points[index2 + 2]);

                tempVector3.set(tempVector1).sub(tempVector2);
                float m = tempVector3.dot(tempVector3);
                float scalar = ((constraint.distance * constraint.distance - m) / m) * constraint.stiffness * step;
                tempVector3.scl(scalar);
                tempVector1.add(tempVector3);
                tempVector2.sub(tempVector3);

                points[index1] = tempVector1.x;
                points[index1 + 1] = tempVector1.y;
                points[index1 + 2] = tempVector1.z;
                points[index2] = tempVector2.x;
                points[index2 + 1] = tempVector2.y;
                points[index2 + 2] = tempVector2.z;
            }
        }

        // pins
        for (PinConstraint pin : pins) {
            int index = pin.aIndex;
            points[index] = pin.position.x;
            points[index + 1] = pin.position.y;
            points[index + 2] = pin.position.z;
        }

        // ground restrict
        for (int i = 0; i < CLOTH_NUM_POINTS; i++) {
            int index = i * (VERTEX_NUM_COMPONENTS + COLOR_NUM_COMPONENTS);
            if (points[index + 1] < -1.3f) points[index + 1] = -1.3f;
        }

        mesh.updateVertices(0, points);
    }

    int findNearestIndex(Vector3 nearPoint, Vector3 farPoint) {
        int nearestIndex = -1;
        float minDistance = 99.0f;

        tempVector3.set(farPoint).sub(nearPoint);
        float length = tempVector3.len();

        for (int i = 0; i < CLOTH_NUM_POINTS; i++) {
            int index = i * (VERTEX_NUM_COMPONENTS + COLOR_NUM_COMPONENTS);
            tempVector1.set(points[index], points[index + 1], points[index + 2]).sub(nearPoint);
            tempVector2.set(points[index], points[index + 1], points[index + 2]).sub(farPoint);

            float d = tempVector1.crs(tempVector2).len() / length;

            if (d < minDistance) {
                minDistance = d;
                nearestIndex = i;
            }
        }
        return minDistance < 0.1f ? nearestIndex : -1;
    }


    public class PinConstraint {
        public int aIndex;
        public Vector3 position = new Vector3();
    }

    public class StiffnessConstraint {
        public int aIndex;
        public int bIndex;
        public float distance;
        public float stiffness;
    }
}
