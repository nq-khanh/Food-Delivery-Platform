package com.hkt.fooddelivery.repository;

import com.hkt.fooddelivery.entity.Restaurant;
import com.hkt.fooddelivery.entity.Shipper;
import com.hkt.fooddelivery.entity.User;
import com.hkt.fooddelivery.entity.enums.ApprovalStatus;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipperRepository extends JpaRepository<Shipper, UUID> {
    Optional<Shipper> findByUser(User user);
    @Query("""
        SELECT s FROM Shipper s
        JOIN FETCH s.user
        WHERE s.approvalStatus = :status
    """)
    List<Shipper> findByApprovalStatusWithUser(ApprovalStatus status);

    @Query(value = """
        SELECT s.* FROM shippers s
        WHERE s.approval_status = 'APPROVED'
          AND s.is_online = true
          AND s.is_busy = false
          AND s.location IS NOT NULL
        ORDER BY s.location <-> :point
        LIMIT :limit
        """, nativeQuery = true)
    List<Shipper> findNearestAvailableShippers(@Param("point") Point point, @Param("limit") int limit);
}
