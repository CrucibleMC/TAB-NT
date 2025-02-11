package me.neznamy.component.shared.component;

import me.neznamy.component.shared.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation that only uses the "text" field with legacy colors in it, without using
 * any component style or extra.
 */
public class SimpleTextComponent extends TextComponent {

    /**
     * Constructs new instance with given text.
     *
     * @param   text
     *          Component text
     */
    public SimpleTextComponent(@NotNull String text) {
        super(text);
    }

    @Override
    @NotNull
    public String toLegacyText() {
        return text;
    }

    @Override
    @Nullable
    protected TextColor fetchLastColor() {
        char[] chars = text.toCharArray();
        for (int index = chars.length - 2; index >= 0; index--) {
            if (chars[index] != '§') continue;
            TextColor color = TextColor.getLegacyByChar(chars[index + 1]);
            if (color != null) return color;
        }
        return null;
    }
}
