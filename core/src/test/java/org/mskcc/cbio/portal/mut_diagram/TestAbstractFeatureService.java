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

package org.mskcc.cbio.portal.mut_diagram;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;

import junit.framework.TestCase;
import org.codehaus.jackson.map.ObjectMapper;
import org.mskcc.cbio.portal.mut_diagram.FeatureService;
import org.mskcc.cbio.portal.mut_diagram.Sequence;
import org.mskcc.cbio.portal.mut_diagram.impl.CacheFeatureService;
import org.mskcc.cbio.portal.mut_diagram.impl.PfamGraphicsCacheLoader;

import java.util.List;

/**
 * Tests the FeatureService.
 */
public class TestAbstractFeatureService extends TestCase {
    private FeatureService featureService;

    @Override
    protected void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        PfamGraphicsCacheLoader cacheLoader = new PfamGraphicsCacheLoader(objectMapper);
        featureService = new CacheFeatureService(cacheLoader);
    }

    public void testCreateFeatureService() {
        assertNotNull(featureService);
    }

    public void testNullUniProtId() {
        try {
            List<Sequence> sequenceList = featureService.getFeatures(null);
            fail("Null Pointer Exception should have been thrown.");
        } catch (NullPointerException e) {
        }
    }
}