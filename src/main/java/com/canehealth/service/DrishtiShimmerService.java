package com.canehealth.service;

import com.canehealth.repository.DrishtiShimmerDataRepository;
import org.gtri.hdap.mdata.service.ShimmerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DrishtiShimmerService extends ShimmerService {

    @Autowired
    private DrishtiShimmerDataRepository drishtiShimmerDataRepository;

}
