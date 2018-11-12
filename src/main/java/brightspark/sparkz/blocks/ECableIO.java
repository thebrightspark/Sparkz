package brightspark.sparkz.blocks;

import net.minecraft.util.IStringSerializable;

import java.util.Locale;

/**
 * Cable side IOs
 * Note that these are relative to the cable
 */
public enum ECableIO implements IStringSerializable
{
    NONE,
    NEUTRAL,
    INPUT,
    OUTPUT;

    public static ECableIO getFromIndex(int index)
    {
        return index < 0 || index >= values().length ? null : values()[index];
    }

    @Override
    public String getName()
    {
        return name().toLowerCase(Locale.ROOT);
    }

    public ECableIO next()
    {
        int i = ordinal() + 1;
        if(i >= values().length)
            i = 0;
        return values()[i];
    }
}
