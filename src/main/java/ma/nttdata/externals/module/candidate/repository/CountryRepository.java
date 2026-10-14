package ma.nttdata.externals.module.candidate.repository;

import ma.nttdata.externals.module.candidate.entity.Country;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CountryRepository extends ListCrudRepository<Country, UUID> {
    Optional<Country> findByEnglishName(String englishName);
    Optional<Country> findByName(String name);
}
