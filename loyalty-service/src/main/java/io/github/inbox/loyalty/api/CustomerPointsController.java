package io.github.inbox.loyalty.api;

import io.github.inbox.loyalty.points.PointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerPointsController {

    private final PointsService service;

    public CustomerPointsController(PointsService service) {
        this.service = service;
    }

    @Operation(summary = "Get points balance by customer id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Points balance successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "404", description = "Customer points not found")
    })
    @GetMapping("/{customerId}/points")
    public PointsBalanceResponse balance(@Parameter(description = "Id of the customer", required = true)
                                         @PathVariable("customerId") @Positive Long customerId) {
        return service.balance(customerId);
    }
}
