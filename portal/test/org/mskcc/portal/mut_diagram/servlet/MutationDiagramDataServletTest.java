package org.mskcc.portal.mut_diagram.servlet;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.portal.mut_diagram.FeatureService;
import org.mskcc.portal.mut_diagram.IdMappingService;
import org.mskcc.portal.mut_diagram.Sequence;

import com.google.common.collect.ImmutableList;

/**
 * Unit test for MutationDiagramDataServlet.
 */
public final class MutationDiagramDataServletTest {
    private MutationDiagramDataServlet dataServlet;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private String mutationsJson;
    private String emptyMutationsJson;
    private ExtendedMutation extendedMutation;
    private ObjectMapper objectMapper;
    private FeatureService featureService;
    private IdMappingService idMappingService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        featureService = mock(FeatureService.class, withSettings().serializable());
        idMappingService = mock(IdMappingService.class, withSettings().serializable());
        dataServlet = new MutationDiagramDataServlet(objectMapper, featureService, idMappingService);

        extendedMutation = new ExtendedMutation(new CanonicalGene(42, "PIK3CA"), "validationStatus", "mutationStatus", "mutationType");
        extendedMutation.setAminoAcidChange("A123K");
        extendedMutation.setCaseId("caseId");
        try {
            mutationsJson = objectMapper.writeValueAsString(new ExtendedMutation[] { extendedMutation });
            emptyMutationsJson = objectMapper.writeValueAsString(new ExtendedMutation[0]);
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testConstructor() {
        assertNotNull(dataServlet);
    }

    @Test
    public void testDoPostNullMutations() throws Exception {
        when(request.getParameter("hugoGeneSymbol")).thenReturn("PIK3CA");
        when(request.getParameter("mutations")).thenReturn(null);
        when(idMappingService.getUniProtIds("PIK3CA")).thenReturn(ImmutableList.of("P42336"));
        Sequence sequence = new Sequence();
        sequence.setLength(42);
        when(featureService.getFeatures("P42336")).thenReturn(ImmutableList.of(sequence));
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        dataServlet.doPost(request, response);
        verify(response).setContentType("application/json");
        assertEquals("[{\"length\":42,\"markups\":null,\"motifs\":null,\"regions\":null,\"metadata\":{\"hugoGeneSymbol\":\"PIK3CA\",\"uniProtId\":\"P42336\"},\"options\":null}]", stringWriter.toString());
    }

    @Test
    public void testDoPostEmptyStringMutations() throws Exception {
        when(request.getParameter("hugoGeneSymbol")).thenReturn("PIK3CA");
        when(request.getParameter("mutations")).thenReturn("");
        when(idMappingService.getUniProtIds("PIK3CA")).thenReturn(ImmutableList.of("P42336"));
        Sequence sequence = new Sequence();
        sequence.setLength(42);
        when(featureService.getFeatures("P42336")).thenReturn(ImmutableList.of(sequence));
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        dataServlet.doPost(request, response);
        verify(response).setContentType("application/json");
        assertEquals("[{\"length\":42,\"markups\":null,\"motifs\":null,\"regions\":null,\"metadata\":{\"hugoGeneSymbol\":\"PIK3CA\",\"uniProtId\":\"P42336\"},\"options\":null}]", stringWriter.toString());
    }

    @Test
    public void testDoPostEmptyMutations() throws Exception {
        when(request.getParameter("hugoGeneSymbol")).thenReturn("PIK3CA");
        when(request.getParameter("mutations")).thenReturn(emptyMutationsJson);
        when(idMappingService.getUniProtIds("PIK3CA")).thenReturn(ImmutableList.of("P42336"));
        Sequence sequence = new Sequence();
        sequence.setLength(42);
        when(featureService.getFeatures("P42336")).thenReturn(ImmutableList.of(sequence));
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        dataServlet.doPost(request, response);
        verify(response).setContentType("application/json");
        assertEquals("[{\"length\":42,\"markups\":null,\"motifs\":null,\"regions\":null,\"metadata\":{\"hugoGeneSymbol\":\"PIK3CA\",\"uniProtId\":\"P42336\"},\"options\":null}]", stringWriter.toString());
    }

    @Test
    public void testDoPost() throws Exception {
        when(request.getParameter("hugoGeneSymbol")).thenReturn("PIK3CA");
        when(request.getParameter("mutations")).thenReturn(mutationsJson);
        when(idMappingService.getUniProtIds("PIK3CA")).thenReturn(ImmutableList.of("P42336"));
        Sequence sequence = new Sequence();
        sequence.setLength(42);
        when(featureService.getFeatures("P42336")).thenReturn(ImmutableList.of(sequence));
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        dataServlet.doPost(request, response);
        verify(response).setContentType("application/json");
        // todo: expected values should be read from a file
        assertEquals("[{\"length\":42,\"markups\":[{\"start\":123,\"end\":123,\"href\":null,\"colour\":[\"#f36\"],\"lineColour\":\"#666\",\"display\":\"true\",\"residue\":null,\"type\":\"mutation\",\"headStyle\":\"diamond\",\"v_align\":\"top\",\"metadata\":{\"count\":1,\"location\":123,\"label\":\"A123K\"}}],\"motifs\":null,\"regions\":null,\"metadata\":{\"hugoGeneSymbol\":\"PIK3CA\",\"uniProtId\":\"P42336\"},\"options\":null}]", stringWriter.toString());
    }

    @Test
    public void testDoPostTwiceWithDifferentMutations() throws Exception {
        String alternateMutationsJson = null;
        ExtendedMutation alternateMutation = new ExtendedMutation(new CanonicalGene(42, "PIK3CA"), "validationStatus", "mutationStatus", "mutationType");
        alternateMutation.setAminoAcidChange("K345G");
        alternateMutation.setCaseId("caseId");
        try {
            alternateMutationsJson = objectMapper.writeValueAsString(new ExtendedMutation[] { alternateMutation });
        }
        catch (Exception e) {
            fail(e.getMessage());
        }

        when(request.getParameter("hugoGeneSymbol")).thenReturn("PIK3CA");
        // return A123K the first call, K345G the second
        when(request.getParameter("mutations")).thenReturn(mutationsJson).thenReturn(alternateMutationsJson);
        when(idMappingService.getUniProtIds("PIK3CA")).thenReturn(ImmutableList.of("P42336"));
        Sequence sequence = new Sequence();
        sequence.setLength(42);
        when(featureService.getFeatures("P42336")).thenReturn(ImmutableList.of(sequence));
        StringWriter stringWriter0 = new StringWriter();
        StringWriter stringWriter1 = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter0)).thenReturn(new PrintWriter(stringWriter1));

