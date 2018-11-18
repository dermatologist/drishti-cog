package com.canehealth.repository;

import org.gtri.hdap.mdata.jpa.entity.ShimmerData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DrishtiShimmerDataRepository extends CrudRepository<ShimmerData, Long> {
    ShimmerData findByDocumentId(String documentId);
}
