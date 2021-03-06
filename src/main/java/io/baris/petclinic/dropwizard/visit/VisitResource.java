package io.baris.petclinic.dropwizard.visit;

import io.baris.petclinic.dropwizard.vet.VetManager;
import io.baris.petclinic.dropwizard.visit.model.Visit;
import io.baris.petclinic.dropwizard.pet.PetManager;
import io.baris.petclinic.dropwizard.visit.model.MakeVisitRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Visit resource to serve visit endpoints
 */
@Path("/visits")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@RequiredArgsConstructor
public class VisitResource {

    private final VisitManager visitManager;
    private final PetManager petManager;
    private final VetManager vetManager;

    @Operation(
        summary = "Make visit",
        tags = {"Visit"},
        responses = {
            @ApiResponse(
                description = "The visit",
                content = @Content(schema = @Schema(implementation = Visit.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid parameter"),
            @ApiResponse(responseCode = "422", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Visit could not be created")
        }
    )
    @PUT
    @Path("/pets/{petId}/vets/{vetId}")
    public Visit makeVisit(
        final @PathParam("petId") int petId,
        final @PathParam("vetId") int vetId,
        final @Valid MakeVisitRequest createPetRequest
    ) {
        // validation
        petManager.getPet(petId)
            .orElseThrow(() -> new BadRequestException("Pet does not exist"));
        vetManager.getVet(vetId)
            .orElseThrow(() -> new BadRequestException("Pet does not exist"));

        return visitManager
            .makeVisit(VisitMapper.mapToMakeVisit(petId, vetId, createPetRequest))
            .orElseThrow(() -> new InternalServerErrorException("Visit could not be created"));
    }

    @Operation(
        summary = "Get visits for a pet",
        tags = {"Visit"},
        responses = {
            @ApiResponse(
                description = "Visits for a pet",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Visit.class)))
            )
        }
    )
    @GET
    @Path("/pets/{petId}")
    public List<Visit> getPetVisits(
        final @PathParam("petId") int petId
    ) {
        // validation
        petManager.getPet(petId)
            .orElseThrow(() -> new BadRequestException("Pet does not exist"));

        return visitManager.getPetVisits(petId);
    }
}
