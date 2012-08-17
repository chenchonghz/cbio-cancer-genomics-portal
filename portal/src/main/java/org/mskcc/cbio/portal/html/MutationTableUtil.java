package org.mskcc.cbio.portal.html;

import org.mskcc.cbio.portal.html.special_gene.SpecialGeneFactory;
import org.mskcc.cbio.portal.html.special_gene.SpecialGene;
import org.mskcc.cbio.portal.util.SequenceCenterUtil;
import org.mskcc.cbio.cgds.model.ExtendedMutation;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Utility Class for Creating the Mutation Table.
 *
 * @author Ethan Cerami
 */
public class MutationTableUtil
{
    private ArrayList<String> headerList = new ArrayList<String>();
	private SpecialGene specialGene;

	// Mappings between data values and html values
	private HashMap<String, String[]> mutationStatusMap;
	private HashMap<String, String[]> validationStatusMap;
	private HashMap<String, String[]> mutationTypeMap;

    // Validation Status Constants
    private static final String DISPLAY_VALID = "valid";
	private static final String CSS_VALID = "valid";
	private static final String VALID = "valid";

	// Mutation Status Constants
    private static final String CSS_SOMATIC = "somatic";
    private static final String CSS_GERMLINE = "germline";
	private static final String SOMATIC = "somatic";
	private static final String GERMLINE = "germline";
	private static final String DISPLAY_SOMATIC = "S";
	private static final String DISPLAY_GERMLINE = "G";

	// Mutation Type Constants
	private static final String DISPLAY_MISSENSE = "Missense";
	private static final String DISPLAY_NONSENSE = "Nonsense";
	private static final String DISPLAY_FS_DEL = "FS del";
	private static final String DISPLAY_FS_INS = "FS ins";
	private static final String DISPLAY_IF_DEL = "IF ins";
	private static final String DISPLAY_IF_INS = "IF del";
	private static final String DISPLAY_SPLICE = "Splice";
	private static final String MISSENSE = "missense mutation";
	private static final String NONSENSE = "nonsense mutation";
	private static final String FS_DEL = "frame shift del";
	private static final String FS_INS = "frame shift ins";
	private static final String IF_DEL = "in frame ins";
	private static final String IF_INS = "in frame del";
	private static final String SPLICE = "splice site";
	private static final String CSS_MISSENSE = "missense_mutation";
	private static final String CSS_OTHER_MUT = "other_mutation";

    public MutationTableUtil(String geneSymbol)
    {
        specialGene = SpecialGeneFactory.getInstance(geneSymbol);
        this.mutationStatusMap = this.initMutationStatusMap();
	    this.validationStatusMap = this.initValidationStatusMap();
	    this.mutationTypeMap = this.initMutationTypeMap();
	    this.initHeaders();
    }

    public ArrayList<String> getTableHeaders() {
        return headerList;
    }

    public String getTableFooterMessage() {
        if (specialGene != null) {
            return specialGene.getFooter();
        } else {
            return HtmlUtil.EMPTY_STRING;
        }
    }

    public String getTableHeaderHtml() {
        return HtmlUtil.createTableHeaderRow(headerList);
    }

    public ArrayList<String> getDataFields(ExtendedMutation mutation) {
        ArrayList <String> dataFieldList = new ArrayList<String>();

        //  Case ID.
        dataFieldList.add(HtmlUtil.getSafeWebValue(mutation.getCaseId()));

        //  Basic Mutation Info.
        dataFieldList.add(HtmlUtil.getSafeWebValue(getMutationStatus(mutation)));
        dataFieldList.add(HtmlUtil.getSafeWebValue(getMutationType(mutation)));
        dataFieldList.add(HtmlUtil.getSafeWebValue(getValidationStatus(mutation)));
        dataFieldList.add(HtmlUtil.getSafeWebValue(getSequencingCenter(mutation)));
        dataFieldList.add(HtmlUtil.getSafeWebValue(mutation.getProteinChange()));

        //  OMA Links
        MutationAssessorHtmlUtil omaUtil = new MutationAssessorHtmlUtil(mutation);
        dataFieldList.add(omaUtil.getFunctionalImpactLink());
        dataFieldList.add(omaUtil.getMultipleSequenceAlignmentLink());
        dataFieldList.add(omaUtil.getPdbStructureLink());

        //  Fields for "Special" Genes
        if (specialGene != null) {
            dataFieldList.addAll(specialGene.getDataFields(mutation));
        }
        return dataFieldList;
    }

    public String getDataRowHtml(ExtendedMutation mutation) {
        return HtmlUtil.createTableRow(getDataFields(mutation));
    }

    private String getSequencingCenter(ExtendedMutation mutation) {
        return SequenceCenterUtil.getSequencingCenterAbbrev
                (mutation.getSequencingCenter());
    }

