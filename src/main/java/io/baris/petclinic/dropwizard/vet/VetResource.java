package io.baris.petclinic.dropwizard.vet;

import io.baris.petclinic.dropwizard.vet.model.CreateVetRequest;
import io.baris.petclinic.dropwizard.vet.model.Vet;
import io.baris.petclinic.dropwizard.vet.model.UpdateVetRequest;
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
 * Vet resource to serve vet endpoints
 */
@Path("/vets")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@RequiredArgsConstructor
public class VetResource {

    private final VetManager vetManager;

    @Operation(
        summary = "Get vet",
        tags = {"Vet"},
        responses = {
            @ApiResponse(
                description = "The vet",
                content = @Content(schema = @Schema(implementation = Vet.class))
            ),
            @ApiResponse(responseCode = "404", description = "Vet not found")
        }
    )
    @GET
    @Path("/{id}")
    public Vet getVet(
        final @PathParam("id") int id
    ) {
        return vetManager
            .getVet(id)
            .orElseThrow(() -> new NotFoundException("Vet not found"));
    }

    @Operation(
        summary = "Get all vets",
        tags = {"Vet"},
        responses = {
            @ApiResponse(
                description = "All vets",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Vet.class)))
            )
        }
    )
    @GET
    public List<Vet> getAllVets() {
        return vetManager.getAllVets();
    }

    @Operation(
        summary = "Create vet",
        tags = {"Vet"},
        responses = {
            @ApiResponse(
                description = "The vet",
                content = @Content(schema = @Schema(implementation = Vet.class))
            ),
            @ApiResponse(responseCode = "422", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Vet could not be created")
        }
    )
    @PUT
    public Vet createVet(
        final @Valid CreateVetRequest createVetRequest
    ) {
        return vetManager
            .createVet(VetMapper.mapToCreateVet(createVetRequest))
            .orElseThrow(() -> new InternalServerErrorException("Vet could not be created"));
    }

    @Operation(
        summary = "Update vet",
        tags = {"Vet"},
        responses = {
            @ApiResponse(
                description = "The vet",
                content = @Content(schema = @Schema(implementation = Vet.class))
            ),
            @ApiResponse(responseCode = "422", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Vet not found"),
            @ApiResponse(responseCode = "500", description = "Vet could not be updated")
        }
    )
    @Path("{id}")
    @POST
    public Vet updateVet(
        final @PathParam("id") int id,
        final @Valid UpdateVetRequest updateVetRequest
    ) {
        return vetManager
            .updateVet(VetMapper.mapToUpdateVet(id, updateVetRequest))
            .orElseThrow(() -> new NotFoundException("Vet not found"));
    }
}
