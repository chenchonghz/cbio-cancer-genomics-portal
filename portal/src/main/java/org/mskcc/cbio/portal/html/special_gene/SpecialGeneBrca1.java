package org.mskcc.cbio.portal.html.special_gene;

import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.portal.mapback.MapBack;
import org.mskcc.cbio.portal.mapback.Brca1;
import org.mskcc.cbio.portal.html.HtmlUtil;

import java.util.ArrayList;

/**
 * Special Gene Implementation for BRCA1.
 *
 * @author Ethan Cerami.
 */
class SpecialGeneBrca1 extends SpecialGene {
    public static final String BRCA1 = "BRCA1";

    public ArrayList<String> getDataFieldHeaders() {
        ArrayList<String> headerList = new ArrayList<String>();
        headerList.add("NT Position*");
        headerList.add("Notes");
        return headerList;
    }

    public String getFooter() {
        return ("* Known BRCA1 185/187DelAG and 5382/5385 insC founder mutations " +
                "are noted.");
    }

    public ArrayList<String> getDataFields(ExtendedMutation mutation) {
        ArrayList<String> dataFields = new ArrayList<String>();
        MapBack mapBack = new MapBack(new Brca1(), mutation.getEndPosition());
        long ntPosition = mapBack.getNtPositionWhereMutationOccurs();
        String annotation = getAnnotationBrca1(ntPosition);
        setNtPosition(ntPosition, dataFields);
        dataFields.add(HtmlUtil.getSafeWebValue(annotation));
        return dataFields;
    }

    private static String getAnnotationBrca1(long nt) {
        if (nt >= 185 && nt <= 188) {
            return "185/187DelAG Founder Mutation";
        } else if (nt >= 5382 && nt <= 5385) {
            return "5382/5385 insC Founder Mutation";
        } else {
            return null;
        }
    }
}