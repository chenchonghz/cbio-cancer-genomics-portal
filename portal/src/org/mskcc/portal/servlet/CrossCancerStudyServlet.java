package org.mskcc.portal.servlet;

import org.mskcc.portal.util.XDebug;
import org.mskcc.portal.util.GlobalProperties;
import org.mskcc.portal.model.CancerType;
import org.mskcc.portal.remote.GetCancerTypes;
import org.owasp.validator.html.PolicyException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Central Servlet for performing Cross-Cancer Study Queries.
 *
 * @author Ethan Cerami.
 */
public class CrossCancerStudyServlet extends HttpServlet {

    private ServletXssUtil servletXssUtil;

    /**
     * Initializes the servlet.
     *
     * @throws javax.servlet.ServletException Servlet Init Error.
     */
    public void init() throws ServletException {
        super.init();
        String cgdsUrl = getInitParameter(QueryBuilder.CGDS_URL_PARAM);
        GlobalProperties.setCgdsUrl(cgdsUrl);
		String pathwayCommonsUrl = getInitParameter(QueryBuilder.PATHWAY_COMMONS_URL_PARAM);
        GlobalProperties.setPathwayCommonsUrl(pathwayCommonsUrl);
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servlet Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException            IO Error.
     */
    protected void doGet(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles HTTP POST Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servelt Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException            IO Error.
     */
    protected void doPost(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        XDebug xdebug = new XDebug();
        xdebug.startTimer();

        String geneList = servletXssUtil.getCleanInput(httpServletRequest, QueryBuilder.GENE_LIST);
        ArrayList<CancerType> cancerTypeList = GetCancerTypes.getCancerTypes(xdebug);

        httpServletRequest.setAttribute(QueryBuilder.CANCER_TYPES_INTERNAL, cancerTypeList);
        httpServletRequest.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);

        if (geneList == null || geneList.length() == 0) {
            xdebug.logMsg(this, "Branching to Query Page");
            RequestDispatcher dispatcher =
                    getServletContext().getRequestDispatcher("/WEB-INF/jsp/cross_cancer_query.jsp");
            dispatcher.forward(httpServletRequest, httpServletResponse);
        } else {
            xdebug.logMsg(this, "Branching to Results Page");
            RequestDispatcher dispatcher =
                    getServletContext().getRequestDispatcher("/WEB-INF/jsp/cross_cancer_results.jsp");
            dispatcher.forward(httpServletRequest, httpServletResponse);
        }
    }
}