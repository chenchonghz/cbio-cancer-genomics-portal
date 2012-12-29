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

// package
package org.mskcc.cbio.importer.model;

// imports
import java.io.File;

/**
 * Class which contains cancer study metadata.
 */
public class CancerStudyMetadata {
	
    // delimiter between tumor type and center (used for find the path)
	public static final String CANCER_STUDY_DELIMITER = "/"; 

	// this is value in worsheet-matrix cell if cancer study is in a desired portal
	public static final String CANCER_STUDY_IN_PORTAL_INDICATOR = "x";

	// file extension of metadata file
	public static final String CANCER_STUDY_METADATA_FILE_EXT = ".txt";

	// cancer study identifier delimiter (used in metadata files)
	private static final String CANCER_STUDY_IDENTIFIER_DELIMITER = "_";

	// this is tag to replace in description
	public static final String NUM_CASES_TAG = "<NUM_CASES>";

	// bean properties
	private String tumorType;
	private TumorTypeMetadata tumorTypeMetadata;
	private String description;
	private String center;
	private String lab;

    /**
     * Create a CancerStudyMetadata instance with properties in given array.
	 * ITs assumed order of properties is that from google worksheet.
	 * cancerStudyPath is of the form brca/tcga/pub that you would find 
	 * on the google spreadsheet cancer_studies worksheet.
	 *
	 * All portal columns are ignored (anything > 1)
     *
	 * @param properties String[]
     */
    public CancerStudyMetadata(String[] properties) {

		if (properties.length != 4) {
            throw new IllegalArgumentException("corrupt properties array passed to contructor");
		}

		String[] parts = properties[0].trim().split(CANCER_STUDY_DELIMITER);
		if (parts.length < 2) {
			throw new IllegalArgumentException("cancerStudyPath is missing tumor type and or center");
		}

		// set tumor type & center
		this.tumorType = parts[0];
		this.center = parts[1];

		// add remaining designations to lab
		this.lab = "";
		for (int lc = 2; lc <= parts.length-1; lc++) {
			this.lab = this.lab + parts[lc] + File.separator;
		}
		// knock off trailing file separator
		this.lab = (this.lab.length() > 0) ? this.lab.substring(0, this.lab.length()-1) : this.lab;
		this.description = properties[1].trim();
	}

	public String getTumorType() { return tumorType; }
	public TumorTypeMetadata getTumorTypeMetadata() { return tumorTypeMetadata; }
	public void setTumorTypeMetadata(TumorTypeMetadata tumorTypeMetadata) { this.tumorTypeMetadata = tumorTypeMetadata; }

	public String getCenter() { return center; }
	public String getLab() { return lab; }
	public String getStudyPath() {
		String suffix = (lab.length() > 0) ? (File.separator + lab) : "";
		return (tumorType + File.separator + center + suffix);
	}
	public String getDescription() { return description; }

	public String toString() {
		return (tumorType + CANCER_STUDY_IDENTIFIER_DELIMITER +
				center + lab.replaceAll(CANCER_STUDY_DELIMITER, CANCER_STUDY_IDENTIFIER_DELIMITER));
	}
}
