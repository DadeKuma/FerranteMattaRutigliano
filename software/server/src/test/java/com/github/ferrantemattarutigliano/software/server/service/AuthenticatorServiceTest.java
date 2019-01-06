package com.github.ferrantemattarutigliano.software.server.service;

import com.github.ferrantemattarutigliano.software.server.constant.Message;
import com.github.ferrantemattarutigliano.software.server.constant.Role;
import com.github.ferrantemattarutigliano.software.server.model.entity.Individual;
import com.github.ferrantemattarutigliano.software.server.model.entity.ThirdParty;
import com.github.ferrantemattarutigliano.software.server.model.entity.User;
import com.github.ferrantemattarutigliano.software.server.repository.IndividualRepository;
import com.github.ferrantemattarutigliano.software.server.repository.ThirdPartyRepository;
import com.github.ferrantemattarutigliano.software.server.repository.UserRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;


public class AuthenticatorServiceTest {
    @InjectMocks
    private AuthenticatorService authenticatorService;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private IndividualRepository mockIndividualRepository;
    @Mock
    private ThirdPartyRepository mockThirdPartyRepository;
    @Mock
    private SecurityContext mockSecurityContext;
    @Mock
    private Authentication mockAuthentication;
    @Mock
    private Principal mockPrincipal;

    @Before
    public void initTest() {
        MockitoAnnotations.initMocks(this);
    }

    private void mockIndividualAuthorized(User expectedUser, Individual expectedIndividual) {
        SecurityContextHolder.setContext(mockSecurityContext);

        Mockito.when(mockSecurityContext.getAuthentication())
                .thenReturn(mockAuthentication);
        Mockito.when(mockAuthentication.getPrincipal())
                .thenReturn(mockPrincipal);
        Mockito.when(mockSecurityContext.getAuthentication().getPrincipal())
                .thenReturn(expectedUser);
        Mockito.when(mockUserRepository.existsByUsername(expectedUser.getUsername()))
                .thenReturn(true);
        //mock the existing individual associated with the user
        Mockito.when(mockIndividualRepository.existsByUser(expectedUser))
                .thenReturn(true);
        Mockito.when(mockIndividualRepository.findByUser(expectedUser))
                .thenReturn(expectedIndividual);
    }

    private void mockThirdPartyAuthorized(User expectedUser, ThirdParty expectedThirdParty) {
        SecurityContextHolder.setContext(mockSecurityContext);

        Mockito.when(mockSecurityContext.getAuthentication())
                .thenReturn(mockAuthentication);
        Mockito.when(mockAuthentication.getPrincipal())
                .thenReturn(mockPrincipal);
        Mockito.when(mockSecurityContext.getAuthentication().getPrincipal())
                .thenReturn(expectedUser);
        Mockito.when(mockUserRepository.existsByUsername(expectedUser.getUsername()))
                .thenReturn(true);
        //mock the existing third party associated with the user
        Mockito.when(mockThirdPartyRepository.existsByUser(expectedUser))
                .thenReturn(true);
        Mockito.when(mockThirdPartyRepository.findByUser(expectedUser))
                .thenReturn(expectedThirdParty);
    }

    @Test
    public void individualRegistrationTest() {
        User dummyUser = new User();
        dummyUser.setUsername("username");
        dummyUser.setPassword("password");
        dummyUser.setEmail("email@email.com");

        Individual dummyIndividual = new Individual();
        dummyIndividual.setUser(dummyUser);
        dummyIndividual.setSsn("123456789");
        dummyIndividual.setFirstname("Pippo");
        dummyIndividual.setLastname("Pappo");

        //mock "this user doesn't exists"
        Mockito.when(mockUserRepository.existsByUsername(dummyUser.getUsername())).thenReturn(false);
        Mockito.when(mockUserRepository.existsByEmail(dummyUser.getEmail())).thenReturn(false);
        Mockito.when(mockIndividualRepository.existsBySsn(dummyIndividual.getSsn())).thenReturn(false);

        String result = authenticatorService.individualRegistration(dummyIndividual);
        assertEquals(Message.REGISTRATION_SUCCESS.toString(), result);
    }


