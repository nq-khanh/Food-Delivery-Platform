package com.hkt.fooddelivery.service;

import com.hkt.fooddelivery.dto.LocationDTO;
import com.hkt.fooddelivery.dto.ShipperResponse;
import com.hkt.fooddelivery.dto.UpdateVehicleRequest;
import com.hkt.fooddelivery.entity.Shipper;
import com.hkt.fooddelivery.exception.ResourceNotFoundException;
import com.hkt.fooddelivery.mapper.ShipperMapper;
import com.hkt.fooddelivery.repository.ShipperRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipperService {

    private final ShipperRepository repository;
    private final ShipperMapper mapper;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private Shipper getShipper(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Shipper với ID: " + id));
    }

    public ShipperResponse getProfile(UUID id) {
        return mapper.toResponse(getShipper(id));
    }

    @Transactional
    public ShipperResponse goOnline(UUID id) {
        Shipper s = getShipper(id);
        s.goOnline(); // Đã có check BusinessException: Phải được APPROVED mới online được
        return mapper.toResponse(repository.save(s));
    }

    @Transactional
    public ShipperResponse goOffline(UUID id) {
        Shipper s = getShipper(id);
        s.goOffline();
        return mapper.toResponse(repository.save(s));
    }

    @Transactional
    public void updateLocation(UUID id, LocationDTO locationDTO) {
        Shipper s = getShipper(id);
        Point point = geometryFactory.createPoint(new Coordinate(locationDTO.lng(), locationDTO.lat()));
        s.updateLocation(point);
        repository.save(s);
    }

    @Transactional
    public ShipperResponse updateVehicle(UUID id, UpdateVehicleRequest req) {
        Shipper s = getShipper(id);
        if (req.vehicleInfo() != null) s.updateVehicleInfo(req.vehicleInfo());
        if (req.licensePlate() != null) s.updateLicensePlate(req.licensePlate());
        return mapper.toResponse(repository.save(s));
    }

    @Transactional
    public void setBusyStatus(UUID id, boolean isBusy) {
        Shipper s = getShipper(id);
        if (isBusy) s.markBusy();
        else s.markAvailable();
        repository.save(s);
    }

    /**
     * Tìm danh sách Shipper gần một vị trí cụ thể (vị trí nhà hàng)
     */
    public List<ShipperResponse> findNearbyShippers(LocationDTO location, int limit) {
        Point searchPoint = geometryFactory.createPoint(new Coordinate(location.lng(), location.lat()));

        List<Shipper> shippers = repository.findNearestAvailableShippers(searchPoint, limit);

        return shippers.stream()
                .map(mapper::toResponse)
                .toList();
    }
}