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

package org.mskcc.cbio.portal.html.special_gene;

import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.portal.mapback.MapBack;
import org.mskcc.cbio.portal.mapback.Brca2;
import org.mskcc.cbio.portal.mapback.Brca1;
import org.mskcc.cbio.portal.html.HtmlUtil;

import java.util.ArrayList;

/**
 * Factory for Special Gene Objects.
 *
 * @author Ethan Cerami.
 */
public class SpecialGeneFactory {

    /**
     * Gets instance of a Special Gene.
     * @param geneSymbol Gene Symbol.
     * @return Special Gene Object.
     */
    public static SpecialGene getInstance(String geneSymbol) {
        if (geneSymbol.equalsIgnoreCase(SpecialGeneBrca1.BRCA1)) {
            return new SpecialGeneBrca1();
        } else if (geneSymbol.equalsIgnoreCase(SpecialGeneBrca2.BRCA2)) {
            return new SpecialGeneBrca2();
        } else {
            return null;
        }
    }
}