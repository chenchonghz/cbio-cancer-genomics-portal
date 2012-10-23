/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.oncotator;


import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;

import java.io.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

/**
 * Command Line Tool to Oncotate a Single MAF File.
 */
public class OncotateTool
{
    protected static final String TAB = "\t";
	protected static final String SILENT_MUTATION = "Silent";
    protected static int MAX_NUM_RECORDS_TO_PROCESS = -1;
    private static final int DEFAULT_ONCO_HEADERS_COUNT = 5;

	protected int buildNumErrors = 0;
	protected OncotatorService oncotatorService;

    //private HashMap<String, Integer> genomicCountMap;

	/**
	 * Default constructor with the default oncotator service.
	 */
    public OncotateTool()
    {
	    this.oncotatorService = new OncotatorService();
	    //this.genomicCountMap = new HashMap<String, Integer>();
    }

	/**
	 * Alternative constructor with a specific oncotator service.
	 */
	public OncotateTool(OncotatorService oncotatorService)
	{
		this.oncotatorService = oncotatorService;
	}

	/**
	 * Oncotates the given input MAF file and creates a new MAF
	 * file with new/updated oncotator columns.
	 *
	 * @param inputMafFile  input MAF
	 * @param outputMafFile output MAF
	 * @param noCache       flag to indicate whether to use cache or not
	 * @return              number of errors (if any) during the process
	 * @throws Exception    if an (IO or service) Exception occurs
	 */
	protected int oncotateMaf(File inputMafFile,
			File outputMafFile,
			boolean noCache) throws Exception
	{
		// determine whether to use the DB cache or not
		this.oncotatorService.setUseCache(!noCache);

		outputFileNames(inputMafFile, outputMafFile);

		FileReader reader = new FileReader(inputMafFile);
		BufferedReader bufReader = new BufferedReader(reader);
		String headerLine = bufReader.readLine();
		MafUtil mafUtil = new MafUtil(headerLine);
		String dataLine = bufReader.readLine();

		int numRecordsProcessed = 0;
		FileWriter writer = new FileWriter(outputMafFile);

		headerLine = this.removeExistingOncoHeaders(headerLine,
			calculateOncoHeaderCount(mafUtil));

		this.writeHeaders(headerLine, writer);

		while (dataLine != null)
		{
			MafRecord mafRecord = mafUtil.parseRecord(dataLine);
			String variantClassification = mafRecord.getVariantClassification();

			// adjust data line before writing to make sure the consistency
			// among the lines
			writer.write(this.adjustDataLine(dataLine, mafUtil));

			//  Skip Silent Mutations
			if (!variantClassification.equalsIgnoreCase(SILENT_MUTATION)) {
				conditionallyOncotateRecord(mafRecord, writer);
				numRecordsProcessed++;
				conditionallyAbort(numRecordsProcessed);
			} else {
				// keep everything empty, but output "silent" for mut. type
				writeSilentDataFields(writer);
			}
			writer.write("\n");
			dataLine = bufReader.readLine();
		}

		System.out.println("Total Number of Records Processed:  " + numRecordsProcessed);
//		for (String coords:  genomicCountMap.keySet()) {
//			Integer count = genomicCountMap.get(coords);
//			if (count > 1) {
//				System.out.println(coords + "\t" + (count-1));
//			}
//		}

		reader.close();
		writer.close();

		return this.oncotatorService.getErrorCount();
	}

	protected void outputFileNames(File inputMafFile, File outputMafFile) {
        System.out.println("Reading MAF From:  " + inputMafFile.getAbsolutePath());
        System.out.println("Writing new MAF To:  " + outputMafFile.getAbsolutePath());
    }

	protected void conditionallyAbort(int numRecordsProcessed) {
        if (MAX_NUM_RECORDS_TO_PROCESS > 0 && numRecordsProcessed > MAX_NUM_RECORDS_TO_PROCESS) {
            throw new IllegalStateException("Aborting at " + MAX_NUM_RECORDS_TO_PROCESS + " records");
        }
    }

	protected void writeEmptyDataFields(FileWriter writer) throws IOException {
        for (int i=0; i< DEFAULT_ONCO_HEADERS_COUNT; i++) {
            writer.write(TAB + "");
        }
    }

	protected void writeSilentDataFields(FileWriter writer) throws IOException
	{
		writer.write(TAB + SILENT_MUTATION);

		for (int i=1; i< DEFAULT_ONCO_HEADERS_COUNT; i++) {
			writer.write(TAB + "");
		}
	}

