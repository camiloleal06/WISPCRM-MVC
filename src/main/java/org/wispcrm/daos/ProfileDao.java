package org.wispcrm.daos;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.wispcrm.modelo.profiles.Profile;
import org.wispcrm.modelo.profiles.ProfileDTO;
import org.wispcrm.modelo.profiles.ProfileView;

import java.util.List;

@Repository
public interface ProfileDao extends JpaRepository<Profile, Integer> {
    @Query("SELECT new org.wispcrm.modelo.profiles.ProfileDTO(p.id, p.name) FROM Profile p")
    List<ProfileDTO> listaProfile();
    @Cacheable("profiles")
    List<ProfileView> findAllBy();
}