	/**
	 * Extracts the validation status information for the given mutation,
	 * and generates the html element for that validation status
	 *
	 * @param mutation  mutation instance
	 * @return          returns html representation for the validation status
	 */
    private String getValidationStatus(ExtendedMutation mutation)
    {
	    String[] values = this.validationStatusMap.get(
			    mutation.getValidationStatus().toLowerCase());

	    // if there is a value pair (display, css) for the current validation status,
	    // use those values
	    if (values != null)
	    {
		    return HtmlUtil.createTextWithinSpan(values[0], values[1]);
	    }
	    // else, directly use the validation status value itself
	    else
	    {
		    return mutation.getValidationStatus();
	    }
    }

	/**
	 * Extracts the mutation status information for the given mutation,
	 * and generates the html element for that mutation status
	 *
	 * @param mutation  mutation instance
	 * @return          returns html representation for the mutation status
	 */
    private String getMutationStatus(ExtendedMutation mutation)
    {
	    String[] values = this.mutationStatusMap.get(
			    mutation.getMutationStatus().toLowerCase());

	    // if there is a value pair (display, css) for the current mutation status,
	    // use those values
        if (values != null)
        {
	        return HtmlUtil.createTextWithinSpan(values[0], values[1]);
        }
        // else, directly use the mutation status value itself
        else
        {
            return mutation.getMutationStatus();
        }
    }

	/**
	 * Extracts the mutation type information for the given mutation,
	 * and generates the html element for that mutation type
	 *
	 * @param mutation  mutation instance
	 * @return          returns html representation for the mutation type
	 */
	private String getMutationType(ExtendedMutation mutation)
	{
		// make it lowercase and remove any under dashes to match a possible key
		String type = mutation.getMutationType().toLowerCase().replaceAll("_", " ");

		String[] values = this.mutationTypeMap.get(type);

		// if there is a value pair (display, css) for the current mutation type,
		// use those values
		if (values != null)
		{
			return HtmlUtil.createTextWithinSpan(values[0], values[1]);
		}
		// else, directly use the mutation type value itself
		else
		{
			return mutation.getMutationType();
		}
	}

    private void initHeaders()
    {
        headerList.add("Case ID");
        headerList.add("Mutation Status");
        headerList.add("Mutation Type");
        headerList.add("Validation Status");
        headerList.add("Sequencing Center");
        headerList.add("AA Change");
        headerList.add("Predicted Impact**");
        headerList.add("Alignment");
        headerList.add("Structure");

        //  Add Any Gene-Specfic Headers
        if (specialGene != null) {
            headerList.addAll(specialGene.getDataFieldHeaders());
        }
    }

	/**
	 * Creates a mapping between the mutation status (data) values and
	 * view values. The first element of an array corresponding to a
	 * data value is the display text (html), and the second one
	 * is style (css).
	 *
	 * @return  a mapping for possible values of mutation status
	 */
	private HashMap<String, String[]> initMutationStatusMap()
	{
		HashMap<String, String[]> map = new HashMap<String, String[]>();

		String[] somatic = {DISPLAY_SOMATIC, CSS_SOMATIC};
		String[] germline = {DISPLAY_GERMLINE, CSS_GERMLINE};

		map.put(SOMATIC, somatic);
		map.put(GERMLINE, germline);

		return map;
	}

	/**
	 * Creates a mapping between the validation status (data) values and
	 * view values. The first element of an array corresponding to a
	 * data value is the display text (html), and the second one
	 * is style (css).
	 *
	 * @return  a mapping for possible values of validation status
	 */
	private HashMap<String, String[]> initValidationStatusMap()
	{
		HashMap<String, String[]> map = new HashMap<String, String[]>();

		String[] valid = {DISPLAY_VALID, CSS_VALID};

		map.put(VALID, valid);

		return map;
	}

	/**
	 * Creates a mapping between the mutation type (data) values and
	 * view values. The first element of an array corresponding to a
	 * data value is the display text (html), and the second one
	 * is style (css).
	 *
	 * @return  a mapping for possible values of validation status
	 */
	private HashMap<String, String[]> initMutationTypeMap()
	{
		HashMap<String, String[]> map = new HashMap<String, String[]>();

		String[] missense = {DISPLAY_MISSENSE, CSS_MISSENSE};
		String[] nonsense = {DISPLAY_NONSENSE, CSS_OTHER_MUT};
		String[] fsDel = {DISPLAY_FS_DEL, CSS_OTHER_MUT};
		String[] fsIns = {DISPLAY_FS_INS, CSS_OTHER_MUT};
		String[] ifDel = {DISPLAY_IF_DEL, CSS_OTHER_MUT};
		String[] ifIns = {DISPLAY_IF_INS, CSS_OTHER_MUT};
		String[] splice = {DISPLAY_SPLICE, CSS_OTHER_MUT};

		map.put(MISSENSE, missense);
		map.put(NONSENSE, nonsense);
		map.put(FS_DEL, fsDel);
		map.put(FS_INS, fsIns);
		map.put(IF_DEL, ifDel);
		map.put(IF_INS, ifIns);
		map.put(SPLICE, splice);

		return map;
	}


}