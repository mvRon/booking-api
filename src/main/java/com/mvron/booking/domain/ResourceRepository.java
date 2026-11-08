package com.mvron.booking.domain;

import java.util.Optional;

public interface ResourceRepository {

    Optional<Resource> findById(long id);
}