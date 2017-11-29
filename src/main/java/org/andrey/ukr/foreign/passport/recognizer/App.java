package org.andrey.ukr.foreign.passport.recognizer;

import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException, TesseractException, ParseException {

        Tesseract1 instance = new Tesseract1();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test1.png");
        BufferedImage bi = ImageIO.read(inputStream);
        String result = instance.doOCR(bi);

        System.out.println("=========== DEBUG ===========");
        int num = 0;
        for (String s : result.split("\n")) {
            System.out.println((num < 10 ? "0" : "") + num + ": " + s);
            num++;
        }
        System.out.println("=========== DEBUG ===========");

        MRZTextParser parser = new MRZTextParser();
        System.out.println(parser.parse(result));
    }
}
