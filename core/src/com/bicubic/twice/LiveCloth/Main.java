package com.bicubic.twice.LiveCloth;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.io.FileWriter;
import java.io.IOException;

public class Main implements ApplicationListener, InputProcessor {
    public ExternalButtonsHandler externalButtonsHandler = null;

    int width, height;

    ShaderProgram shaderProgram;
    SpriteBatch spriteBatch;

    Buttons buttons;
    Camera camera;
    Cloth cloth;
    ColorEffect colorEffect;
    //Shadow shadow;
    float pointSize;
    float lineWidth;

    boolean isPaused;

    String vertexShader = "attribute vec4 a_Position;" +
            "attribute vec4 a_Color;" +
            "uniform mat4 u_ViewProjectionMatrix;" +
            "uniform float u_PointSize;" +
            "varying vec4 v_Color;" +
            "void main()" +
            "{" +
            "v_Color = a_Color;" +
            "gl_PointSize = u_PointSize;" +
            "gl_Position = u_ViewProjectionMatrix * a_Position;" +
            "}";

    String fragmentShader = "varying vec4 v_Color;" +
            "void main()" +
            "{" +
            "gl_FragColor = v_Color;" +
            "}";

    private Vector3 tempVector1 = new Vector3();
    private Vector3 tempVector2 = new Vector3();

    @Override
    public void create() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        Gdx.app.log("", String.format("w=%d  h=%d", width, height));

        for (int i = 0; i < Touches.TOUCHES_NUM_MAX; i++) {
            Touches.pointers[i] = -1;
            Touches.positions3D[i] = new Vector3();
            Touches.positions2D[i] = new Vector2();
        }

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        spriteBatch = new SpriteBatch();

        pointSize = 2.0f;
        lineWidth = 1.0f;
        String atlasFileName = "buttons-1.atlas";
        if (width > 450) {
            pointSize = 3.0f;
            lineWidth = 2.0f;
            atlasFileName = "buttons-2.atlas";
        }
        if (width > 900) {
            pointSize = 5.0f;
            lineWidth = 4.0f;
            atlasFileName = "buttons-3.atlas";
        }

        float bannerHeight = 32.0f;
        if (width > 400) bannerHeight = 50.0f;
        if (width > 720) bannerHeight = 90.0f;

        buttons = new Buttons(atlasFileName, bannerHeight, width, height);

        camera = new Camera(0.0f);
        camera.setProjection(65.0f, width, height, 0.1f, 10.0f);
        camera.update();

        cloth = new Cloth();
        cloth.initialize(new Vector3(0, -0.001f, 0), 0.99f);

        //shadow = new Shadow();
        //shadow.initialize(cloth.points, cloth.lines);

        colorEffect = new ColorEffect();
        colorEffect.initialize(cloth.points, new Vector3(1.0f, 0.4f, 0.2f));

        Gdx.gl20.glViewport(0, 0, width, height);
        Gdx.gl20.glClearColor(1, 1, 1, 1);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Gdx.input.setInputProcessor(this);

