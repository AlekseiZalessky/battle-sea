package com.battlesea.button;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class ButtonStyles {

    public static TextButton.TextButtonStyle createDefaultStyle(BitmapFont font) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        // Активная кнопка (синяя)
        pixmap.setColor(0.2f, 0.4f, 0.6f, 1);
        pixmap.fill();
        Texture activeTexture = new Texture(pixmap);
        TextureRegionDrawable activeUp = new TextureRegionDrawable(new TextureRegion(activeTexture));

        // Неактивная кнопка (серая)
        pixmap.setColor(0.4f, 0.4f, 0.4f, 1);
        pixmap.fill();
        Texture disabledTexture = new Texture(pixmap);
        TextureRegionDrawable disabledUp = new TextureRegionDrawable(new TextureRegion(disabledTexture));

        // Кнопка при наведении (светло-синяя)
        pixmap.setColor(0.3f, 0.5f, 0.7f, 1);
        pixmap.fill();
        Texture overTexture = new Texture(pixmap);
        TextureRegionDrawable overUp = new TextureRegionDrawable(new TextureRegion(overTexture));

        // Кнопка при нажатии (темно-синяя)
        pixmap.setColor(0.1f, 0.2f, 0.4f, 1);
        pixmap.fill();
        Texture downTexture = new Texture(pixmap);
        TextureRegionDrawable downUp = new TextureRegionDrawable(new TextureRegion(downTexture));

        pixmap.dispose();

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = Color.WHITE;
        style.up = activeUp;
        style.over = overUp;
        style.down = downUp;
        style.disabledFontColor = Color.GRAY;
        style.disabled = disabledUp;

        return style;
    }
}
