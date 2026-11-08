package com.mvron.booking.infrastructure.persistence;

import com.mvron.booking.domain.Resource;
import com.mvron.booking.domain.ResourceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaResourceRepository implements ResourceRepository {

    private final SpringDataResourceRepository springDataResourceRepository;

    public JpaResourceRepository(SpringDataResourceRepository springDataResourceRepository) {
        if (springDataResourceRepository == null) {
            throw new IllegalArgumentException("springDataResourceRepository must not be null");
        }
        this.springDataResourceRepository = springDataResourceRepository;
    }


    @Override 
    public Optional<Resource> findById(long id) {
        return springDataResourceRepository.findById(id).map(ResourceEntityMapper::toDomain);
    }


}