package org.carlspring.strongbox.io;

import org.carlspring.commons.http.range.ByteRange;
import org.springframework.util.ReflectionUtils;

import java.io.*;
import java.lang.reflect.Field;

public class StreamUtils
{

    private StreamUtils()
    {
    }

    public static long getLength(ByteRangeInputStream bris)
    {
        return bris.getLength();
    }

    public static void setCurrentByteRange(ByteRangeInputStream bris,
                                           ByteRange byteRange)
            throws IOException
    {
        if (bris != null)
        {
            bris.setCurrentByteRange(byteRange);
            bris.skip(byteRange.getOffset());
        }
    }

    public static <T extends InputStream> T findSource(Class<T> sourceClass,
                                                       InputStream in)
    {
        if (sourceClass.isAssignableFrom(in.getClass()))
        {
            return (T) in;
        }

        Field inField = ReflectionUtils.findField(FilterInputStream.class, "in");
        if (inField != null)
        {
            inField.setAccessible(true);

            InputStream source = in;
            while (source instanceof FilterInputStream)
            {
                try
                {
                    source = (InputStream) inField.get(source);
                }
                catch (Exception e)
                {
                    return null;
                }
                if (sourceClass.isAssignableFrom(source.getClass()))
                {
                    return sourceClass.cast(source);
                }
            }
        }
        return null;
    }

    public static <T extends OutputStream> T findSource(Class<T> sourceClass,
                                                        OutputStream out)
    {
        if (sourceClass.isAssignableFrom(out.getClass()))
        {
            return (T) out;
        }

        Field outField = ReflectionUtils.findField(FilterOutputStream.class, "out");
        if (outField != null)
        {
            outField.setAccessible(true);

            OutputStream source = out;
            while (source instanceof FilterOutputStream)
            {
                try
                {
                    source = (OutputStream) outField.get(source);
                }
                catch (Exception e)
                {
                    return null;
                }
                if (sourceClass.isAssignableFrom(source.getClass()))
                {
                    return sourceClass.cast(source);
                }
            }
        }
        return null;
    }

}