	protected String removeExistingOncoHeaders(String headerLine,
			Integer oncoHeaderCount)
	{
		String newHeaderLine = headerLine.trim();

		// header has oncotator columns, remove those column from the end
		// (assuming oncotator columns are always at the end)
		if (oncoHeaderCount > 0)
		{
			String[] parts = newHeaderLine.split(TAB);
			newHeaderLine = "";

			for (int i = 0; i < parts.length - oncoHeaderCount; i++)
			{
				newHeaderLine += parts[i];

				if (i != parts.length - oncoHeaderCount - 1)
				{
					newHeaderLine += TAB;
				}
			}
		}

		return newHeaderLine;
	}

	protected void writeHeaders(String headerLine,
		    FileWriter writer) throws IOException
    {
		// write the new header line (without oncotator columns)
	    writer.write(headerLine);

        // append oncotator headers to the end of the header list
        writer.write(TAB + "ONCOTATOR_VARIANT_CLASSIFICATION");
        writer.write(TAB + "ONCOTATOR_PROTEIN_CHANGE");
        writer.write(TAB + "ONCOTATOR_COSMIC_OVERLAPPING");
        writer.write(TAB + "ONCOTATOR_DBSNP_RS");
        writer.write(TAB + "ONCOTATOR_GENE_SYMBOL");
        
        writer.write("\n");
    }

	protected void conditionallyOncotateRecord(MafRecord mafRecord,
		    FileWriter writer) throws Exception
    {
        String ncbiBuild = mafRecord.getNcbiBuild();

	    if (!ncbiBuild.equals("37") &&
	        !ncbiBuild.equalsIgnoreCase("hg19") &&
	        !ncbiBuild.equalsIgnoreCase("GRCh37"))
	    {
            outputBuildNumErrorMessage(ncbiBuild);
            buildNumErrors++;

            if (buildNumErrors > 10) {
                abortDueToBuildNumErrors();
            }
        }
	    else
	    {
            oncotateRecord(mafRecord, writer);
        }
    }

	protected void abortDueToBuildNumErrors() {
        System.out.println("Too many records with wrong build #.  Aborting...");
        System.exit(1);
    }

	protected void outputBuildNumErrorMessage(String ncbiBuild) {
        System.out.println("Record uses NCBI Build:  " + ncbiBuild);
        System.out.println("-->  Oncotator only works with Build 37/hg19.");
    }

	protected void oncotateRecord(MafRecord mafRecord,
		    FileWriter writer) throws Exception
    {
        String chr = mafRecord.getChr();
        long start = mafRecord.getStartPosition();
        long end = mafRecord.getEndPosition();
        String refAllele = mafRecord.getReferenceAllele();
        String tumorAllele = determineTumorAllele(mafRecord, refAllele);
        if (tumorAllele != null) {
            String coords = createCoordinates(chr, start, end, refAllele, tumorAllele);
            System.out.println(coords);
//            if (genomicCountMap.containsKey(coords)) {
//                Integer count = genomicCountMap.get(coords);
//                genomicCountMap.put(coords, count+1);
//            } else {
//                genomicCountMap.put(coords, 1);
//            }
            OncotatorRecord oncotatorRecord =
                    oncotatorService.getOncotatorRecord(chr, start, end, refAllele,tumorAllele);
            writeOncotatorResults(writer, oncotatorRecord);
        } else {
            writeEmptyDataFields(writer);
        }
    }

	protected String determineTumorAllele(MafRecord mafRecord,
		    String refAllele)
    {
        String tumorAllel1 = mafRecord.getTumorSeqAllele1();
        String tumorAllel2 = mafRecord.getTumorSeqAllele2();
        String tumorAllele = null;
        if (!refAllele.equalsIgnoreCase(tumorAllel1)) {
            tumorAllele = tumorAllel1;
        } else if(!refAllele.equalsIgnoreCase(tumorAllel2)) {
            tumorAllele = tumorAllel2;
        }
        return tumorAllele;
    }

