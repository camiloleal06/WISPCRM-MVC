package org.wispcrm.daos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wispcrm.modelo.AwsCredential;

public interface AwsCredentialDao extends JpaRepository<AwsCredential, Integer> {

}
