package com.bicubic.twice.LiveCloth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class MainGdxActivity extends AndroidApplication implements Main.ExternalButtonsHandler {

    private RelativeLayout mainLayout;
    private View mainView;

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useAccelerometer = false;
        cfg.useCompass = false;
        cfg.useWakelock = true;

        Main mainLogic = new Main();
        mainLogic.externalButtonsHandler = this;

        mainView = initializeForView(mainLogic, cfg);
        mainLayout = new RelativeLayout(this);
        mainLayout.addView(mainView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        setContentView(mainLayout);
    }

    @Override
    public void shareButtonPressed() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Live Cloth demo project");
        intent.putExtra(Intent.EXTRA_TEXT, "https://github.com/puritanin/");
        startActivity(Intent.createChooser(intent, "How do you want to share?"));
    }
}