        dataServlet.doPost(request, response);
        dataServlet.doPost(request, response);
        verify(response, times(2)).setContentType("application/json");
        assertEquals("[{\"length\":42,\"markups\":[{\"start\":123,\"end\":123,\"href\":null,\"colour\":[\"#f36\"],\"lineColour\":\"#666\",\"display\":\"true\",\"residue\":null,\"type\":\"mutation\",\"headStyle\":\"diamond\",\"v_align\":\"top\",\"metadata\":{\"count\":1,\"location\":123,\"label\":\"A123K\"}}],\"motifs\":null,\"regions\":null,\"metadata\":{\"hugoGeneSymbol\":\"PIK3CA\",\"uniProtId\":\"P42336\"},\"options\":null}]", stringWriter0.toString());
        assertEquals("[{\"length\":42,\"markups\":[{\"start\":345,\"end\":345,\"href\":null,\"colour\":[\"#f36\"],\"lineColour\":\"#666\",\"display\":\"true\",\"residue\":null,\"type\":\"mutation\",\"headStyle\":\"diamond\",\"v_align\":\"top\",\"metadata\":{\"count\":1,\"location\":345,\"label\":\"K345G\"}}],\"motifs\":null,\"regions\":null,\"metadata\":{\"hugoGeneSymbol\":\"PIK3CA\",\"uniProtId\":\"P42336\"},\"options\":null}]", stringWriter1.toString());

    }

    @Test
    @Ignore // (ObjectMapper is not serializable)
    public void testSerializable() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(dataServlet);
        out.close();
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        Object dest = in.readObject();
        in.close();
        assertNotNull((MutationDiagramDataServlet) dest);
    }
}

