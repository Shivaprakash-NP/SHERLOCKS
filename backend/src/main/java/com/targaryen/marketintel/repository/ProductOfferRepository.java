package com.targaryen.marketintel.repository;

import com.targaryen.marketintel.model.ProductOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOfferRepository extends JpaRepository<ProductOffer, Long> {
}
