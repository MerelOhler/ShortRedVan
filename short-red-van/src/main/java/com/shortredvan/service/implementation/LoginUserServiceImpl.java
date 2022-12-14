package com.shortredvan.service.implementation;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import com.shortredvan.entity.LoginUser;
import com.shortredvan.exception.DuplicateFoundException;
import com.shortredvan.exception.ResourceNotFoundException;
import com.shortredvan.repository.LoginUserRepository;
import com.shortredvan.service.LoginUserService;
import com.shortredvan.service.PartyLoginUserService;

@Service
public class LoginUserServiceImpl implements LoginUserService {
  
  private LoginUserRepository loginUserRepository;
  private PartyLoginUserService pluService;
  
  @Autowired
  public LoginUserServiceImpl(LoginUserRepository loginUserRepository, PartyLoginUserService pluService) {
    this.loginUserRepository = loginUserRepository;
    this.pluService = pluService;
  }

  @Override
  public LoginUser getLoginUserById(int id) {
    return loginUserRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("LoginUser","ID", id));
  }

  @Override
  public List<LoginUser> getAllLoginUsers() {
    return loginUserRepository.findAll();
  }

  @Override
  public LoginUser getLoginUserByEmail(String email) {
    return loginUserRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("LoginUser","Email",email));
  }
  
  @Override
  public List<LoginUser> getLoginUsers4PartyId(int id) {
    return loginUserRepository.findByPartyId(id);
  }

  @Override
  public LoginUser createLoginUser(LoginUser loginUser) {
    try {
      return loginUserRepository.save(loginUser);
    } catch (DataIntegrityViolationException e) {
      new DuplicateFoundException("LoginUser", "Email", loginUser.getEmail());
      return null;
    }
  }

  @Override
  public LoginUser updateLoginUser(LoginUser loginUser, int id, LoginUser currentLogin) {
    try {
      LoginUser existingLU = loginUserRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("LoginUserID","ID", id));
      existingLU.setEmail(loginUser.getEmail());
      existingLU.setFirstName(loginUser.getFirstName());
      existingLU.setLastName(loginUser.getLastName());
      existingLU.setPassword(loginUser.getPassword());
      existingLU.setUserRole(loginUser.getUserRole());
      //cannot update date created
      existingLU.setDateModified(new Timestamp(System.currentTimeMillis()));
      existingLU.setModifiedBy(currentLogin.getLoginUserId());
      return loginUserRepository.save(existingLU);
    } catch (DataIntegrityViolationException e) {
      new DuplicateFoundException("LoginUser", "Email", loginUser.getEmail());
      return null;
    }
  }

  @Override
  public void deleteLoginUserById(LoginUser currentLogin) {
    pluService.deletePLUsByLUId(currentLogin);
    LoginUser existingLU = loginUserRepository.findById(currentLogin.getLoginUserId()).orElseThrow(() -> new ResourceNotFoundException("LoginUserID","ID", currentLogin.getLoginUserId()));
    existingLU.setModifiedBy(currentLogin.getLoginUserId());
    existingLU.setDateModified(new Timestamp(System.currentTimeMillis()));
    existingLU.setDateDeleted(new Timestamp(System.currentTimeMillis()));
    loginUserRepository.save(existingLU);
    
    
  }


}
