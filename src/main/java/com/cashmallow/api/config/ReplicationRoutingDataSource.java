package com.cashmallow.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    private Logger log = LoggerFactory.getLogger(ReplicationRoutingDataSource.class);

    /**
     * Determine datasource
     * <p>
     * If you want to set 'readOnly = true' in @Transactional, you must also set 'propagation = Propagation.SUPPORTS'.
     * Example) @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
     *
     * @see https://howtodoinjava.com/spring-orm/spring-3-2-5-abstractroutingdatasource-example/
     * @see http://egloos.zum.com/kwon37xi/v/5364167
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String dataSourceType = TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "read" : "write";

        log.trace("current dataSourceType : {}", dataSourceType);

        return dataSourceType;
    }


}
