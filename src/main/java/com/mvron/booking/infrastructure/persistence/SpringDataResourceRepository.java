package com.mvron.booking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataResourceRepository extends JpaRepository<ResourceEntity, Long>{
    
}
