package org.pac4j.saml.metadata;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.util.ConfigurationManager;
import org.pac4j.saml.util.DefaultConfigurationManager;
import org.pac4j.saml.util.SAML2HttpClientBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * This is {@link SAML2HttpUrlMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 */
public class SAML2HttpUrlMetadataGeneratorTests {
    private static SAML2Configuration initialConfiguration() throws MalformedURLException {
        final ConfigurationManager mgr = new DefaultConfigurationManager();
        mgr.configure();

        val configuration = new SAML2Configuration();
        configuration.setForceKeystoreGeneration(true);
        configuration.setKeystorePath("target/keystore.jks");
        configuration.setKeystorePassword("pac4j");
        configuration.setPrivateKeyPassword("pac4j");
        configuration.setSignMetadata(true);
        configuration.setServiceProviderEntityId("urn:mace:saml:pac4j.org");
        configuration.setServiceProviderMetadataResource(new UrlResource("http://localhost:8088/saml"));
        configuration.setIdentityProviderMetadataResource(new ClassPathResource("idp-metadata.xml"));
        configuration.init();
        return configuration;
    }

    @Test
    public void verifyPost() throws Exception {

        val wireMockServer = new WireMockServer(8088);
        wireMockServer.stubFor(
            post(urlPathEqualTo("/saml"))
                .withHeader("Accept", equalTo(ContentType.APPLICATION_XML.getMimeType()))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", ContentType.APPLICATION_XML.getMimeType())));
        wireMockServer.start();
        try {
            var metadataGenerator =
                new SAML2HttpUrlMetadataGenerator(new URL("http://localhost:8088/saml"), new SAML2HttpClientBuilder().build());
            val entity = metadataGenerator.buildEntityDescriptor();
            assertNotNull(entity);
            val metadata = metadataGenerator.getMetadata(entity);
            assertNotNull(metadata);
            assertTrue(metadataGenerator.storeMetadata(metadata, true));
        } finally {
            wireMockServer.stop();
        }
    }

    @Test
    public void verifyGet() throws Exception {
        val restBody = IOUtils.toString(
            new ClassPathResource("sample-sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8);
        val wireMockServer = new WireMockServer(8087);
        wireMockServer.stubFor(
            get(urlPathEqualTo("/saml"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", ContentType.APPLICATION_XML.getMimeType())
                    .withBody(restBody)));
        wireMockServer.start();
        try {
            val configuration = initialConfiguration();
            final SAML2MetadataGenerator metadataGenerator =
                new SAML2HttpUrlMetadataGenerator(new URL("http://localhost:8087/saml"), new SAML2HttpClientBuilder().build());
            val resolver = metadataGenerator.buildMetadataResolver();
            assertNotNull(resolver);

            var entity = resolver.resolveSingle(
                new CriteriaSet(new EntityIdCriterion(configuration.getServiceProviderEntityId())));
            assertNotNull(entity);
        } finally {
            wireMockServer.stop();
        }
    }
}
