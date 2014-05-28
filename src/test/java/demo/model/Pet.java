package demo.model;

import com.github.lemniscate.lib.tiered.annotation.ApiNestedResource;
import com.github.lemniscate.lib.tiered.repo.Model;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@ApiNestedResource(parentProperty = "owner")
@Getter @Setter
public class Pet implements Model<Long> {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne
    @JoinTable(name="user_pet",
            inverseJoinColumns = { @JoinColumn(name="owner_id", referencedColumnName="id", nullable=true, updatable=true) },
            joinColumns = { @JoinColumn(name="pet_id", referencedColumnName="id", nullable=true, updatable=true)})
    private User owner;

    private String name;

}