	protected void writeOncotatorResults(Writer writer,
		    OncotatorRecord oncotatorRecord) throws IOException
    {
        String proteinChange =
		        oncotatorRecord.getBestCanonicalTranscript().getProteinChange();
        String cosmicOverlapping =
		        oncotatorRecord.getCosmicOverlappingMutations();
        String dbSnpRs =
		        oncotatorRecord.getDbSnpRs();
        String variantClassification =
		        oncotatorRecord.getBestCanonicalTranscript().getVariantClassification();
        String geneSymbol =
		        oncotatorRecord.getBestCanonicalTranscript().getGene();

	    writer.write(TAB + outputField(variantClassification));
        writer.write(TAB + outputField(proteinChange));
        writer.write(TAB + outputField(cosmicOverlapping));
        writer.write(TAB + outputField(dbSnpRs));
        writer.write(TAB + outputField(geneSymbol));
    }

	protected String outputField(String field)
    {
        if (field == null) {
            return "";
        } else {
            return field;
        }
    }

	protected String createCoordinates(String chr, long start, long end,
                                     String refAllele, String tumorAllele)
    {
        return chr + "_" + start + "_" + end + "_" + refAllele 
                + "_" + tumorAllele;
    }
    
    /**
     * Adjusts the data line for consistency.
     * 
     * If the data is already oncotated removes oncotator columns
     * (assuming they are the last columns of the row)
     * to enable re-oncotation. Otherwise adjusts the data line to have columns
     * exactly the same as the number of column headers to prevent incorrect
     * oncotating.
     * 
     * @param dataLine	line to be adjusted
     * @param util		MAF util containing header information
     * @return			adjusted data line
     */
    protected String adjustDataLine(String dataLine, MafUtil util)
    {
    	String line = "";

	    // check if already oncotated
	    Integer actualOncoHeaderCount = calculateOncoHeaderCount(util);
    	
    	// file already oncotated
	    if (actualOncoHeaderCount > 0)
    	{
		    String[] parts = dataLine.split(TAB, -1);
        	
    		// remove oncotator data columns at the end of the row
    		// (to enable overwrite instead of appending new cols to the end)
    		for (int i = 0; i < parts.length - actualOncoHeaderCount; i++)
    		{
    			line += parts[i];
    			
    			if (i != parts.length - actualOncoHeaderCount - 1)
    			{
    				line += TAB;
    			}
    		}
    	}
    	// not oncotated, adjust tabs if necessary
    	else
	    {
		    line = util.adjustDataLine(dataLine);
	    }
    	
    	return line;
    }

	/**
	 * Calculates the actual number of Oncotator headers in the MAF file.
	 * Assuming there is at most DEFAULT_ONCO_HEADERS_COUNT headers.
	 *
	 * @param util  MafUtil instance having header indices
	 * @return      actual number of Oncotator headers
	 */
	protected Integer calculateOncoHeaderCount(MafUtil util)
	{
		Integer oncoHeaderCount = 0;

		if (util.getOncoVariantClassificationIndex() != -1)
		{
			oncoHeaderCount++;
		}

		if (util.getOncoProteinChangeIndex() != -1)
		{
			oncoHeaderCount++;
		}

		if (util.getOncoDbSnpRsIndex() != -1)
		{
			oncoHeaderCount++;
		}

		if (util.getOncoCosmicOverlappingIndex() != -1)
		{
			oncoHeaderCount++;
		}

		if (util.getOncoGeneSymbolIndex() != -1)
		{
			oncoHeaderCount++;
		}

		return oncoHeaderCount;
	}

    public static void main(String[] args)
    {
        String inputMaf = null;
	    String outputMaf = null;
	    boolean noCache = false;

	    if (args.length < 2)
        {
            System.out.println("command line usage: oncotateMaf.sh [-nocache] <input_maf_file> <output_maf_file>");
            System.exit(1);
        }
	    else
	    {
		    if (args[0].equals("-nocache"))
		    {
			    noCache = true;
			    inputMaf = args[1];
			    outputMaf = args[2];
		    }
		    else
		    {
			    inputMaf = args[0];
			    outputMaf = args[1];
		    }
	    }

        Date start = new Date();
	    int oncoResult = 0;

        try {
            OncotateTool tool = new OncotateTool();
	        oncoResult = tool.oncotateMaf(new File(inputMaf),
	                                      new File(outputMaf),
	                                      noCache);
        }
        catch (Exception e)
        {
            System.out.println("Error occurred:  " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            Date end = new Date();
            double timeElapsed = (end.getTime() - start.getTime()) / 1000.0;
            System.out.println("Total time:  " + timeElapsed + " seconds.");

	        // check errors at the end
	        if (oncoResult != 0)
	        {
		        // TODO produce different error codes, for different types of errors?
		        System.out.println("Process completed with " + oncoResult + " error(s).");
		        System.exit(2);
	        }
        }
    }
}