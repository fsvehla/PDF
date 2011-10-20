package at.faxonline.pdf;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;

import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    A PDF document contains multiple Hash / Dictionary style objects that describe it's structure.
    The page count is defined in the root 'Pages' element as the 'Count' attribute.
*/

public class PageCounter
{
    private static final Pattern isPageDictionary = Pattern.compile("/Type\\s?/Pages");
    private static final Pattern countExtraction  = Pattern.compile("/Count\\s(\\d+)");

    private String path;
    private InputStream inputStream;
    private PeekablePushbackInputStream stream;

    public PageCounter(String path)
    {
        this.path = path;
    }

    public PageCounter(InputStream inputStream)
    {
        this.inputStream = inputStream;
    }

    public int getPageCount() throws IOException
    {
        open();

        int     pageCount      = 1;
        boolean foundPageCount = false;

        int nextByte;
        int prevByte = 0;

         while(!stream.isEOF() && !foundPageCount) {
            nextByte = stream.read();

            // This is a hack to make sure that we don't encounter "<<" in a stream, and much
            // easier than scan the whole structure for stream / endstream
            if ((char)nextByte == '<' && stream.peekChar() == '<' && (prevByte == 10 || prevByte == 13)) {
                //noinspection ResultOfMethodCallIgnored
                stream.read(); // Skip the second "<"

                boolean eod = false;
                StringBuilder stringBuilder = new StringBuilder();

                while (! eod) {
                    char nextChar = stream.readChar();

                    if(nextChar == '>' && stream.readChar() == '>') {
                        String string = stringBuilder.toString();

                        if (isPageDictionary.matcher(string).find()) {
                            boolean hasParent = string.contains("/Parent");
                            boolean hasKids   = string.contains("/Kids");

                            if (! hasParent && hasKids && string.contains("/Count")) {
                                Matcher matcher = countExtraction.matcher(string);

                                if(matcher.find()) {
                                    pageCount = Integer.parseInt(matcher.group(1));
                                    foundPageCount = true;
                                }
                            }
                        }

                        eod = true;
                    } else {
                        stringBuilder.append(nextChar);
                    }
                }
            }

            prevByte = nextByte;
        }

        if (! foundPageCount) {
            System.err.println("Found no page count.");
        }

        close();
        return pageCount;
    }

    private void open() throws IOException {
        if (inputStream == null) {
          // get stream from path
          inputStream = new FileInputStream(path);
        }
        // else use provided input stream

        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, 4096);
        stream = new PeekablePushbackInputStream(bufferedInputStream, 2);
    }

     private void close() throws IOException {
        stream.close();
        inputStream.close();
    }
}
