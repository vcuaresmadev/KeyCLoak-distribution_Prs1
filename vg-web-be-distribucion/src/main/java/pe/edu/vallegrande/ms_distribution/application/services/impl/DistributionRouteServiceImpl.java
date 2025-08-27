package pe.edu.vallegrande.ms_distribution.application.services.impl;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.ms_distribution.application.services.DistributionRouteService;
import pe.edu.vallegrande.ms_distribution.domain.enums.Constants;
import pe.edu.vallegrande.ms_distribution.domain.models.DistributionRoute;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionRouteCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.response.DistributionRouteResponse;
import pe.edu.vallegrande.ms_distribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.ms_distribution.infrastructure.repository.DistributionRouteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
public class DistributionRouteServiceImpl implements DistributionRouteService {

    @Autowired
    private DistributionRouteRepository routeRepository;

    @Override
    public Flux<DistributionRoute> getAll() {
        return routeRepository.findAll();
    }

    @Override
    public Flux<DistributionRoute> getAllActive() {
        return routeRepository.findAllByStatus(Constants.ACTIVE.name());
    }

    @Override
    public Flux<DistributionRoute> getAllInactive() {
        return routeRepository.findAllByStatus(Constants.INACTIVE.name());
    }

    @Override
    public Mono<DistributionRoute> getById(String id) {
        return routeRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(), "Route not found", "Route with ID " + id + " does not exist")));
    }

 @Override
public Mono<DistributionRouteResponse> save(DistributionRouteCreateRequest request) {
    return generateNextRouteCode()
        .flatMap(generatedCode -> {

            DistributionRoute route = DistributionRoute.builder()
                    .organizationId(request.getOrganizationId())
                    .routeCode(generatedCode) // ← Usamos el código generado
                    .routeName(request.getRouteName())
                    // Guardamos el JSON crudo de zonas como String para flexibilidad
                    .zones(com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                            .createArrayNode()
                            .addAll(request.getZones().stream().map(z -> {
                                com.fasterxml.jackson.databind.node.ObjectNode node = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().createObjectNode();
                                node.put("zone_id", z.getZoneId());
                                node.put("order", z.getOrder());
                                node.put("estimated_duration", z.getEstimatedDuration());
                                return node;
                            }).collect(java.util.stream.Collectors.toList()))
                            .toPrettyString())
                    .totalEstimatedDuration(request.getTotalEstimatedDuration())
                    .responsibleUserId(request.getResponsibleUserId())
                    .status(Constants.ACTIVE.name())
                    .createdAt(Instant.now())
                    .build();

            return routeRepository.save(route)
                    .map(saved -> DistributionRouteResponse.builder()
                            .id(saved.getId())
                            .organizationId(saved.getOrganizationId())
                            .routeCode(saved.getRouteCode())
                            .routeName(saved.getRouteName())
                            // No transformamos el String a lista; si fuera necesario, se puede parsear
                            .totalEstimatedDuration(saved.getTotalEstimatedDuration())
                            .responsibleUserId(saved.getResponsibleUserId())
                            .status(saved.getStatus())
                            .createdAt(saved.getCreatedAt())
                            .build());
        });
}

private Mono<String> generateNextRouteCode() {
    return routeRepository.findTopByOrderByRouteCodeDesc()
            .map(last -> {
                String lastCode = last.getRouteCode(); // Ej: "RUT005"
                int number = Integer.parseInt(lastCode.replace("RUT", ""));
                return String.format("RUT%03d", number + 1);
            })
            .defaultIfEmpty("RUT001");
}


    @Override
    public Mono<DistributionRoute> update(String id, DistributionRoute route) {
        return routeRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(), "Route not found", "Cannot update non-existent route with id " + id)))
                .flatMap(existing -> {
                    existing.setRouteName(route.getRouteName());
                    existing.setZones(route.getZones());
                    existing.setTotalEstimatedDuration(route.getTotalEstimatedDuration());
                    existing.setResponsibleUserId(route.getResponsibleUserId());
                    return routeRepository.save(existing);
                });
    }

    @Override
    public Mono<Void> delete(String id) {
        return routeRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(), "Route not found", "Cannot delete non-existent route with id " + id)))
                .flatMap(routeRepository::delete);
    }

    @Override
    public Mono<DistributionRoute> activate(String id) {
        return changeStatus(id, Constants.ACTIVE.name());
    }

    @Override
    public Mono<DistributionRoute> deactivate(String id) {
        return changeStatus(id, Constants.INACTIVE.name());
    }

    private Mono<DistributionRoute> changeStatus(String id, String status) {
        return routeRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(), "Route not found", "Cannot change status of non-existent route with id " + id)))
                .flatMap(route -> {
                    route.setStatus(status);
                    return routeRepository.save(route);
                });
    }
}
