package sample.domain;

import io.github.crudzilla.CRUDZillaConfig;
import io.github.crudzilla.CRUDZillaEntidadeSecurity;
import io.github.crudzilla.CRUDZillaOperations;
import io.github.crudzilla.persistency.CRUDZillaEntity;
import io.github.crudzilla.persistency.RepositoryJpa;
import org.springframework.stereotype.Repository;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Repository
public class SampleEntityRepository extends RepositoryJpa<SampleEntity, Integer> {
}
