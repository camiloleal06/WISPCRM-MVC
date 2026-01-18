package org.wispcrm.interfaces;

import org.wispcrm.modelo.profiles.Profile;

import java.util.List;

public interface ProfileInterface {

   List<Profile> findAll();
   void save(Profile profile);
   Profile findOne(Integer id);
}