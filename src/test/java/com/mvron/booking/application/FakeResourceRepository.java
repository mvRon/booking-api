package com.mvron.booking.application;

import com.mvron.booking.domain.Resource;
import com.mvron.booking.domain.ResourceRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class FakeResourceRepository implements ResourceRepository {

    private final Map<Long, Resource> resources = new HashMap<>();

    void add(Resource resource) {
        resources.put(resource.getId(), resource);
    }

    @Override
    public Optional<Resource> findById(long id) {
        return Optional.ofNullable(resources.get(id));
    }
}