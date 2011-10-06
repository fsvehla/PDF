package at.faxonline.pdf;

import java.io.FileInputStream;
import java.io.BufferedInputStream;

import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    A PDF document contains multiple Hash / Dictionary style objects that describe it's structure.
    The page count is defined in the root 'Pages' element as the 'Count' attribute.
*/

public class PageCounter
{
    public static final Pattern isPageDictionary = Pattern.compile("/Type\\s?/Pages");
    public static final Pattern countExtraction  = Pattern.compile("/Count\\s(\\d+)");

    String path;

    public PageCounter(String path)
    {
        this.path = path;
    }

    public int getPageCount() throws IOException
    {
        FileInputStream             inputStream         = new FileInputStream(path);
        BufferedInputStream         bufferedInputStream = new BufferedInputStream(inputStream, 4096);
        PeekablePushbackInputStream stream = new PeekablePushbackInputStream(bufferedInputStream, 2);

        int     pageCount      = 1;
        boolean foundPageCount = false;

        int nextByte;
        int prevByte = 0;

         while(stream.isEOF() == false && foundPageCount == false) {
            nextByte = stream.read();

            // This is a hack to make sure that we don't encounter "<<" in a stream, and much
            // easier than scan the whole structure for stream / endstream
            if ((char)nextByte == '<' && stream.peekChar() == '<' && (prevByte == 10 || prevByte == 13)) {
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

        inputStream.close();
        return pageCount;
    }
}
