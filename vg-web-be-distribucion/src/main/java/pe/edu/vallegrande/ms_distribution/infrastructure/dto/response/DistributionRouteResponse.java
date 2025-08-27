package pe.edu.vallegrande.ms_distribution.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
 

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionRouteResponse {

    private String id;
    private String organizationId;
    private String routeCode;
    private String routeName;
    private String zones;
    private Integer totalEstimatedDuration;
    private String responsibleUserId;
    private String status;
    private Instant createdAt;

    
}
