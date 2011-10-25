package org.mskcc.portal.mutation.diagram;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

/**
 * Implementation of DomainService based on CacheBuilder.
 */
public final class CacheDomainService implements DomainService {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(CacheDomainService.class);
    private final Cache<String, List<Domain>> cache;

    /**
     * Create a new cache domain service with a cache populated by the specified cache loader.
     *
     * @param cacheLoader cache loader, must not be null
     */
    public CacheDomainService(final CacheLoader<String, List<Domain>> cacheLoader) {
        checkNotNull(cacheLoader, "cacheLoader must not be null");
        cache = CacheBuilder.newBuilder().build(cacheLoader);
    }

    /** {@inheritDoc} */
    public List<Domain> getDomains(String uniProtId) {
        checkNotNull(uniProtId, "uniProdId must not be null");
        try {
            return cache.get(uniProtId);
        }
        catch (Exception e) {
            logger.error("could not load domains from cache for " + uniProtId, e);
            return Collections.emptyList();
        }
    }
}
