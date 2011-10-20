package at.faxonline.pdf;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class PageCounterTest {
    @Test
    public void testGetPageCountWithWellFormedParentDictionaryInStream() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

        outputStream.writeBytes("%PDF\n");

        outputStream.writeBytes("<< /Type /Pages /MediaBox [0 0 595.28 841.89] /Parent 1 /Count 1 /Kids [3 R] >>\n");
        outputStream.writeBytes("<< /Type /Pages /MediaBox [0 0 595.28 841.89] /Parent 1 /Count 6 /Kids [2 R ] >>\n");
        outputStream.writeBytes("<< /Type /Pages /MediaBox [0 0 595.28 841.89] /Count 7 /Kids [ 2 0 R ] >>\n");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        PageCounter counter = new PageCounter(inputStream);
        org.junit.Assert.assertEquals(7, counter.getPageCount());
    }
}
