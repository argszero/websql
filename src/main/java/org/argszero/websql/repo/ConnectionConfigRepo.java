package org.argszero.websql.repo;

import org.argszero.websql.domain.ConnectionConfig;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by shaoaq on 14-5-1.
 */
public interface ConnectionConfigRepo extends CrudRepository<ConnectionConfig, Long> {
}
