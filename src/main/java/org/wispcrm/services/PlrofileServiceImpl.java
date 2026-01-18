package org.wispcrm.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wispcrm.daos.ProfileDao;
import org.wispcrm.excepciones.NotFoundException;
import org.wispcrm.interfaces.ProfileInterface;
import org.wispcrm.modelo.profiles.Profile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlrofileServiceImpl implements ProfileInterface {

    private final ProfileDao profileDao;

    @Override
    public List<Profile> findAll() {
        return listProfiles();
    }

    @Override
    public void save(Profile profile) {
        profileDao.save(profile);

    }
    @Override
    public Profile findOne(Integer id) {
        return profileDao.findById(id).orElseThrow(() -> new NotFoundException("No existe el Profile"));
    }

    public List<Profile> listProfiles() {
        return profileDao.findAllBy()
                .stream()
                .map(profileDTO -> Profile.builder()
                        .id(profileDTO.getId())
                        .name(profileDTO.getName()).build())
                .collect(Collectors.toList());
    }
}
