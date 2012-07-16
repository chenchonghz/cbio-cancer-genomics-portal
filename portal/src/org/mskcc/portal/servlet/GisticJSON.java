package org.mskcc.portal.servlet;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGistic;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.Gistic;
import org.mskcc.cgds.validate.validationException;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * JSON servlet for fetching Gistic data.
 * If there is no Gistic data, then return an empty JSON.
 * @author Gideon Dresdner
 */
public class GisticJSON extends HttpServlet {
    private ServletXssUtil servletXssUtil;
    public static final String SELECTED_CANCER_STUDY = "selected_cancer_type";
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
     * Make a map out of every mutsig
     * Add that map to the mutSigJSONArray
     * Returns the empty set, {}, if qval > 0.01 (specificed by Ethan)
     */
    public static Map Gistic_toMap(Gistic gistic) {
        Map map = new HashMap();

        map.put("chromosome", gistic.getChromosome());
        map.put("peakStart", gistic.getPeakStart());
        map.put("peakEnd", gistic.getPeakEnd());

        map.put("genes_in_ROI", gistic.getGenes_in_ROI());
        
//        ArrayList<CanonicalGene> genes = gistic.getGenes_in_ROI();
//
//        Map genes_map = new HashMap();
//
//        for (CanonicalGene gene : genes) {
//            genes_map.put(gene.getStandardSymbol());
//        }

        map.put("qval", gistic.getqValue());
        map.put("res_qval", gistic.getRes_qValue());
        map.put("ampdel", gistic.getAmpDel());
        
        return map;
    }

    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String cancer_study_id = request.getParameter(SELECTED_CANCER_STUDY);
        JSONArray gisticJSONArray = new JSONArray();

        try {
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancer_study_id);
            
            if (log.isDebugEnabled()) {
                log.debug("cancerStudyId passed to GisticJSON: " + cancerStudy.getInternalId()) ;
            }

            ArrayList<Gistic> gistics = DaoGistic.getAllGisticByCancerStudyId(cancerStudy.getInternalId());

            if (log.isDebugEnabled()) {
                log.debug("list of gistics associated with cancerStudy: " + gistics) ;
            }

//            Collections.sort(gistics, new sortMutsigByRank());

            for (Gistic gistic : gistics) {
                Map map = Gistic_toMap(gistic);

                if (!map.isEmpty()) {
                    gisticJSONArray.add(map);
                }
            }
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();

            try {
                JSONValue.writeJSONString(gisticJSONArray, out);
            } finally {
                out.close();
            }

        } catch (DaoException e) {
            throw new ServletException(e);
        } catch (validationException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Just in case the request changes from GET to POST
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        doGet(request, response);
    }
}