package demo.model;

import com.github.lemniscate.lib.tiered.annotation.ApiResource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.Identifiable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@ApiResource
@Getter @Setter
public class User implements Identifiable<Long> {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "owner", cascade=CascadeType.ALL)
    private List<Pet> pets = new ArrayList<Pet>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private UserDetails details;

}
