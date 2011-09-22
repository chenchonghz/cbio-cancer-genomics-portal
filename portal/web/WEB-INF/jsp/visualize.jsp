<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.mskcc.portal.model.*" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.DecimalFormat" %>                 
<%@ page import="org.mskcc.portal.util.GeneSetUtil" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.mskcc.portal.util.ZScoreUtil" %>
<%@ page import="org.mskcc.portal.servlet.ServletXssUtil" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.mskcc.portal.oncoPrintSpecLanguage.CallOncoPrintSpecParser" %>
<%@ page import="org.mskcc.portal.oncoPrintSpecLanguage.ParserOutput" %>
<%@ page import="org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintSpecification" %>
<%@ page import="org.mskcc.portal.util.HeatMapLegend" %>
<%@ page import="org.mskcc.portal.util.OncoPrintSpecificationDriver" %>
<%@ page import="org.mskcc.portal.util.Config" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>
<%@ page import="org.mskcc.portal.oncoPrintSpecLanguage.Utilities" %>
<%@ page import="org.mskcc.cgds.model.CancerStudy" %>
<%@ page import="org.mskcc.cgds.model.CaseList" %>
<%@ page import="org.mskcc.cgds.model.GeneticProfile" %>
<%@ page import="org.mskcc.cgds.model.GeneticAlterationType" %>
<%@ page import="org.mskcc.cgds.model.ClinicalData" %>

<%
    ArrayList<GeneticProfile> profileList =
            (ArrayList<GeneticProfile>) request.getAttribute
            (QueryBuilder.PROFILE_LIST_INTERNAL);
    HashSet<String> geneticProfileIdSet = (HashSet<String>) request.getAttribute
            (QueryBuilder.GENETIC_PROFILE_IDS);
    ServletXssUtil xssUtil = ServletXssUtil.getInstance();
    double zScoreThreshold = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, request);
    ArrayList<CaseList> caseSets = (ArrayList<CaseList>)
            request.getAttribute(QueryBuilder.CASE_SETS_INTERNAL);
    String caseSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);
    String caseIds = xssUtil.getCleanInput(request, QueryBuilder.CASE_IDS);
    ArrayList<CancerStudy> cancerStudies = (ArrayList<CancerStudy>)
            request.getAttribute(QueryBuilder.CANCER_TYPES_INTERNAL);
    String cancerTypeId = (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID);

    ProfileData mergedProfile = (ProfileData)
            request.getAttribute(QueryBuilder.MERGED_PROFILE_DATA_INTERNAL);
    String geneList = xssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);

    ParserOutput theOncoPrintSpecParserOutput = OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver( geneList,
             (HashSet<String>) request.getAttribute(QueryBuilder.GENETIC_PROFILE_IDS),
             (ArrayList<GeneticProfile>) request.getAttribute(QueryBuilder.PROFILE_LIST_INTERNAL),
             zScoreThreshold );

    OncoPrintSpecification theOncoPrintSpecification = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification();
    ProfileDataSummary dataSummary = new ProfileDataSummary( mergedProfile, theOncoPrintSpecification, zScoreThreshold );

    DecimalFormat percentFormat = new DecimalFormat("###,###.#%");
    String geneSetChoice = request.getParameter(QueryBuilder.GENE_SET_CHOICE);
    if (geneSetChoice == null) {
        geneSetChoice = "user-defined-list";
    }
    GeneSetUtil geneSetUtil = GeneSetUtil.getInstance();
    ArrayList<GeneSet> geneSetList = geneSetUtil.getGeneSetList();
    Set<String> warningUnion = (Set<String>) request.getAttribute(QueryBuilder.WARNING_UNION);


    ArrayList <GeneWithScore> geneWithScoreList = dataSummary.getGeneFrequencyList();
    ArrayList<String> mergedCaseList = mergedProfile.getCaseIdList();

    Config globalConfig = Config.getInstance();
    String siteTitle = SkinUtil.getTitle();

    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Results");
    String computeLogOddsRatioStr = request.getParameter(QueryBuilder.COMPUTE_LOG_ODDS_RATIO);
    boolean computeLogOddsRatio = false;
    if (computeLogOddsRatioStr != null) {
        computeLogOddsRatio = true;
    }

    ExtendedMutationMap mutationMap = (ExtendedMutationMap)
            request.getAttribute(QueryBuilder.MUTATION_MAP);
    Boolean mutationDetailLimitReached = (Boolean)
            request.getAttribute(QueryBuilder.MUTATION_DETAIL_LIMIT_REACHED);

    ArrayList <ClinicalData> clinicalDataList = (ArrayList<ClinicalData>)
            request.getAttribute(QueryBuilder.CLINICAL_DATA_LIST);
    
    boolean rppaExists = countProfiles(profileList, GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL) > 0
                || countProfiles(profileList, GeneticAlterationType.PROTEIN_ARRAY_PHOSPHORYLATION) > 0;
    
    boolean includeNetworks = SkinUtil.includeNetworks();
%>

