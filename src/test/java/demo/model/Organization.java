package demo.model;

import com.github.lemniscate.lib.rest.annotation.ApiResource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.Identifiable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@ApiResource
@Getter @Setter
public class Organization implements Identifiable<Long> {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    private String name;
}
