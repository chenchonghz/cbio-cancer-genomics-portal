<%@ page import="java.util.Enumeration" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.mskcc.portal.util.MakeOncoPrint" %>
<div class="oncoprint_section">
    <p><h4>OncoPrint&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<small>(<a href="faq.jsp#what-are-oncoprints">What are OncoPrints?</a>)</small></h4>
    <p></p>
    <%
    
    // TODO: rename this file and other 'fingerprints' to OncoPrint, as that's our standard name

    // height of OncoPrint is roughly proportional to number of genes
    // using MakeOncoPrint.CELL_HEIGHT to avoid another constant
    // TODO: calculate height more carefully; best to use the 
    int height = 95 + (MakeOncoPrint.CELL_HEIGHT + 2) * geneWithScoreList.size();
    height += 100;

    %>
        <form action="index.do" method="GET"><P>Get OncoPrint:

    <%

    StringBuffer tempUri = new StringBuffer("index.do?");
    Enumeration paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
        String paramName = (String) paramNames.nextElement();
        String values[] = request.getParameterValues(paramName);
        for( String value: values){
            value = xssUtil.getCleanInput(value);
            tempUri.append(paramName + "=" + URLEncoder.encode(value.trim()) +"&");
            out.println ("<input type=hidden name='" + paramName + "' value = '" + value.trim() + "'>");
        }
    }

    // output buttons for both OncoPrint output formats
    String[] outputTypes = { "html", "svg" };
    for( String outputType: outputTypes){

        out.println ("&nbsp;&nbsp;<input type='submit' name='output' value='" + outputType.toUpperCase() + "'>");

    }

    String fullURL = tempUri.toString() + "output=html&";

    %>
            <! -- CHECKBOX FOR SHOW ALTERED --><input type="checkbox" name="showAlteredColumns" value="true">Only show altered cases.

            </form>

    <p>
        <object data="<%= fullURL%>" class=oncoprint TYPE="text/html" WIDTH=800 HEIGHT=<%= height %> ></object>
    </p>


        <!-- TODO: http://www.w3.org/TR/REC-html40/struct/objects.html#adef-width-IMG "All IMG and OBJECT attributes that concern visual alignment and presentation have been deprecated in favor of style sheets." -->
        <p>

</div>