package org.mskcc.cgds.test.scripts;

import junit.framework.TestCase;
import org.mskcc.cgds.scripts.CutInvalidCases;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.File;

public class TestCutInvalidCases extends TestCase {

    public void testCutInvalidCases() throws Exception {
        File casesExcludedFile = new File("testData/cases_excluded_test.txt");
        File dataFile = new File("testData/cna_test.txt");
        ProgressMonitor pMonitor = new ProgressMonitor();
        CutInvalidCases parser = new CutInvalidCases(casesExcludedFile,
                dataFile, pMonitor);
        String out = parser.process();

        String lines[] = out.split("\n");
        String headerLine = lines[0];
        String parts[] = headerLine.split("\t");
        for (String header : parts) {
            if (header.trim().equals("TCGA-06-0142")) {
                fail("TCGA-06-0142 should have been stripped out.");
            } else if (header.trim().equals("TCGA-06-0151")) {
                fail("TCGA-06-0142 should have been stripped out.");
            } else if (header.trim().equals("TCGA-06-0159")) {
                fail("TCGA-06-0159 should have been stripped out.");
            }
        }
        int numHeaders = parts.length;
        parts = lines[3].split("\t");

        //  Should go from 95 to 93 columns.
        assertEquals (93, numHeaders);
        assertEquals (93, parts.length);
    }
}