    @Test
    public void thirdPartyRegistrationTest() {
        User dummyUser = new User();
        dummyUser.setUsername("username");
        dummyUser.setPassword("password");
        dummyUser.setEmail("email@email.com");

        ThirdParty dummyThirdParty = new ThirdParty();
        dummyThirdParty.setUser(dummyUser);
        dummyThirdParty.setVat("12345678901");
        dummyThirdParty.setOrganizationName("Amazon");

        //mock "this user doesn't exists"
        Mockito.when(mockUserRepository.existsByUsername(dummyUser.getUsername())).thenReturn(false);
        Mockito.when(mockUserRepository.existsByEmail(dummyUser.getEmail())).thenReturn(false);
        Mockito.when(mockThirdPartyRepository.existsByVat(dummyThirdParty.getVat())).thenReturn(false);

        String result = authenticatorService.thirdPartyRegistration(dummyThirdParty);
        assertEquals(Message.REGISTRATION_SUCCESS.toString(), result);
    }

    @Test
    public void individualRegistrationFailTest() {
        User dummyUser = new User();
        dummyUser.setUsername("username");
        dummyUser.setPassword("password");
        dummyUser.setEmail("email@email.com");

        Individual dummyIndividual = new Individual();
        dummyIndividual.setUser(dummyUser);
        dummyIndividual.setSsn("123456789");
        dummyIndividual.setFirstname("Pippo");
        dummyIndividual.setLastname("Pappo");

        //mock "this user ssn already exists"
        Mockito.when(mockUserRepository.existsByUsername(dummyUser.getUsername())).thenReturn(false);
        Mockito.when(mockUserRepository.existsByEmail(dummyUser.getEmail())).thenReturn(false);
        Mockito.when(mockIndividualRepository.existsBySsn(dummyIndividual.getSsn())).thenReturn(true);

        String result = authenticatorService.individualRegistration(dummyIndividual);
        assertEquals(Message.INDIVIDUAL_ALREADY_EXISTS.toString(), result);
    }

    @Test
    public void thirdPartyRegistrationFailTest(){
        User dummyUser = new User();
        dummyUser.setUsername("username");
        dummyUser.setPassword("password");
        dummyUser.setEmail("email@email.com");

        ThirdParty dummyThirdParty = new ThirdParty();
        dummyThirdParty.setUser(dummyUser);
        dummyThirdParty.setVat("12345678901");
        dummyThirdParty.setOrganizationName("Amazon");

        //mock "this user vat already exists"
        Mockito.when(mockUserRepository.existsByUsername(dummyUser.getUsername())).thenReturn(false);
        Mockito.when(mockUserRepository.existsByEmail(dummyUser.getEmail())).thenReturn(false);
        Mockito.when(mockThirdPartyRepository.existsByVat(dummyThirdParty.getVat())).thenReturn(true);

        String result = authenticatorService.thirdPartyRegistration(dummyThirdParty);
        assertEquals(Message.THIRD_PARTY_ALREADY_EXISTS.toString(), result);
    }


    @Test
    public void individualLoginTest() {
        User dummyUser = new User();
        dummyUser.setUsername("username");
        dummyUser.setPassword("password");
        dummyUser.setEmail("email@email.com");

        Individual dummyIndividual = new Individual();
        dummyIndividual.setUser(dummyUser);
        dummyIndividual.setSsn("123456789");
        dummyIndividual.setFirstname("Pippo");
        dummyIndividual.setLastname("Pappo");

        Mockito.when(mockIndividualRepository.findByUser(dummyUser))
                .thenReturn(dummyIndividual);

        User result = authenticatorService.login(dummyUser);
        //todo how does login work?
        //assertEquals(dummyUser, result);
    }

    //todo add login failure for both individual and tp

    @Test
    public void changeIndividualProfileTest() {
        User dummyUser = new User();
        dummyUser.setUsername("username");
        dummyUser.setPassword("password");
        dummyUser.setEmail("email@email.com");
        //mock the existing individual
        Individual dummyIndividual = new Individual();
        dummyIndividual.setUser(dummyUser);
        dummyIndividual.setSsn("123456789");
        dummyIndividual.setFirstname("Pippo");
        dummyIndividual.setLastname("Pappo");
        //mock the updated individual
        Individual dummyIndividualUpdated = new Individual();
        dummyIndividualUpdated.setUser(dummyUser);
        dummyIndividualUpdated.setFirstname("Changed Name!");
        dummyIndividualUpdated.setLastname("Pappo");
        dummyIndividualUpdated.setWeight(200);
        dummyIndividualUpdated.setHeight(170);
        dummyIndividualUpdated.setCity("Milan");
        dummyIndividualUpdated.setState("Italy");
        dummyIndividualUpdated.setAddress("Via roma 123");
        //start test
        mockIndividualAuthorized(dummyUser, dummyIndividual);

        String username = dummyUser.getUsername();
        String result = authenticatorService.changeIndividualProfile(username, dummyIndividualUpdated);
        assertEquals(Message.CHANGE_PROFILE_SUCCESS.toString(), result);
    }

