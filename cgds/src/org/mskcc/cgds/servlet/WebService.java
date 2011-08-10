package org.mskcc.cgds.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoCase;
import org.mskcc.cgds.dao.DaoCaseList;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneticProfile;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.util.AccessControl;
import org.mskcc.cgds.util.DatabaseProperties;
import org.mskcc.cgds.web_api.GetCaseLists;
import org.mskcc.cgds.web_api.GetClinicalData;
import org.mskcc.cgds.web_api.GetGeneticProfiles;
import org.mskcc.cgds.web_api.GetMutationData;
import org.mskcc.cgds.web_api.GetMutationFrequencies;
import org.mskcc.cgds.web_api.GetNetwork;
import org.mskcc.cgds.web_api.GetMutSig;
import org.mskcc.cgds.web_api.GetProfileData;
import org.mskcc.cgds.web_api.GetProteinArrayData;
import org.mskcc.cgds.web_api.GetTypesOfCancer;
import org.mskcc.cgds.web_api.ProtocolException;
import org.mskcc.cgds.web_api.WebApiUtil;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Core Web Service.
 *
 * @author Ethan Cerami.
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class WebService extends HttpServlet {

    public static final String CANCER_STUDY_ID = "cancer_study_id";
    public static final String CANCER_TYPE_ID = "cancer_type_id";
    public static final String GENETIC_PROFILE_ID = "genetic_profile_id";
    public static final String GENE_LIST = "gene_list";
    public static final String CMD = "cmd";
    public static final String Q_VALUE_THRESHOLD = "q_value_threshold";
    public static final String GENE_SYMBOL = "gene_symbol";
    public static final String ENTREZ_GENE_ID = "entrez_gene_id";
    public static final String CASE_LIST = "case_list";
    public static final String CASE_SET_ID = "case_set_id";
    public static final String SUPPRESS_MONDRIAN_HEADER = "suppress_mondrian_header";
    public static final String EMAIL_ADDRESS = "email_address";
    public static final String SECRET_KEY = "secret_key";
    public static final String PROTEIN_ARRAY_TYPE = "protein_array_type";
    public static final String PROTEIN_ARRAY_ID = "protein_array_id";

	// ref to access control
	private AccessControl accessControl;

    /**
     * Shutdown the Servlet.
     */
    public void destroy() {
        super.destroy();
    }

    /**
     * Initializes Servlet with parameters in web.xml file.
     *
     * @throws javax.servlet.ServletException Servlet Initialization Error.
     */
    public void init() throws ServletException {
        super.init();
        System.out.println("Starting up the Cancer Genomics Data Server...");
        System.out.println("Reading in init parameters from web.xml");
        DatabaseProperties dbProperties = DatabaseProperties.getInstance();
        ServletConfig config = this.getServletConfig();
        String dbHost = config.getInitParameter("db_host");
        String dbUser = config.getInitParameter("db_user");
        String dbPassword = config.getInitParameter("db_password");
        String dbName = config.getInitParameter("db_name");
        System.out.println("Starting CGDS Server");
        dbProperties.setDbName(dbName);
        dbProperties.setDbHost(dbHost);
        dbProperties.setDbUser(dbUser);
        dbProperties.setDbPassword(dbPassword);
        verifyDbConnection();

		// setup our context and init some beans
		ApplicationContext context =
			new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");
		accessControl = (AccessControl)context.getBean("accessControl");
    }

    /**
     * Handles GET Requests.
     *
     * @param httpServletRequest  HttpServlet Request.
     * @param httpServletResponse HttpServlet Response.
     * @throws ServletException Servlet Error.
     * @throws IOException      IO Error.
     */
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {
        processClient(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles POST Requests.
     *
     * @param httpServletRequest  HttpServlet Request.
     * @param httpServletResponse HttpServlet Response.
     * @throws ServletException Servlet Error.
     * @throws IOException      IO Error.
     */
    protected void doPost(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse) throws ServletException, IOException {
        processClient(httpServletRequest, httpServletResponse);
    }

    /**
     * Processes all Client Requests.
     *
     * @param httpServletRequest  HttpServlet Request.
     * @param httpServletResponse HttpServlet Response.
     * @throws IOException IO Error.
     */
    public void processClient(HttpServletRequest httpServletRequest,
                              HttpServletResponse httpServletResponse) throws IOException {
        PrintWriter writer = httpServletResponse.getWriter();
        Date startTime = new Date();
        String cmd = httpServletRequest.getParameter(CMD);

        try {
            httpServletResponse.setContentType("text/plain");
            writer.print(WebApiUtil.WEP_API_HEADER);

            // Branch, based on command.
            if (null == cmd) {
                outputMissingParameterError(writer, CMD);
                return;
            }

            // check command
            if (!goodCommand(writer, cmd)) {
                return;
            }

            if (cmd.equals("getTypesOfCancer")) {
                // getTypesOfCancer requires no access control
                getTypesOfCancer(writer);
                return;
            }
            if (cmd.equals("getNetwork")) {
                // getNetwork doesn't access any data; so no access control needed
                getNetwork(httpServletRequest, writer);
                return;
            }

			if (cmd.equals("getProteinArrayInfo")) {
				getProteinArrayInfo(httpServletRequest, writer);
				return;
			}
			if (cmd.equals("getProteinArrayData")) {
				getProteinArrayData(httpServletRequest, writer);
				return;
			}

            //  We support the new getCancerStudies plus the deprecated getCancerTypes command
            if (cmd.equals("getCancerStudies") || cmd.equals("getCancerTypes")) {

                // getCancerStudies requires special access control
                // identify every study accessible to the user
                getCancerStudies(httpServletRequest, writer);
                return;
            }


            // TODO: CASES: REMOVE?
            // no cancer_study_id or no case_set_id or genetic_profile_id
            if (null == getCancerStudyId(httpServletRequest) &&
                    null == httpServletRequest.getParameter(WebService.CASE_SET_ID) &&
                    null == httpServletRequest.getParameter(WebService.GENETIC_PROFILE_ID) &&
                    null == httpServletRequest.getParameter(WebService.CASE_LIST)) {
                outputError(writer, "No cancer study (cancer_study_id), or genetic profile (genetic_profile_id) " +
                        "or case list or (case_list) case set (case_set_id) provided by request. " +
                        "Please reformulate request.");
                return;
            }

            HashSet<String> cancerStudyIDs = getCancerStudyIDs(httpServletRequest);
            if (null == cancerStudyIDs) {
                outputError(writer, "Problem when identifying a cancer study for the request.");
                return;
            }
            // TODO: if cancerStudyID == CancerStudy.NO_SUCH_STUDY report an error with more info
            for (String cancerStudyID : cancerStudyIDs) {
                if (!DaoCancerStudy.doesCancerStudyExistByStableId(cancerStudyID)) {
                    outputError(writer, "The cancer study identified by the request (" + cancerStudyID +
                            ") is not in the dbms. Please reformulate request.");
                    return;
                }
            }

            // check access control
            for (String cancerStudyID : cancerStudyIDs) {
                CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyID);
                if (!checkAccess(httpServletRequest, writer, cancerStudyID)) {

                    // access denied
                    outputError(writer, "User cannot access the cancer study called '" + cancerStudy.getName()
                            + "'. Please provide credentials to access private data.");
                    return;
                }
            }

            // IMPORTANT IMPORTANT IMPORTANT IMPORTANT
            // all web api commands that access private data (except getcancerstudies, which is a special case)
            // must be placed AFTER this comment, so that the user's right to access the data is verified
            if (cmd.equals("getGeneticProfiles")) {
                // PROVIDES CANCER_STUDY_ID
                getGeneticProfiles(httpServletRequest, writer);
            } else if (cmd.equals("getProfileData")) {
                // PROVIDES genetic_profile_id
                getProfileData(httpServletRequest, writer);
            } else if (cmd.equals("getCaseLists")) {
                // PROVIDES CANCER_STUDY_ID
                getCaseLists(httpServletRequest, writer);
            } else if (cmd.equals("getClinicalData")) {
                // PROVIDES case_set_id
                getClinicalData(httpServletRequest, writer);
            } else if (cmd.equals("getMutationData")) {
                // PROVIDES genetic_profile_id
                getMutationData(httpServletRequest, writer);
            } else if (cmd.equals("getMutationFrequency")) {
                // PROVIDES CANCER_STUDY_ID
                getMutationFrequency(httpServletRequest, writer);
            } else if (cmd.equals("getMutSig")) {
                //Provides MutSig Data
                getMutSig(httpServletRequest, writer);
            } else {
                throw new ProtocolException("Unrecognized command: " + cmd);
            }
        } catch (DaoException e) {
            e.printStackTrace();
            outputError(writer, "internal error:  " + e.getMessage());
        } catch (ProtocolException e) {
            e.printStackTrace();
            outputError(writer, e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            outputError(writer, e.toString());
        } finally {
            writer.flush();
            writer.close();
            Date stopTime = new Date();
            long timeElapsed = stopTime.getTime() - startTime.getTime();
        }
    }

    /**
     * Gets the Network of Interest.
     *
     * @param httpServletRequest HttpServletRequest Object.
     * @param writer             Print Writer Object.
     * @throws DaoException      Database Exception.
     * @throws ProtocolException Protocol Exception.
     */
    private void getNetwork(HttpServletRequest httpServletRequest,
                            PrintWriter writer) throws DaoException, ProtocolException {
        String geneList = httpServletRequest.getParameter(GENE_LIST);
        if (geneList == null || geneList.length() == 0) {
            throw new ProtocolException("Missing Parameter:  " + GENE_LIST);
        }
        ArrayList<String> targetGeneList = getGeneList(httpServletRequest);
        String out = GetNetwork.getNetwork(targetGeneList);
        writer.print(out);
    }
    
    private void getProteinArrayInfo(HttpServletRequest httpServletRequest, 
            PrintWriter writer) throws DaoException, ProtocolException {
        String geneList = httpServletRequest.getParameter(GENE_LIST);
        ArrayList <String> targetGeneList;
        if (geneList == null || geneList.length() == 0) {
            targetGeneList = null;
        }else {
            targetGeneList = getGeneList (httpServletRequest);
        }
        
        String type = httpServletRequest.getParameter(PROTEIN_ARRAY_TYPE);
        writer.print(GetProteinArrayData.getProteinArrayInfo(targetGeneList,type));
    }
    
    private void getProteinArrayData(HttpServletRequest httpServletRequest, 
            PrintWriter writer) throws DaoException, ProtocolException {
        String arrayId = httpServletRequest.getParameter(PROTEIN_ARRAY_ID);
        if (arrayId == null || arrayId.length() == 0) {
            throw new ProtocolException ("Missing Parameter:  " + PROTEIN_ARRAY_ID);
        }
        ArrayList<String> targetCaseIds = null;
        if (null != httpServletRequest.getParameter(CASE_LIST)
                || null != httpServletRequest.getParameter(CASE_SET_ID))
            targetCaseIds = getCaseList(httpServletRequest);
        writer.print(GetProteinArrayData.getProteinArrayData(arrayId, targetCaseIds));
    }

    private void getTypesOfCancer(PrintWriter writer) throws DaoException, ProtocolException {

        String out = GetTypesOfCancer.getTypesOfCancer();
        writer.print(out);
    }

    private void getCancerStudies(HttpServletRequest httpServletRequest, PrintWriter writer) throws DaoException,
            ProtocolException {
        //String out = accessControl.getCancerStudies(httpServletRequest.getParameter(EMAIL_ADDRESS),
		//											httpServletRequest.getParameter(SECRET_KEY));
		String out = accessControl.getCancerStudies();
        writer.print(out);
    }

    private boolean checkAccess(HttpServletRequest httpServletRequest, PrintWriter writer,
                                String stableStudyId) throws DaoException {
        //return accessControl.checkAccess(httpServletRequest.getParameter(EMAIL_ADDRESS),
		//								 httpServletRequest.getParameter(SECRET_KEY), stableStudyId);
		return accessControl.checkAccess(stableStudyId);
    }

    private void getMutationFrequency(HttpServletRequest httpServletRequest, PrintWriter writer)
            throws DaoException, ProtocolException {

        String cancerStudyId = getCancerStudyId(httpServletRequest);
        if (cancerStudyId == null) {
            outputMissingParameterError(writer, CANCER_STUDY_ID);
        } else {
            String out = GetMutationFrequencies.getMutationFrequencies(Integer.parseInt(cancerStudyId),
                    httpServletRequest);
            writer.print(out);
        }
    }

    private void getGeneticProfiles(HttpServletRequest httpServletRequest, PrintWriter writer)
            throws DaoException {

        String cancerStudyStableId = getCancerStudyId(httpServletRequest);
        if (cancerStudyStableId == null) {
            outputMissingParameterError(writer, CANCER_STUDY_ID);
        } else {
            String out = GetGeneticProfiles.getGeneticProfiles(cancerStudyStableId);
            writer.print(out);
        }
    }

    private void getCaseLists(HttpServletRequest httpServletRequest, PrintWriter writer)
            throws DaoException {

        String cancerStudyStableId = getCancerStudyId(httpServletRequest);
        if (cancerStudyStableId == null) {
            outputMissingParameterError(writer, CANCER_STUDY_ID);
        } else {
            String out = GetCaseLists.getCaseLists(cancerStudyStableId);
            writer.print(out);
        }
    }

    private void outputError(PrintWriter writer, String msg) {
        writer.print("Error: " + msg + "\n");
    }

    private void getProfileData(HttpServletRequest request, PrintWriter writer)
            throws DaoException, ProtocolException, UnsupportedEncodingException {
        ArrayList<String> caseList = getCaseList(request);
        validateRequestForProfileOrMutationData(request);
        ArrayList<String> geneticProfileIdList = getGeneticProfileId(request);
        ArrayList<String> targetGeneList = getGeneList(request);

        if (targetGeneList.size() > 1 && geneticProfileIdList.size() > 1) {
            throw new ProtocolException
                    ("You can specify multiple genes or multiple genetic profiles, " +
                            "but not both at once!");
        }

        Boolean suppressMondrianHeader = new Boolean(request.getParameter(SUPPRESS_MONDRIAN_HEADER));
        String out = GetProfileData.getProfileData(geneticProfileIdList, targetGeneList,
                caseList, suppressMondrianHeader);
        writer.print(out);
    }

    private void getClinicalData(HttpServletRequest request, PrintWriter writer)
            throws DaoException, ProtocolException, UnsupportedEncodingException {
        HashSet<String> caseSet = new HashSet<String>(getCaseList(request));
        String out = GetClinicalData.getClinicalData(caseSet);
        writer.print(out);
    }

    /*
     * For getMutSig client specifies a Cancer Study ID,
     * and either a q_value_threshold, or a gene list.
     * The two latter parameters are optional.
     */
    private void getMutSig(HttpServletRequest request, PrintWriter writer)
            throws DaoException {
        String cancerStudyID = getCancerStudyId(request);
        String q_value_threshold = request.getParameter(Q_VALUE_THRESHOLD);
        String gene_list = request.getParameter(GENE_LIST);
        int cancerID = Integer.parseInt(cancerStudyID);
        if ((q_value_threshold == null || q_value_threshold.length() == 0)
                && (gene_list == null || gene_list.length() == 0)) {
            StringBuffer output = GetMutSig.GetAMutSig(cancerID);
            writer.print(output);
        } else if ((q_value_threshold != null || q_value_threshold.length() != 0)
                && (gene_list == null || gene_list.length() == 0)) {
            StringBuffer output = GetMutSig.GetAMutSig(cancerID, q_value_threshold, true);
            writer.print(output);
        } else if ((q_value_threshold == null || q_value_threshold.length() == 0)
                && (gene_list != null || gene_list.length() != 0)) {
            StringBuffer output = GetMutSig.GetAMutSig(cancerID, gene_list, false);
            writer.print(output);
        } else {
            writer.print("Invalid command. Please input a valid Q-Value Threshold, or Gene List.");
        }
    }

    private void getMutationData(HttpServletRequest request, PrintWriter writer)
            throws DaoException, ProtocolException, UnsupportedEncodingException {
        ArrayList<String> caseList = getCaseList(request);
        validateRequestForProfileOrMutationData(request);
        ArrayList<String> geneticProfileIdList = getGeneticProfileId(request);
        String geneticProfileId = geneticProfileIdList.get(0);
        ArrayList<String> targetGeneList = getGeneList(request);
        String out = GetMutationData.getProfileData(geneticProfileId, targetGeneList,
                caseList);
        writer.print(out);
    }

    private ArrayList<String> getGeneList(HttpServletRequest request) {
        String geneList = request.getParameter(GENE_LIST);
        //  Split on white space or commas
        Pattern p = Pattern.compile("[,\\s]+");
        String genes[] = p.split(geneList);
        ArrayList<String> targetGeneList = new ArrayList<String>();
        for (String gene : genes) {
            gene = gene.trim();
            if (gene.length() == 0) continue;
            targetGeneList.add(gene);
        }
        return targetGeneList;
    }

    private void validateRequestForProfileOrMutationData(HttpServletRequest request)
            throws ProtocolException {
        String geneticProfileIdStr = request.getParameter(GENETIC_PROFILE_ID);
        String geneList = request.getParameter(GENE_LIST);
        if (geneticProfileIdStr == null || geneticProfileIdStr.length() == 0) {
            throw new ProtocolException("Missing Parameter:  " + GENETIC_PROFILE_ID);
        }

        if (geneList == null || geneList.length() == 0) {
            throw new ProtocolException("Missing Parameter:  " + GENE_LIST);
        }
    }

    // TODO: rename TO getGeneticProfileId, as the return value is PLURAL
    private static ArrayList<String> getGeneticProfileId(HttpServletRequest request) throws ProtocolException {
        String geneticProfileIdStr = request.getParameter(GENETIC_PROFILE_ID);
        //  Split on white space or commas
        Pattern p = Pattern.compile("[,\\s]+");
        String geneticProfileIds[] = p.split(geneticProfileIdStr);
        ArrayList<String> geneticProfileIdList = new ArrayList<String>();
        for (String geneticProfileId : geneticProfileIds) {
            geneticProfileId = geneticProfileId.trim();
            geneticProfileIdList.add(geneticProfileId);
        }
        return geneticProfileIdList;
    }

    private ArrayList<String> getCaseList(HttpServletRequest request) throws ProtocolException,
            DaoException {
        String cases = request.getParameter(CASE_LIST);
        String caseSetId = request.getParameter(CASE_SET_ID);

        ArrayList<String> caseList = new ArrayList<String>();
        if (caseSetId != null) {
            DaoCaseList dao = new DaoCaseList();
            CaseList selectedCaseList = dao.getCaseListByStableId(caseSetId);
            if (selectedCaseList == null) {
                throw new ProtocolException("Invalid " + CASE_SET_ID + ":  " + caseSetId + ".");
            }
            caseList = selectedCaseList.getCaseList();
        } else if (cases != null) {
            for (String _case : cases.split("[\\s,]+")) {
                _case = _case.trim();
                if (_case.length() == 0) continue;
                caseList.add(_case);
            }
        } else {
            throw new ProtocolException(CASE_SET_ID + " or " + CASE_LIST + " must be specified.");
        }
        return caseList;
    }

    private void outputMissingParameterError(PrintWriter writer, String missingParameter) {
        outputError(writer, "you must specify a " + missingParameter + " parameter.");
    }

    /**
     * Verifies Database Connection.  In the event of an error, log
     * messages are written out to catalina.out.
     */
    private void verifyDbConnection() {
        //System.out.println("Verifying Database Connection...");
        try {
            //System.out.println("Attempting to retrieve Cancer Types...");
            DaoCancerStudy.getAllCancerStudies();
            //System.out.println("Database Connection -->  [OK]");
        } catch (DaoException e) {
            System.err.println("****  Fatal Error in CGDS.  Could not connect to "
                    + "database");
        }
    }

    private boolean goodCommand(PrintWriter writer, String cmd ){
       // check that command is correct
       String[] commands = { "getTypesOfCancer", "getNetwork", "getCancerStudies",
                "getCancerTypes", "getGeneticProfiles", "getProfileData", "getCaseLists", 
                "getClinicalData", "getMutationData", "getMutationFrequency",
                "getProteinArrayInfo", "getProteinArrayData"};
       for( String aCmd : commands ){
          if( aCmd.equals(cmd)){
             return true;
          }
       }
       outputError( writer, "'" + cmd + "' not a valid command." );
       return false;

    }

    /**
     * Given an HttpServletRequest, determine all cancer_study_ids associated with it.
     * cancer study identifiers can be inferred from profile_ids, case_list_ids, or case_ids.
     * this returns the set of ALL POSSIBLE cancer study identifiers
     *
     * @param request
     * @return the cancer_study_ids associated with the request, which will be empty
     *         if none can be determined; or null if a problem arises.
     * @throws DaoException
     * @throws ProtocolException
     */
    public static HashSet<String> getCancerStudyIDs(HttpServletRequest request)
            throws DaoException, ProtocolException {

        HashSet<String> cancerStudies = new HashSet<String>();

        // a CANCER_STUDY_ID is explicitly provided, as in getGeneticProfiles, getCaseLists, etc.
        // make sure the cancer_study_id provided in the request points to a real study
        String studyIDstring = getCancerStudyId(request);
        if (studyIDstring != null) {
            if (DaoCancerStudy.doesCancerStudyExistByStableId(studyIDstring)) {
                cancerStudies.add(studyIDstring);
            } else {
                return null;
            }
        }

        // a genetic_profile_id is explicitly provided, as in getProfileData
        if (null != request.getParameter(GENETIC_PROFILE_ID)) {
            ArrayList<String> genetic_profile_ids = getGeneticProfileId(request);
            for (String genetic_profile_id : genetic_profile_ids) {

                if (genetic_profile_id == null) {
                    return null;
                }

                DaoGeneticProfile aDaoGeneticProfile = new DaoGeneticProfile();
                GeneticProfile aGeneticProfile = aDaoGeneticProfile.getGeneticProfileByStableId(genetic_profile_id);
                if (aGeneticProfile != null &&
                        DaoCancerStudy.doesCancerStudyExistByInternalId(aGeneticProfile.getCancerStudyId())) {
                    cancerStudies.add(DaoCancerStudy.getCancerStudyByInternalId
                            (aGeneticProfile.getCancerStudyId()).getCancerStudyIdentifier());
                }
            }
        }

        // a case_set_id is explicitly provided, as in getProfileData, getMutationData, getClinicalData, etc.
        String caseSetId = request.getParameter(WebService.CASE_SET_ID);
        if (caseSetId != null) {
            DaoCaseList aDaoCaseList = new DaoCaseList();
            CaseList aCaseList = aDaoCaseList.getCaseListByStableId(caseSetId);
            if (aCaseList == null) {
                return null;
            }
            if (DaoCancerStudy.doesCancerStudyExistByInternalId(aCaseList.getCancerStudyId())) {
                cancerStudies.add(DaoCancerStudy.getCancerStudyByInternalId
                        (aCaseList.getCancerStudyId()).getCancerStudyIdentifier());
            } else {
                return null;
            }
        }

        // a case_list is explicitly provided, as in getClinicalData, etc.
        String caseList = request.getParameter(WebService.CASE_LIST);
        if (caseList != null) {
            DaoCase aDaoCase = new DaoCase();
            for (String _case : caseList.split("[\\s,]+")) {
                _case = _case.trim();
                if (_case.length() == 0) continue;

                int profileId = aDaoCase.getProfileIdForCase(_case);
                if (DaoCase.NO_SUCH_PROFILE_ID == profileId) {
                    return null;
                }

                DaoGeneticProfile aDaoGeneticProfile = new DaoGeneticProfile();
                GeneticProfile aGeneticProfile = aDaoGeneticProfile.getGeneticProfileById(profileId);
                if (aGeneticProfile == null) {
                    return null;
                }
                if (DaoCancerStudy.doesCancerStudyExistByInternalId(aGeneticProfile.getCancerStudyId())) {
                    cancerStudies.add(DaoCancerStudy.getCancerStudyByInternalId
                            (aGeneticProfile.getCancerStudyId()).getCancerStudyIdentifier());
                } else {
                    return null;
                }
            }
        }
        return cancerStudies;
    }

    /**
     * Get Cancer Study ID in a backward compatible fashion.
     */
    private static String getCancerStudyId(HttpServletRequest request) {
        String cancerStudyId = request.getParameter(WebService.CANCER_STUDY_ID);
        if (cancerStudyId == null || cancerStudyId.length() == 0) {
            cancerStudyId = request.getParameter(WebService.CANCER_TYPE_ID);
        }
        return cancerStudyId;
    }
}