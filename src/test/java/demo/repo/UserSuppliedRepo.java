package demo.repo;

import com.github.lemniscate.lib.tiered.repo.ApiResourceRepository;
import demo.model.User;

import java.util.List;

public interface UserSuppliedRepo extends ApiResourceRepository<User, Long>{

    List<User> findByName(String name);
}
