package org.mskcc.portal.mut_diagram.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.portal.mut_diagram.FeatureService;
import org.mskcc.portal.mut_diagram.IdMappingService;
import org.mskcc.portal.mut_diagram.Markup;
import org.mskcc.portal.mut_diagram.Pileup;
import org.mskcc.portal.mut_diagram.Sequence;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Mutation diagram data servlet.
 */
@Singleton
public final class MutationDiagramDataServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(MutationDiagramDataServlet.class);
    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    private final ObjectMapper objectMapper;
    private final FeatureService featureService;
    private final IdMappingService idMappingService;

    @Inject
    public MutationDiagramDataServlet(final ObjectMapper objectMapper, final FeatureService featureService, final IdMappingService idMappingService) {
        this.objectMapper = objectMapper;
        this.featureService = featureService;
        this.idMappingService = idMappingService;
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // todo:  check and sanitize hugoGeneSymbol and mutations if necessary
        String hugoGeneSymbol = request.getParameter("hugoGeneSymbol");
        List<ExtendedMutation> mutations = readMutations(request.getParameter("mutations"));
        List<String> uniProtIds = idMappingService.getUniProtIds(hugoGeneSymbol);

        String uniProtId = uniProtIds.get(0); // uh oh
        List<Sequence> sequences = featureService.getFeatures(uniProtId);
        if (!sequences.isEmpty()) {
            Sequence sequence = sequences.get(0);
            if (sequence.getMetadata() == null) {
                sequence.setMetadata(new HashMap<String, Object>());
            }
            sequence.getMetadata().put("hugoGeneSymbol", hugoGeneSymbol);
            sequence.getMetadata().put("uniProtId", uniProtId);

            for (Pileup pileup : Pileup.pileup(mutations)) {
                Markup markup = new Markup();
                markup.setDisplay("true");
                markup.setStart(pileup.getLocation());
                markup.setEnd(pileup.getLocation());
                markup.setColour(ImmutableList.of("#f36"));
                markup.setLineColour("#666");
                markup.setHeadStyle("diamond");
                markup.setV_align("top");
                markup.setType("mutation");
                markup.setMetadata(new HashMap<String, Object>());
                markup.getMetadata().put("count", pileup.getCount());
                markup.getMetadata().put("label", pileup.getLabel());
                markup.getMetadata().put("location", pileup.getLocation());
                if (sequence.getMarkups() == null) {
                    sequence.setMarkups(new ArrayList<Markup>());
                }
                System.out.println("adding markup for pileup " + pileup.getLabel());
                sequence.getMarkups().add(markup);
            }
            System.out.println("sequence markups " + sequence.getMarkups());
        }
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), sequences);
    }

    /**
     * Read and return a list of extended mutations from the specified value in JSON format
     * or an empty list if the value cannot be read.
     *
     * @param value list of extended mutations in JSON format
     * @return a list of extended mutations from the specified value in JSON format, or an
     *    empty list if the value cannot be read
     */
    List<ExtendedMutation> readMutations(final String value) {
        List<ExtendedMutation> mutations = Collections.emptyList();
        if (value != null) {
            try {
                TypeFactory typeFactory = objectMapper.getTypeFactory();
                CollectionType sequenceList = typeFactory.constructCollectionType(List.class, ExtendedMutation.class);
                mutations = objectMapper.readValue(value, sequenceList);
            }
            catch (JsonParseException e) {
                logger.warn("could not deserialize extended mutations", e);
            }
            catch (JsonMappingException e) {
                logger.warn("could not deserialize extended mutations", e);
            }
            catch (IOException e) {
                logger.warn("could not deserialize extended mutations", e);
            }
        }
        return mutations;
    }
}
