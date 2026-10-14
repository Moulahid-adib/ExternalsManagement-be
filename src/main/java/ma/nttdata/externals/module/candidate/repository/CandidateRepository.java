package ma.nttdata.externals.module.candidate.repository;

import ma.nttdata.externals.module.candidate.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CandidateRepository extends JpaRepository<Candidate, UUID> {

    @Query("SELECT DISTINCT c.mainTech FROM Candidate c WHERE c.mainTech IS NOT NULL")
    List<String> findDistinctMainTechs();

    @Query("""
    SELECT DISTINCT c
    FROM Candidate c
    JOIN c.languages l
    WHERE LOWER(l.languageInEnglish) IN :languages
    AND c.yearsOfExperience >= :minYears
    AND (
       LOWER(c.mainTech) = LOWER(:mainTech)
    OR LOWER(c.mainTech) LIKE LOWER(CONCAT('%', :mainTech, '%'))
    OR LOWER(:mainTech) LIKE CONCAT('%', LOWER(c.mainTech), '%')
            )
    ORDER BY c.yearsOfExperience DESC
    """)
    List<Candidate> findCandidatesRoughMatch(
            @Param("mainTech") String mainTech,
            @Param("minYears") int minYears,
            @Param("languages") List<String> languages
    );

    @Query("""
    SELECT DISTINCT c
    FROM Candidate c
    WHERE c.yearsOfExperience >= :minYears
    AND (
       LOWER(c.mainTech) = LOWER(:mainTech)
    OR LOWER(c.mainTech) LIKE LOWER(CONCAT('%', :mainTech, '%'))
    OR LOWER(:mainTech) LIKE CONCAT('%', LOWER(c.mainTech), '%')
            )
    ORDER BY c.yearsOfExperience DESC
    """)
    List<Candidate> findCandidatesRoughMatchNoLang(
            @Param("mainTech") String mainTech,
            @Param("minYears") int minYears
    );


}