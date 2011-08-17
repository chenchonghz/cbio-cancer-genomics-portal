package org.mskcc.cgds.scripts;

/*
 * @author Lennart Bastian
 * ImportMutSig is used to import the Broad Institutes MutSig data for different Cancer types
 * into our CGDS SQL database.
 * Command line users must specify a MutSig file, and properties file containing a CancerID.
 */

import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.ProgressMonitor;

import java.util.*;
import java.io.*;


public class ImportMutSigData {
    private ProgressMonitor pMonitor;
    private File mutSigFile;
    private File metaDataFile;

    public ImportMutSigData(File mutSigFile, File metaDataFile, ProgressMonitor pMonitor) {
        this.mutSigFile = mutSigFile;
        this.pMonitor = pMonitor;
        this.metaDataFile = metaDataFile;
    }

    //method responsible for parsing MutSig data, and adding individual 'MutSig' objects to CDGS database.

    public void importData() throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(mutSigFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        int cancerType = loadProps();
        while (line != null) {

            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
            if (!line.startsWith("rank"))
                if (!line.startsWith("#")) {
                    String parts[] = line.split("\t");
                    int rank = Integer.parseInt(parts[0]);
                    String hugoGeneSymbol = parts[1];
                    int N = Integer.parseInt(parts[2]);
                    int n = Integer.parseInt(parts[3]);
                    int nVal = Integer.parseInt(parts[4]);
                    int nVer = Integer.parseInt(parts[5]);
                    int CpG = Integer.parseInt(parts[6]);
                    int CandG = Integer.parseInt(parts[7]);
                    int AandT = Integer.parseInt(parts[8]);
                    int Indel = Integer.parseInt(parts[9]);
                    String pValue = parts[10];
                    String qValue = parts[11];
                    CanonicalGene gene = daoGene.getGene(hugoGeneSymbol);
                    //check if gene is null, if it is, re-assign an EntrezGeneID of 0, and log to pMonitor
                    //this way data can still be found in CGDS and Gene can be manually assigned an EntrezID
                    if (gene == null) {
                        gene = new CanonicalGene(0, hugoGeneSymbol);
                        pMonitor.logWarning("Invalid gene symbol:  " + hugoGeneSymbol);
                    }
                    MutSig mutSig = new MutSig(cancerType, gene, rank, N, n, nVal, nVer, CpG, CandG, AandT, Indel, pValue, qValue);
                    DaoMutSig.addMutSig(mutSig);
                }
            line = buf.readLine();
        }
        if (MySQLbulkLoader.isBulkLoad()) {
            //daoMutSig.flushGenesToDatabase();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("command line usage:  importMutSig.pl <Mutsig_file.txt> <MetaProperties.txt>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File mutSigFile = new File(args[0]);
        File propertiesFile = new File(args[1]);
        System.out.println("Reading data from: " + mutSigFile.getAbsolutePath());
        System.out.println("Properties: " + propertiesFile.getAbsolutePath());
        int numLines = FileUtil.getNumLines(mutSigFile);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportMutSigData parser = new ImportMutSigData(mutSigFile, propertiesFile, pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
    }

    //parses MutSig properties file and extracts CancerStudyID

    private int loadProps() throws IOException, DaoException {
        Properties props = new Properties();
        props.load(new FileInputStream(metaDataFile));
        String cancer_study_identifier;
        cancer_study_identifier = props.getProperty("cancer_study_identifier");
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancer_study_identifier);
        int cancerStudyID = cancerStudy.getInternalId();
        return cancerStudyID;
    }


}


