package org.mskcc.cbio.portal.servlet;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.mskcc.cbio.cgds.dao.*;
import org.mskcc.cbio.cgds.model.*;
import org.mskcc.cbio.cgds.web_api.GetProfileData;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.cbio.portal.util.*;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.plaf.basic.BasicBorders;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class GeneAlterationsJSON extends HttpServlet {
    private ServletXssUtil servletXssUtil;
    public static final String SELECTED_CANCER_STUDY = "selected_cancer_type";
    public static final String GENE_LIST = "gene_list";
    public static final String ACTION_NAME = "Action";
    // todo: can these strings be referenced directly from QueryBuilder itself?

    public static final String HUGO_GENE_SYMBOL = "hugoGeneSymbol";
    public static final String SAMPLE = "sample";
    public static final String UNALTERED_SAMPLE = "unaltered_sample";
    public static final String ALTERATION = "alteration";
    public static final String PERCENT_ALTERED = "percent_altered";
    public static final String MUTATION = "mutation";


    private static Log log = LogFactory.getLog(GisticJSON.class);

    /**
     * Initializes the servlet.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {
        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * todo: this is code duplication!
     * Format percentage.
     *
     * <p/>
     * if value == 0 return "--"
     * case value
     * 0: return "--"
     * 0<value<=0.01: return "<1%"
     * 1<value: return "<value>%"
     *
     * @param value double
     *
     * @return String
     */
    public static String alterationValueToString(double value) {

        // in oncoPrint show 0 percent as 0%, not --
        if (0.0 < value && value <= 0.01) {
            return "<1%";
        }

        // if( 1.0 < value ){
        Formatter f = new Formatter();
        f.format("%.0f", value * 100.0);
        return f.out().toString() + "%";
    }

    /**
     * converts a string to a bitmask,
     * e.g. "CNA_AMPLIFIED | CNA_GAINED" -> (1<<0) + (1<<1)
     *
     * @param alterationSettings
     * @return
     */
    public static int alterationSettings_toBits(String alterationSettings) {
        int bitmask = 0;
        
        String[] split = alterationSettings.split("\\|");

        for (String s : split) {
            s = s.trim();
            bitmask = s.equals("CNA_AMPLIFIED") ? bitmask              + (1 << 0) : bitmask;
            bitmask = s.equals("CNA_GAINED") ? bitmask                 + (1 << 1) : bitmask;
            bitmask = s.equals("CNA_DIPLOID") ? bitmask                + (1 << 2) : bitmask;
            bitmask = s.equals("CNA_HEMIZYGOUSLYDELETED") ? bitmask    + (1 << 3) : bitmask;
            bitmask = s.equals("CNA_HOMODELETED") ? bitmask            + (1 << 4) : bitmask;
            bitmask = s.equals("CNA_NONE") ? bitmask                   + (1 << 5) : bitmask;
            bitmask = s.equals("MRNA_UPREGULATED") ? bitmask           + (1 << 6) : bitmask;
            bitmask = s.equals("MRNA_DOWNREGULATED") ? bitmask         + (1 << 7) : bitmask;
            bitmask = s.equals("MRNA_NOTSHOWN") ? bitmask              + (1 << 8) : bitmask;
            bitmask = s.equals("RPPA_UPREGULATED") ? bitmask           + (1 << 9) : bitmask;
            bitmask = s.equals("RPPA_NORMAL") ? bitmask                + (1 << 10) : bitmask;
            bitmask = s.equals("RPPA_DOWNREGULATED") ? bitmask         + (1 << 11) : bitmask;
            bitmask = s.equals("RPPA_NOTSHOWN") ? bitmask              + (1 << 12) : bitmask;
            bitmask = s.equals("MUTATED") ? bitmask                    + (1 << 13) : bitmask;
            bitmask = s.equals("NORMAL") ? bitmask                     + (1 << 14) : bitmask;
        }
        
        if (bitmask == 0) {
            log.info("GeneAlterationsJSON "
                    + "CNA bitmask was never set: "
                    + Arrays.toString(split));
        }

        return  bitmask;
    }


    /**
     * Maps the matrix to a JSONArray of alterations
     * @param geneticEvents matrix M[case][gene]
     * @return
     */
    public JSONArray mapGeneticEventMatrix(GeneticEvent[][] geneticEvents, ProfileDataSummary dataSummary) throws ServletException {
        JSONArray array = new JSONArray();
        // list of json objects

        // todo: this is code duplication!
        // from MakeOncoPrint
        for (int i = 0; i < geneticEvents.length; i++) {
            GeneticEvent rowEvent = geneticEvents[i][0];
            String gene = rowEvent.getGene().toUpperCase();
            String alterationValue =
                    alterationValueToString(dataSummary.getPercentCasesWhereGeneIsAltered(rowEvent.getGene()));

            Map geneAlts = new HashMap();
            // json object e.g. { hugoGeneSymbol: "EGFR", percentAltered: "54%", 'alterations': [ ... ] }

            geneAlts.put(HUGO_GENE_SYMBOL, gene);
            geneAlts.put(PERCENT_ALTERED, alterationValue);

            JSONArray alterations = new JSONArray();
            // list of alterations
            for (int j = 0; j < geneticEvents[0].length; j++) {

                Map alteration = new HashMap();
                // json object, e.g. { 'sample' : "TCGA-A1-A0SD", 'unaltered_sample' : true,
                // 'alteration' : CNA_NONE | MRNA_NOTSHOWN | NORMAL | RPPA_NOTSHOWN },

                GeneticEvent event = geneticEvents[i][j];
                Boolean sampleIsUnaltered = MakeOncoPrint.isSampleUnaltered(j, geneticEvents);
                String alterationSettings = MakeOncoPrint.getGeneticEventAsString(event);
                int alterationSettings_bits = alterationSettings_toBits(alterationSettings);

                alteration.put(SAMPLE, event.caseCaseId());
                alteration.put(UNALTERED_SAMPLE, sampleIsUnaltered);
                alteration.put(ALTERATION, alterationSettings_bits);

                if (event.isMutated()) {
                    JSONArray mutationDetails = new JSONArray();
                    mutationDetails.add(event.getMutationType());
                    alteration.put(MUTATION, mutationDetails);
                    // I don't know why this is a singleton list of a String with commans in it, e.g.
                    // ["K3326*,N1322del"]
                }

                alterations.add(alteration);
            }

            geneAlts.put("alterations", alterations);

            array.add(geneAlts);
        }

        return array;
    }

    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String cancer_study_id = request.getParameter("cancer_study_id");

        String _geneList = request.getParameter("genes");
        // list of genes separated by a space

        String caseIds = request.getParameter("cases");
        // list of cases separated by a space.  This is so
        // that you can query by an arbitrary set of cases
        // separated by a space

        String _geneticProfileIds = request.getParameter("geneticProfileIds");
        // list of geneticProfileIds separated by a space
        // e.g. gbm_mutations, gbm_cna_consensus

        HashSet<String> geneticProfileIdSet = new HashSet<String>(Arrays.asList(_geneticProfileIds.split(" ")));
        
        // map geneticProfileIds -> geneticProfiles
        Iterator<String> gpSetIterator =  geneticProfileIdSet.iterator();
        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        ArrayList<GeneticProfile> profileList = new ArrayList<GeneticProfile>();
        if (gpSetIterator.hasNext()) {
            String gp_str = gpSetIterator.next();
            try {
                GeneticProfile gp = daoGeneticProfile.getGeneticProfileByStableId(gp_str);
                profileList.add(gp);
                // pointer to gp is local, but gets added to profileList which is outside
            } catch (DaoException e) {
                throw new ServletException(e);
            }
        }

        // todo: how should this *not* be hard coded?
        double zScoreThreshold = ZScoreUtil.Z_SCORE_THRESHOLD_DEFAULT;
        double rppaScoreThreshold = ZScoreUtil.RPPA_SCORE_THRESHOLD_DEFAULT;

        // ... do a bunch of work to get the matrix, basically copying out of QueryBuilder ...
        // todo: this is code duplication!
        ParserOutput theOncoPrintSpecParserOutput =
                OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(_geneList,
                        geneticProfileIdSet, profileList, zScoreThreshold, rppaScoreThreshold);

        ArrayList<String> listOfGenes =
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();
        String[] listOfGeneNames = new String[listOfGenes.size()];
        listOfGeneNames = listOfGenes.toArray(listOfGeneNames);

        ArrayList<ProfileData> profileDataList = new ArrayList<ProfileData>();
        Iterator<String> profileIterator = geneticProfileIdSet.iterator();

        XDebug xdebug = new XDebug(request);
        while (profileIterator.hasNext()) {
            String profileId = profileIterator.next();
            GeneticProfile profile = GeneticProfileUtil.getProfile(profileId, profileList);
            if( null == profile ){
                continue;
            }

            xdebug.logMsg(this, "Getting data for:  " + profile.getProfileName());

            ArrayList<String> geneList = new ArrayList<String>(Arrays.asList(_geneList.split("\\s+")));
            GetProfileData remoteCall = null;
            try {
                remoteCall = new GetProfileData(profile, geneList, caseIds);
            } catch (DaoException e) {
                throw new ServletException(e);
            }
            ProfileData pData = remoteCall.getProfileData();
            if(pData == null){
                System.err.println("pData == null");
            } else {
                if (pData.getGeneList() == null ) {
                    System.err.println("pData.getValidGeneList() == null");
                } if (pData.getCaseIdList().size() == 0) {
                    System.err.println("pData.length == 0");
                }
            }
            if (pData != null) {
                xdebug.logMsg(this, "Got number of genes:  " + pData.getGeneList().size());
                xdebug.logMsg(this, "Got number of cases:  " + pData.getCaseIdList().size());
            }
            xdebug.logMsg(this, "Number of warnings received:  " + remoteCall.getWarnings().size());
            profileDataList.add(pData);
        }

        xdebug.logMsg(this, "Merging Profile Data");
        ProfileMerger merger = new ProfileMerger(profileDataList);
        ProfileData mergedProfile = merger.getMergedProfile();

        ProfileDataSummary dataSummary = new ProfileDataSummary(mergedProfile,
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold, rppaScoreThreshold);

        GeneticEvent geneticEvents[][] = ConvertProfileDataToGeneticEvents.convert
			(dataSummary, listOfGeneNames,
			 theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold, rppaScoreThreshold);

        // out.write the matrix

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        JSONArray geneticEventsJSON = mapGeneticEventMatrix(geneticEvents, dataSummary);

        // get outa here!
        JSONArray.writeJSONString(geneticEventsJSON, out);
    }

    /**
     * Just in case the request changes from GET to POST
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        doPost(request, response);
    }
}