<script type="text/javascript">

    function getTinyURL(longURL, success) {
        var API = 'http://json-tinyurl.appspot.com/?url=',
        URL = API + encodeURIComponent(longURL) + '&callback=?';

	    $.getJSON(URL, function(data){
        	success && success(data.tinyurl);
        });
    }

    function shrinkURL(longURL){
        getTinyURL(longURL, function(tinyurl){
            $('#tinyurl').html("<a href=\""+tinyurl+"\">"+tinyurl+"</a>");
        });
    }


</script>

<jsp:include page="global/header.jsp" flush="true" />

	<table>
        <tr>
            <td>

            <div id="results_container">
            
             <%   String smry = "";
                      
                    out.println ("<p><div class='gene_set_summary'>Gene Set / Pathway is altered in "
                        + percentFormat.format(dataSummary.getPercentCasesAffected())
                        + " of all cases.");
                 out.println ("<br></div></p>");
                 out.println ("<p><small><strong>");

                 for (CancerStudy cancerStudy: cancerStudies){
                    if (cancerTypeId.equals(cancerStudy.getCancerStudyStableId())){
                        smry = smry + cancerStudy.getName();
                    }
                }
                for (CaseList caseSet:  caseSets) {
                    if (caseSetId.equals(caseSet.getStableId())) {
                        smry = smry + "/" + caseSet.getName() + ":  "
                                + " (" + mergedCaseList.size() + ")";
                    }
                }
                for (GeneSet geneSet:  geneSetList) {
                    if (geneSetChoice.equals(geneSet.getId())) {
                        smry = smry + "/" + geneSet.getName();
                    }
                }
                smry = smry + "/" + geneWithScoreList.size();
                if (geneWithScoreList.size() == 1){
                    smry = smry + " gene";
                } else {
                    smry = smry + " genes";
                }

                out.println (smry);
                out.println ("</strong></small></p>");
                 %>

            <% if (warningUnion.size() > 0) {
                out.println ("<div class='warning'>");
                out.println ("<h4>Errors:</h4>");
                out.println ("<ul>");
                Iterator<String> warningIterator = warningUnion.iterator();
                int counter = 0;
                while (warningIterator.hasNext()) {
                    String warning = warningIterator.next();
                    if (counter++ < 10) {
                        out.println ("<li>" +  warning + "</li>");
                    }
                }
                if (warningUnion.size() > 10) {
                    out.println ("<li>...</li>");
                }
                out.println ("</ul>");
                out.println ("</div>");
            }
            if (geneWithScoreList.size() == 0) {
                out.println ("<b>Please go back and try again.</b>");
                out.println ("</div>");
            } else { %>

             <script type="text/javascript">
             $(document).ready(function(){

                 // Init Tool Tips
                 $("#toggle_query_form").tipTip();

             });
             </script>

            <p><a href="" title="Modify your original query.  Recommended over than hitting your browser's back button." id="toggle_query_form">
            <span class='query-toggle ui-icon ui-icon-triangle-1-e' style='float:left;'></span>
            <span class='query-toggle ui-icon ui-icon-triangle-1-s' style='float:left; display:none;'></span><b>Modify Query</b></a>
            <p/>

            <div style="margin-left:5px;display:none;" id="query_form_on_results_page">
            <%@ include file="query_form.jsp" %>
            </div>

            <div id="tabs">
                <ul>
                <% Boolean showMutTab = false; %>
                <%
                if (geneWithScoreList.size() > 0) {


                    Enumeration paramEnum = request.getParameterNames();
                    StringBuffer buf = new StringBuffer(request.getAttribute
                            (QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING) + "?");
                    while (paramEnum.hasMoreElements()) {
                        String paramName = (String) paramEnum.nextElement();
                        String values[] = request.getParameterValues(paramName);
                        if (values != null && values.length >0) {
                            for (int i=0; i<values.length; i++) {
                                String currentValue = values[i].trim();
                                if (currentValue.contains("mutation")){
                                    showMutTab = true;
                                }
                                if (paramName.equals(QueryBuilder.GENE_LIST) || paramName.equals(QueryBuilder.CASE_IDS)
                                    && currentValue != null) {
                                    //  Spaces must be converted to semis
                                    currentValue = Utilities.appendSemis(currentValue);
                                    //  Extra spaces must be removed.  Otherwise OMA Links will not work.
                                    currentValue = currentValue.replaceAll("\\s+", " ");
                                    currentValue = URLEncoder.encode(currentValue);
                                }
                                buf.append (paramName + "=" + currentValue + "&");
                            }
                        }
                    }

                    out.println ("<li><a href='#summary' class='result-tab' title='Summary of genomic alterations'>Summary</a></li>");

                    if (includeNetworks) {
                        out.println ("<li><a href='#network' class='result-tab' title='Network visualization and analysis'>"
                        + "Network</a></li>");
                    }

                    out.println ("<li><a href='#plots' class='result-tab' title='Multiple plots, including CNA v. mRNA expression'>"
                        + "Plots</a></li>");

                    if (clinicalDataList != null && clinicalDataList.size() > 0) {
                        out.println ("<li><a href='#survival' class='result-tab' title='Survival analysis and Kaplan-Meier curves'>"
                        + "Survival</a></li>");
                    }

                    if (computeLogOddsRatio && geneWithScoreList.size() > 1) {
                        out.println ("<li><a href='#gene_correlation' class='result-tab' title='Mutual exclusivity and co-occurrence analysis'>"
                        + "Mutual Exclusivity</a></li>");
                    }

                    if (showMutTab){
                        out.println ("<li><a href='#mutation_details' class='result-tab' title='Mutation details, including mutation type, "
                         + "amino acid change, validation status and predicted functional consequence'>"
                         + "Mutation Details</a></li>");
                    }
                    
                    if (rppaExists) {
                        out.println ("<li><a href='#protein_exp' class='result-tab' title='Reverse Phase Protein Array (RPPA) data'>"
                        + "RPPA Data</a></li>");
                    }

                    out.println ("<li><a href='#event_map' class='result-tab' title='Detailed event map of all genomic alterations'>"
                        + "Event Map</a></li>");
                    %>

                    <%@ include file="image_tabs.jsp" %>

                    <%
                    out.println ("<li><a href='#data_download' class='result-tab' title='Download all alterations or copy and paste into Excel'>Data Download</a></li>");
                    out.println ("<li><a href='#bookmark_email' class='result-tab' title='Bookmark or generate a URL for email'>Bookmark/Email</a></li>");
                    out.println ("<!--<li><a href='index.do' class='result-tab'>Create new query</a> -->");

                    out.println ("</ul>");

                    out.println ("<div class=\"section\" id=\"bookmark_email\">");
                    out.println ("<h4>Right click</b> on the link below to bookmark your results or send by email:</h4><br><a href='"
                            + buf.toString() + "'>" + request.getAttribute
                            (QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING) + "?...</a>");

                    String longLink = buf.toString();
                    out.println("<br><br>");
                    out.println("If you would like to use a <b>shorter URL that will not break in email postings</b>, you can use the<br><a href='http://tinyurl.com/'>TinyURL.com</a> service below:<BR>");
                    out.println("<BR><form><input type=\"button\" onClick=\"shrinkURL('"+longLink+"')\" value=\"Get TinyURL\"></form>");
                    out.println("<div id='tinyurl'></div>");
                    out.println("</div>");
                }

                %>

            <div class="section" id="summary">
            <%@ include file="fingerprint.jsp" %>
            <%@ include file="frequency_plot.jsp" %>
            </div>


            <%@ include file="plots_tab.jsp" %>
                    
            <%
                if (clinicalDataList != null && clinicalDataList.size() > 0) { %>
                    <%@ include file="clinical_tab.jsp" %>
            <%    }
            %>

            <% if (computeLogOddsRatio && geneWithScoreList.size() > 1) { %>
                <%@ include file="correlation.jsp" %>
            <% } %>
            <% if (mutationDetailLimitReached != null) {
                    out.println("<div class=\"section\" id=\"mutation_details\">");
                    out.println("<P>To retrieve mutation details, please specify "
                    + QueryBuilder.MUTATION_DETAIL_LIMIT + " or fewer genes.<BR>");
                    out.println("</div>");
                } else if (showMutTab) { %>
                    <%@ include file="mutation_details.jsp" %>
            <%  } %>

            <div class="section" id="event_map">
            <div class="map">
            <% 
            out.println( HeatMapLegend.outputHeatMapLegend( theOncoPrintSpecification.getUnionOfPossibleLevels()) );
            %>
			</div>            
                <br>
            <%@ include file="heatmap.jsp" %>
            </div>   <!-- end heat map div -->
            <%@ include file="image_tabs_data.jsp" %>

            <%
            if (rppaExists) { %>
                <%@ include file="protein_exp.jsp" %>
            <% } %>

            <%
            if (includeNetworks) { %>
                <%@ include file="networks.jsp" %>
            <% } %>
            
            </div> <!-- end tabs div -->
            </div>  <!-- end results container -->
            <% } %>
            </td>
        </tr>
    </table>
    </div>
    </td>
   <!-- <td width="172">
    
    </td>   -->
  </tr>
  <tr>
    <td colspan="3">
	<jsp:include page="global/footer.jsp" flush="true" />
    </td>
  </tr>
</table>
</center>
</div>
<jsp:include page="global/xdebug.jsp" flush="true" />    
</form>

<script type="text/javascript">
    // to fix problem of flash repainting
    $("a.result-tab").click(function(){
        if($(this).attr("href")=="#network") {
            $("div.section#network").removeAttr('style');
        } else {
            $("div.section#network").attr('style', 'display: block !important; height: 0px; width: 0px; visibility: hidden;');
        }
    });

    //  Set up Tip-Tip Event Handler for Genomic Profiles help
    $(".result-tab").tipTip({defaultPosition: "bottom", delay:"100", edgeOffset: 10, maxWidth: 200});
</script>

</body>
</html>
