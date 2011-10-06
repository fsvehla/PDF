package at.faxonline.pdf;

import java.io.InputStream;
import java.io.IOException;

public class PeekablePushbackInputStream extends java.io.PushbackInputStream
{
    public PeekablePushbackInputStream(InputStream input, int size) throws IOException
    {
        super(input, size);
    }

    public boolean isEOF() throws IOException {
        int peek = peek();
        return peek == -1;
    }

    public int peek() throws IOException
    {
        int result = read();
        unread(result);

        return result;
    }

    public char peekChar() throws IOException
    {
        return (char)peek();
    }

    public char readChar() throws IOException
    {
        return (char)read();
    }
}
