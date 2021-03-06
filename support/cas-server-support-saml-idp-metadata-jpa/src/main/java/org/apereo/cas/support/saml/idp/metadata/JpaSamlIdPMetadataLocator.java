package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link JpaSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerSamlMetadataIdP")
@Slf4j
public class JpaSamlIdPMetadataLocator implements SamlIdPMetadataLocator {
    @PersistenceContext(unitName = "samlMetadataIdPEntityManagerFactory")
    private transient EntityManager entityManager;

    private final CipherExecutor<String, String> metadataCipherExecutor;

    private SamlIdPMetadataDocument metadataDocument;

    @Override
    public Resource getSigningCertificate() {
        fetchMetadataDocument();
        val cert = metadataDocument.getSigningCertificate();
        return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Resource getSigningKey() {
        fetchMetadataDocument();
        val data = metadataDocument.getSigningKey();
        val cert = metadataCipherExecutor.decode(data);
        return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Resource getMetadata() {
        fetchMetadataDocument();
        val data = metadataDocument.getMetadata();
        return new InputStreamResource(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Resource getEncryptionCertificate() {
        fetchMetadataDocument();
        val cert = metadataDocument.getEncryptionCertificate();
        return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Resource getEncryptionKey() {
        fetchMetadataDocument();
        val data = metadataDocument.getEncryptionKey();
        val cert = metadataCipherExecutor.decode(data);
        return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
    }

    private void fetchMetadataDocument() {
        try {
            metadataDocument = this.entityManager.createQuery("SELECT r FROM SamlIdPMetadataDocument r", SamlIdPMetadataDocument.class)
                .setMaxResults(1)
                .getSingleResult();
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage(), e);
        }
    }

    @Override
    public boolean exists() {
        fetchMetadataDocument();
        return metadataDocument != null;
    }
}

