package com.pheeeew.sigh.domain.repository.projection;

import java.time.Instant;

public interface SighMapProjection {

    Long getId();

    double getLongitude();

    double getLatitude();

    Instant getCreatedAt();
}
