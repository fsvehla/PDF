package at.faxonline.pdf;

import java.io.IOException;

public class PageCounterApp {
     public static void main(String[] args) throws IOException {
        PageCounter counter = new PageCounter(args[0]);
        System.out.println(String.format("%s: %d", args[0], counter.getPageCount()));
    }
}
