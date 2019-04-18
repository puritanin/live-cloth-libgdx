package com.bicubic.twice.LiveCloth;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.HashMap;
import java.util.Map;

public class Buttons {
    private TextureAtlas textureAtlas;
    public Map<Names, Properties> buttons = new HashMap<Names, Properties>();

    public Buttons(String atlasFileName, float bannerHeight, float viewWidth, float viewHeight) {
        textureAtlas = new TextureAtlas(Gdx.files.internal(atlasFileName));
        buttons.put(Names.SHARE, new Properties(0, 0, textureAtlas.findRegion("share-black"), true));
        buttons.put(Names.ADS, new Properties(0, 0, textureAtlas.findRegion("ads-black"), false));
        buttons.put(Names.PAUSE, new Properties(0, 0, textureAtlas.findRegion("pause-black"), true));
        buttons.put(Names.RESUME, new Properties(0, 0, textureAtlas.findRegion("resume-black"), false));
        buttons.put(Names.ROTATE, new Properties(0, 0, textureAtlas.findRegion("rotate-black"), true));
        buttons.put(Names.SLIDER, new Properties(0, 0, textureAtlas.findRegion("slider-black"), false));

        float xOffset = buttons.get(Names.SHARE).textureRegion.getRegionWidth() / 8.0f;
        float yOffset = viewHeight - xOffset;
        buttons.get(Names.SHARE).x = xOffset;
        buttons.get(Names.SHARE).y = yOffset - buttons.get(Names.SHARE).textureRegion.getRegionHeight();
        buttons.get(Names.ADS).x = xOffset + buttons.get(Names.SHARE).textureRegion.getRegionWidth() + xOffset;
        buttons.get(Names.ADS).y = yOffset - buttons.get(Names.ADS).textureRegion.getRegionHeight();
        buttons.get(Names.ROTATE).x = viewWidth - buttons.get(Names.ROTATE).textureRegion.getRegionWidth() - xOffset;
        buttons.get(Names.ROTATE).y = yOffset - buttons.get(Names.ROTATE).textureRegion.getRegionHeight();
        buttons.get(Names.PAUSE).x = viewWidth - buttons.get(Names.ROTATE).textureRegion.getRegionWidth() - xOffset - buttons.get(Names.PAUSE).textureRegion.getRegionWidth() - xOffset;
        buttons.get(Names.PAUSE).y = yOffset - buttons.get(Names.PAUSE).textureRegion.getRegionHeight();
        buttons.get(Names.RESUME).x = buttons.get(Names.PAUSE).x;
        buttons.get(Names.RESUME).y = buttons.get(Names.PAUSE).y;
        buttons.get(Names.SLIDER).x = (viewWidth - buttons.get(Names.SLIDER).textureRegion.getRegionWidth()) / 2.0f;
        buttons.get(Names.SLIDER).y = bannerHeight + xOffset * 2 + buttons.get(Names.SLIDER).textureRegion.getRegionHeight();
    }

    public Names nameTouched(int x, int y) {
        Rectangle rect = new Rectangle();
        for (Names name : buttons.keySet()) {
            if (buttons.get(name).isVisible) {
                rect.set(buttons.get(name).x, buttons.get(name).y, buttons.get(name).textureRegion.getRegionWidth(), buttons.get(name).textureRegion.getRegionHeight());
                if (rect.contains(x, y)) return name;
            }
        }
        return Names.NONE;
    }

    public class Properties {
        float x;
        float y;
        TextureRegion textureRegion;
        boolean isVisible;
        int touchPointer;  // for slider only

        public Properties(float x, float y, TextureRegion textureRegion, boolean isVisible) {
            this.x = x;
            this.y = y;
            this.textureRegion = textureRegion;
            this.isVisible = isVisible;
            this.touchPointer = -1;
        }
    }

    public enum Names {
        SHARE,
        ADS,
        PAUSE,
        RESUME,
        ROTATE,
        SLIDER,
        NONE
    }
}