    @Test
    public void changeThirdPartyProfileTest() {
        User dummyUser = new User();
        dummyUser.setUsername("username");
        dummyUser.setPassword("password");
        dummyUser.setEmail("email@email.com");
        //mock the existing third party
        ThirdParty dummyThirdParty = new ThirdParty();
        dummyThirdParty.setUser(dummyUser);
        dummyThirdParty.setVat("12345678901");
        dummyThirdParty.setOrganizationName("Amazon");
        //mock the updated third party
        ThirdParty dummyThirdPartyUpdated = new ThirdParty();
        dummyThirdPartyUpdated.setUser(dummyUser);
        dummyThirdPartyUpdated.setVat("12345678901");
        dummyThirdPartyUpdated.setOrganizationName("Changed Name!");
        //start test
        mockThirdPartyAuthorized(dummyUser, dummyThirdParty);

        String username = dummyUser.getUsername();
        String result = authenticatorService.changeThirdPartyProfile(username, dummyThirdPartyUpdated);
        assertEquals(Message.CHANGE_PROFILE_SUCCESS.toString(), result);
    }

    @Test
    public void getThirdPartyProfileTest(){
        User dummyUser = new User();
        dummyUser.setUsername("username");
        dummyUser.setPassword("password");
        dummyUser.setEmail("email@email.com");
        //mock the existing third party
        ThirdParty dummyThirdParty = new ThirdParty();
        dummyThirdParty.setUser(dummyUser);
        dummyThirdParty.setVat("12345678901");
        dummyThirdParty.setOrganizationName("Amazon");

        mockThirdPartyAuthorized(dummyUser, dummyThirdParty);
        ThirdParty result = authenticatorService.getThirdPartyProfile("username");
        Assert.assertEquals(dummyThirdParty, result);
    }

    @Test
    public void getIndividualProfileTest(){
        User dummyUser = new User();
        dummyUser.setUsername("username");
        dummyUser.setPassword("password");
        dummyUser.setEmail("email@email.com");
        //mock the existing individual
        Individual dummyIndividual = new Individual();
        dummyIndividual.setUser(dummyUser);
        dummyIndividual.setSsn("123456789");
        dummyIndividual.setFirstname("Pippo");
        dummyIndividual.setLastname("Pappo");

        mockIndividualAuthorized(dummyUser, dummyIndividual);
        Individual result = authenticatorService.getIndividualProfile("username");
        Assert.assertEquals(dummyIndividual, result);
    }

    @Test
    public void changeUsernameTest(){
        User dummyUser = new User();
        dummyUser.setUsername("username");
        dummyUser.setPassword("password");
        dummyUser.setEmail("email@email.com");
        //mock the existing individual
        Individual dummyIndividual = new Individual();
        dummyIndividual.setUser(dummyUser);
        dummyIndividual.setSsn("123456789");
        dummyIndividual.setFirstname("Pippo");
        dummyIndividual.setLastname("Pappo");

        mockIndividualAuthorized(dummyUser, dummyIndividual);
        String result = authenticatorService.changeUsername("username", "NEW");
        String expected = Message.CHANGE_USERNAME_SUCCESS.toString() + "NEW";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void changePasswordTest(){
        User dummyUser = new User();
        dummyUser.setUsername("username");
        dummyUser.setPassword("password");
        dummyUser.setEmail("email@email.com");
        //mock the existing individual
        Individual dummyIndividual = new Individual();
        dummyIndividual.setUser(dummyUser);
        dummyIndividual.setSsn("123456789");
        dummyIndividual.setFirstname("Pippo");
        dummyIndividual.setLastname("Pappo");

        mockIndividualAuthorized(dummyUser, dummyIndividual);
        String result = authenticatorService.changePassword("username", "HELLO");
        Assert.assertEquals(Message.CHANGE_PASSWORD_SUCCESS.toString(), result);
    }

    @Test
    public void loadByUsernameTest(){
        User dummyUser = Mockito.mock(User.class);
        String username = "username";
        String password = "password";
        String email = "hue@alpha.com";
        dummyUser.setUsername(username);
        dummyUser.setPassword(password);
        dummyUser.setEmail(email);
        //user exists in database
        Mockito.when(mockUserRepository.existsByUsername(username))
                .thenReturn(true);
        Mockito.when(mockUserRepository.findByUsername(username))
                .thenReturn(dummyUser);
        //let's suppose that that user is a Third Party
        Mockito.when(mockThirdPartyRepository.existsByUser(dummyUser))
                .thenReturn(true);

        UserDetails result = authenticatorService.loadUserByUsername(username);
        Assert.assertSame(dummyUser, result);
    }

}