        isPaused = false;
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        camera.setProjection(65.0f, width, height, 0.1f, 10.0f);
        Gdx.gl20.glViewport(0, 0, width, height);
    }

    @Override
    public void render() {
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);

        shaderProgram.begin();
        shaderProgram.setUniformMatrix("u_ViewProjectionMatrix", camera.viewProjectionMatrix);
        shaderProgram.setUniformf("u_PointSize", pointSize);

        Gdx.gl20.glLineWidth(lineWidth);
        cloth.mesh.render(shaderProgram, GL20.GL_POINTS);
        cloth.mesh.render(shaderProgram, GL20.GL_LINES);

        //shadow.mesh.render(shaderProgram, GL20.GL_POINTS);
        //shadow.mesh.render(shaderProgram, GL20.GL_LINES);

        shaderProgram.end();

        Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);

        spriteBatch.begin();
        for (Buttons.Properties b : buttons.buttons.values()) {
            if (b.isVisible) {
                spriteBatch.draw(b.textureRegion, b.x, b.y);
            }
        }
        spriteBatch.end();

        if (!isPaused) {
            cloth.update();
            //shadow.update();
            colorEffect.update();
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        screenY = height - screenY;

        Buttons.Names buttonTouched = buttons.nameTouched(screenX, screenY);
        if (buttonTouched != Buttons.Names.NONE) {
            if (buttonTouched == Buttons.Names.ROTATE) {
                buttons.buttons.get(Buttons.Names.SLIDER).isVisible = !buttons.buttons.get(Buttons.Names.SLIDER).isVisible;
            } else if (buttonTouched == Buttons.Names.SLIDER) {
                buttons.buttons.get(Buttons.Names.SLIDER).touchPointer = pointer;
            } else if (buttonTouched == Buttons.Names.PAUSE) {
                isPaused = true;
                buttons.buttons.get(Buttons.Names.RESUME).isVisible = true;
                buttons.buttons.get(Buttons.Names.PAUSE).isVisible = false;
            } else if (buttonTouched == Buttons.Names.RESUME) {
                isPaused = false;
                buttons.buttons.get(Buttons.Names.RESUME).isVisible = false;
                buttons.buttons.get(Buttons.Names.PAUSE).isVisible = true;
            } else if (buttonTouched == Buttons.Names.SHARE) {
                if (externalButtonsHandler != null) {
                    externalButtonsHandler.shareButtonPressed();
                }
            } else if (buttonTouched == Buttons.Names.ADS) {
                // TODO: ads
            }
            return false;
        }

        camera.unProject(screenX, screenY, 0, tempVector1);
        camera.unProject(screenX, screenY, 1, tempVector2);

        // search nearest point
        int nearestIndex = cloth.findNearestIndex(tempVector1, tempVector2);
        if (nearestIndex != -1) {
            for (int i = 0; i < Touches.TOUCHES_NUM_MAX; i++) {
                if (Touches.pointers[i] == -1) {
                    Touches.pointers[i] = pointer;

                    Touches.pointIndices[i] = nearestIndex;
                    camera.unProject(screenX, screenY, 0, tempVector1);
                    camera.unProject(screenX, screenY, 1, tempVector2);
                    tempVector2.sub(tempVector1).scl(0.5f).add(tempVector1);
                    Touches.positions3D[i].set(tempVector2);

                    Touches.positions2D[i].set(nearestIndex % Cloth.CLOTH_POINTS_X, nearestIndex / Cloth.CLOTH_POINTS_X);
                    colorEffect.setMode2();
                    break;
                }
            }
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        screenY = height - screenY;

        Buttons.Names buttonDragged = buttons.nameTouched(screenX, screenY);
        if (buttonDragged == Buttons.Names.SLIDER && buttons.buttons.get(Buttons.Names.SLIDER).touchPointer == pointer) {
            camera.changeViewAngle(Gdx.input.getDeltaX(pointer));
            return false;
        }

        for (int i = 0; i < Touches.TOUCHES_NUM_MAX; i++) {
            if (Touches.pointers[i] == pointer) {
                camera.unProject(screenX, screenY, 0, tempVector1);
                camera.unProject(screenX, screenY, 1, tempVector2);
                tempVector2.sub(tempVector1).scl(0.5f).add(tempVector1);
                Touches.positions3D[i].set(tempVector2);
                break;
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        for (int i = 0; i < Touches.TOUCHES_NUM_MAX; i++) {
            if (Touches.pointers[i] == pointer) {
                Touches.pointers[i] = -1;
                colorEffect.setMode2();
                break;
            }
        }

        if (buttons.buttons.get(Buttons.Names.SLIDER).touchPointer == pointer) {
            buttons.buttons.get(Buttons.Names.SLIDER).touchPointer = -1;
        }

        if (!Gdx.input.isTouched()) {
            colorEffect.setMode1();
        }

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    public void writeLog(String message) {
        try {
            FileWriter logFile = new FileWriter("log.txt", true);
            logFile.append(message + "\r\n");
            logFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface ExternalButtonsHandler {
        void shareButtonPressed();
    }
}
