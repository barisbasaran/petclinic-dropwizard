package io.baris.petclinic.dropwizard.pet;

import io.baris.petclinic.dropwizard.pet.model.Pet;
import io.baris.petclinic.dropwizard.pet.model.UpdatePetRequest;
import io.baris.petclinic.dropwizard.pet.model.CreatePetRequest;
import io.baris.petclinic.dropwizard.pet.model.Species;
import io.baris.petclinic.dropwizard.testing.AppBootstrapExtension;
import io.baris.petclinic.dropwizard.testing.DbResetExtension;
import io.baris.petclinic.dropwizard.testing.PostgreExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static io.baris.petclinic.dropwizard.testing.TestUtils.TEST_CONFIG;
import static io.baris.petclinic.dropwizard.testing.TestUtils.UNPROCESSIBLE_ENTITY;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class PetIntegrationTest {

    @RegisterExtension
    @Order(0)
    public final static PostgreExtension postgre = new PostgreExtension(TEST_CONFIG);

    @RegisterExtension
    @Order(1)
    public final static AppBootstrapExtension app = new AppBootstrapExtension(TEST_CONFIG, postgre.getDatabaseUrl());

    @RegisterExtension
    public DbResetExtension dbReset = new DbResetExtension(postgre.getJdbi());

    @Test
    public void getAllPets_Success() {
        // arrange
        postgre.addPet("Sofi", 2, Species.CAT);
        postgre.addPet("Lucky", 5, Species.DOG);

        // act
        var response = app.client()
            .target(getTargetUrl())
            .path("pets")
            .request()
            .get();

        // assert
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);

        var pets = response.readEntity(Pet[].class);
        assertThat(pets).hasSize(2);

        var lucky = pets[0];
        assertThat(lucky.getName()).isEqualTo("Lucky");
        assertThat(lucky.getAge()).isEqualTo(5);
        assertThat(lucky.getSpecies()).isEqualTo(Species.DOG);

        var sofi = pets[1];
        assertThat(sofi.getName()).isEqualTo("Sofi");
        assertThat(sofi.getAge()).isEqualTo(2);
        assertThat(sofi.getSpecies()).isEqualTo(Species.CAT);
    }

    @Test
    public void getPet_Success() {
        // arrange
        postgre.addPet("Charlie", 18, Species.PARROT);
        var pet = postgre.getPet("Charlie");
        assertThat(pet).isPresent();

        // act
        var response = app.client()
            .target(getTargetUrl())
            .path("pets")
            .path(String.valueOf(pet.get().getId()))
            .request()
            .get();

        // assert
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        var sofi = response.readEntity(Pet.class);

        assertThat(sofi.getName()).isEqualTo("Charlie");
        assertThat(sofi.getAge()).isEqualTo(18);
        assertThat(sofi.getSpecies()).isEqualTo(Species.PARROT);
    }

    @Test
    public void getPet_FailWhenNotFound() {
        // act
        var response = app.client()
            .target(getTargetUrl())
            .path("pets")
            .path("100")
            .request()
            .get();

        // assert
        assertThat(response.getStatusInfo()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void createPet_Success() {
        // act
        var createPetRequest = CreatePetRequest.builder()
            .name("Sofi")
            .age(4)
            .species(Species.CAT)
            .build();
        var sofi = app.client()
            .target(getTargetUrl())
            .path("pets")
            .request()
            .put(Entity.json(createPetRequest), Pet.class);

        // assert
        assertThat(sofi).isNotNull();
        assertThat(sofi.getId()).isNotNull();
        assertThat(sofi.getName()).isEqualTo("Sofi");
        assertThat(sofi.getAge()).isEqualTo(4);
        assertThat(sofi.getSpecies()).isEqualTo(Species.CAT);

        // verify DB changes
        var sofiInDb = postgre.getPet("Sofi");
        assertThat(sofiInDb).isPresent();
        assertThat(sofiInDb.get().getName()).isEqualTo("Sofi");
        assertThat(sofiInDb.get().getAge()).isEqualTo(4);
        assertThat(sofiInDb.get().getSpecies()).isEqualTo(Species.CAT);
    }

    @Test
    public void createPet_FailWhenSameName() {
        // arrange
        postgre.addPet("Sofi", 2, Species.CAT);

        // act
        var createPetRequest = CreatePetRequest.builder()
            .name("Sofi")
            .age(4)
            .species(Species.CAT)
            .build();
        var response = app.client()
            .target(getTargetUrl())
            .path("pets")
            .request()
            .put(Entity.json(createPetRequest));

        // assert
        assertThat(response.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void createPet_FailWhenMissingName() {
        // act
        var createPetRequest = CreatePetRequest.builder()
            .age(4)
            .species(Species.CAT)
            .build();
        var response = app.client()
            .target(getTargetUrl())
            .path("pets")
            .request()
            .put(Entity.json(createPetRequest));

        // assert
        assertThat(response.getStatusInfo().getStatusCode())
            .isEqualTo(UNPROCESSIBLE_ENTITY);
    }

    @Test
    public void createPet_FailWhenInvalidAge() {
        // act
        var createPetRequest = CreatePetRequest.builder()
            .name("Sofi")
            .species(Species.CAT)
            .build();
        var response = app.client()
            .target(getTargetUrl())
            .path("pets")
            .request()
            .put(Entity.json(createPetRequest));

        // assert
        assertThat(response.getStatusInfo().getStatusCode())
            .isEqualTo(UNPROCESSIBLE_ENTITY);
    }

    @Test
    public void createPet_FailWhenMissingSpecies() {
        // act
        var createPetRequest = CreatePetRequest.builder()
            .name("Sofi")
            .age(4)
            .build();
        var response = app.client()
            .target(getTargetUrl())
            .path("pets")
            .request()
            .put(Entity.json(createPetRequest));

        // assert
        assertThat(response.getStatusInfo().getStatusCode())
            .isEqualTo(UNPROCESSIBLE_ENTITY);
    }

    @Test
    public void updatePet_Success() {
        // arrange
        var oldName = "Sofi";
        postgre.addPet(oldName, 2, Species.CAT);

        var petBefore = postgre.getPet(oldName);
        assertThat(petBefore).isPresent();

        // act
        var newName = "Sofi Junior";
        var updatePetRequest = UpdatePetRequest.builder()
            .name(newName)
            .age(6)
            .species(Species.CAT)
            .build();
        var sofi = app.client()
            .target(getTargetUrl())
            .path("pets")
            .path(String.valueOf(petBefore.get().getId()))
            .request()
            .post(Entity.json(updatePetRequest), Pet.class);

        // assert
        assertThat(sofi).isNotNull();
        assertThat(sofi.getId()).isNotNull();
        assertThat(sofi.getName()).isEqualTo(newName);
        assertThat(sofi.getAge()).isEqualTo(6);
        assertThat(sofi.getSpecies()).isEqualTo(Species.CAT);

        // verify DB changes
        assertThat(postgre.getPet(newName)).isPresent();
        assertThat(postgre.getPet(oldName)).isEmpty();
    }

    @Test
    public void updatePet_FailWhenNotFound() {
        // act
        var updatePetRequest = UpdatePetRequest.builder()
            .name("Sofi")
            .age(6)
            .species(Species.CAT)
            .build();
        var response = app.client()
            .target(getTargetUrl())
            .path("pets")
            .path(String.valueOf(1))
            .request()
            .post(Entity.json(updatePetRequest));

        // assert
        assertThat(response.getStatusInfo()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void updatePet_FailWhenMissingName() {
        // act
        var updatePetRequest = UpdatePetRequest.builder()
            .age(6)
            .species(Species.CAT)
            .build();
        var response = app.client()
            .target(getTargetUrl())
            .path("pets")
            .path(String.valueOf(1))
            .request()
            .post(Entity.json(updatePetRequest));

        // assert
        assertThat(response.getStatusInfo().getStatusCode())
            .isEqualTo(UNPROCESSIBLE_ENTITY);
    }

    @Test
    public void updatePet_FailWhenInvalidAge() {
        // act
        var updatePetRequest = UpdatePetRequest.builder()
            .name("Sofi")
            .species(Species.CAT)
            .build();
        var response = app.client()
            .target(getTargetUrl())
            .path("pets")
            .path(String.valueOf(1))
            .request()
            .post(Entity.json(updatePetRequest));

        // assert
        assertThat(response.getStatusInfo().getStatusCode())
            .isEqualTo(UNPROCESSIBLE_ENTITY);
    }

    @Test
    public void updatePet_FailWhenMissingSpecies() {
        // act
        var updatePetRequest = UpdatePetRequest.builder()
            .name("Sofi")
            .age(6)
            .build();
        var response = app.client()
            .target(getTargetUrl())
            .path("pets")
            .path(String.valueOf(1))
            .request()
            .post(Entity.json(updatePetRequest));

        // assert
        assertThat(response.getStatusInfo().getStatusCode())
            .isEqualTo(UNPROCESSIBLE_ENTITY);
    }

    private String getTargetUrl() {
        return "http://localhost:%d".formatted(app.getLocalPort());
    }
}
