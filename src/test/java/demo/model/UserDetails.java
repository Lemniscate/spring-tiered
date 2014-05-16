package demo.model;

import com.github.lemniscate.lib.rest.annotation.ApiNestedResource;
import com.github.lemniscate.lib.rest.repo.Model;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@ApiNestedResource(parentProperty = "user")
@Getter @Setter
public class UserDetails implements Model<Long> {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    @OneToOne
    @PrimaryKeyJoinColumn
    private User user;

}
