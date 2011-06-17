<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.Config" %>

<%
    Config globalConfig = Config.getInstance();
    String siteTitle = globalConfig.getProperty("skin.title");

    if (siteTitle == null) {
        siteTitle = "cBio Cancer Genomics Portal";
    }
%>

<head>
<link href="css/cancergenomics.css" type="text/css" rel="stylesheet" />
<link href="css/style.css" type="text/css" rel="stylesheet" />
</head>

<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Onco Query Language (OQL) Instructions"); %>
<body>

    <div id="instructions">
    <div class="markdown">
            <p><jsp:include page="content/onco_spec_lang_desc.html" flush="true" /></p>
    </div>
    <jsp:include page="WEB-INF/jsp/global/footer.jsp" flush="true" />
    </div>
</body>
</html>
