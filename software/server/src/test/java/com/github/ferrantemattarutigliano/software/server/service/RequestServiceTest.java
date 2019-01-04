package com.github.ferrantemattarutigliano.software.server.service;

import com.github.ferrantemattarutigliano.software.server.constant.Message;
import com.github.ferrantemattarutigliano.software.server.constant.Role;
import com.github.ferrantemattarutigliano.software.server.model.dto.GroupRequestDTO;
import com.github.ferrantemattarutigliano.software.server.model.dto.IndividualRequestDTO;
import com.github.ferrantemattarutigliano.software.server.model.dto.ReceivedRequestDTO;
import com.github.ferrantemattarutigliano.software.server.model.dto.SentRequestDTO;
import com.github.ferrantemattarutigliano.software.server.model.entity.*;
import com.github.ferrantemattarutigliano.software.server.repository.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Type;
import java.security.Principal;
import java.sql.Date;
import java.sql.Time;
import java.util.*;


public class RequestServiceTest {
    @InjectMocks
    private RequestService requestService;

    @Mock
    private IndividualRepository mockIndividualRepository;

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private SecurityContext mockSecurityContext;

    @Mock
    private Authentication mockAuthentication;

    @Mock
    private Principal mockPrincipal;

    @Mock
    private ThirdPartyRepository mockThirdPartyRepository;

    @Mock
    private IndividualRequestRepository mockIndividualRequestRepository;

    @Mock
    private SimpMessagingTemplate mockSimpMessaggingTemplate;

    @Mock
    private IndividualSpecification mockIndividualSpecification;

    @Mock
    private GroupRequestRepository mockGroupRequestRepository;

    @Mock
    private HealthDataRepository mockHealthDataRepository;


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

    private IndividualRequest createMockIndRequest(String ssn) {
        IndividualRequest request = new IndividualRequest(ssn);
        request.setDate(new Date(1));
        request.setTime(new Time(1));

        return request;

    }

    private GroupRequest createMockGroupRequest(String criteria) {
        GroupRequest request = new GroupRequest(criteria);
        request.setDate(new Date(1));
        request.setTime(new Time(1));

        return request;
    }

    @Test
    public void individualRequestTest() {
        //create a mock user individual
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create mock user thridparty
        String role2 = Role.ROLE_THIRD_PARTY.toString();
        User mockedUser2 = new User("Username", "Password", "AA@AA.com", role);
        ThirdParty mockedThirdParty = new ThirdParty();
        mockedThirdParty.setUser(mockedUser2);
        mockedThirdParty.setVat("11111111111");
        mockedThirdParty.setOrganizationName("topolino");
        //create individual requests
        IndividualRequest firstIndRequest = createMockIndRequest(mockedIndividual.getSsn());
        //add request to a collection
        Collection<IndividualRequest> indRequests = new ArrayList<>();
        indRequests.add(firstIndRequest);
        //save it in thirdparty
        mockedThirdParty.setIndividualRequests(indRequests);


        /* TEST STARTS HERE */
        mockThirdPartyAuthorized(mockedUser2, mockedThirdParty);

        Mockito.when(mockThirdPartyRepository.existsByUser(mockedUser2))
                .thenReturn(true);
        Mockito.when(mockThirdPartyRepository.findByUser(mockedUser2))
                .thenReturn(mockedThirdParty);
        Mockito.when(mockIndividualRepository.existsBySsn(mockedIndividual.getSsn()))
                .thenReturn(true);
        Mockito.when(mockIndividualRepository.findBySsn(mockedIndividual.getSsn()))
                .thenReturn(mockedIndividual);


        String result = requestService.individualRequest(firstIndRequest);

        Assert.assertEquals(Message.REQUEST_SUCCESS.toString() + " Receiver: " + mockedIndividual.getUser().getUsername(), result);


    }

    @Test
    public void groupRequestTestNotAnonymous() {
        //create a mock user individual
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        mockedIndividual.setState("italy");
        //create mock user thridparty
        String role2 = Role.ROLE_THIRD_PARTY.toString();
        User mockedUser2 = new User("Username", "Password", "AA@AA.com", role2);
        ThirdParty mockedThirdParty = new ThirdParty();
        mockedThirdParty.setUser(mockedUser2);
        mockedThirdParty.setVat("11111111111");
        mockedThirdParty.setOrganizationName("topolino");
        //create group requests
        GroupRequest firstGroupRequest = createMockGroupRequest("state=italy");
        firstGroupRequest.setSubscription(false);
        //add request to a collection
        Collection<GroupRequest> groupRequests = new ArrayList<>();
        groupRequests.add(firstGroupRequest);
        //save it in thirdparty
        mockedThirdParty.setGroupRequests(groupRequests);


        /* TEST STARTS HERE */
        mockThirdPartyAuthorized(mockedUser2, mockedThirdParty);

        Mockito.when(mockThirdPartyRepository.existsByUser(mockedUser2))
                .thenReturn(true);
        Mockito.when(mockThirdPartyRepository.findByUser(mockedUser2))
                .thenReturn(mockedThirdParty);
        Mockito.when(mockIndividualRepository.existsBySsn(mockedIndividual.getSsn()))
                .thenReturn(true);
        Mockito.when(mockIndividualRepository.findBySsn(mockedIndividual.getSsn()))
                .thenReturn(mockedIndividual);

        Specification<Individual> specification = mockIndividualSpecification.findByCriteriaSpecification(firstGroupRequest.getCriteria().split(";"));
        //  Mockito.when(mockIndividualSpecification.findByCriteriaSpecification(firstGroupRequest.getCriteria().split(";")))
        //          .thenReturn(mockIndividualSpecification.inState("italy"));

        List<Individual> mockIndividualCollection = new ArrayList<>();
        mockIndividualCollection.add(mockedIndividual);
        Mockito.when(mockIndividualRepository.findAll(specification))
                .thenReturn(mockIndividualCollection);
        Mockito.when(mockGroupRequestRepository.save(firstGroupRequest)).thenReturn(firstGroupRequest);


        String result = requestService.groupRequest(firstGroupRequest);

        Assert.assertEquals(Message.REQUEST_NOT_ANONYMOUS.toString(), result);

    }


/* TODO FIX IT: individualrepository.findall(specifications) returns 0 size value

    @Test
    public void groupRequestTest(){
        //create a mock users individual
        String role = Role.ROLE_INDIVIDUAL.toString();
        int i=0;
        List<Individual> listIndividuals = new ArrayList<>();
        for (i=0;i<1020;i++){
            String x=Integer.toString(i);
            User mockedUser = new User("username"+x, "password"+x, "aa@a"+x+"a.com", role);
            Individual mockedIndividual = new Individual();
            mockedIndividual.setUser(mockedUser);
            mockedIndividual.setFirstname("pippo"+x);
            mockedIndividual.setLastname("pippetti"+x);
            int Ssn=100000000;
            Ssn=Ssn+i;
            String z=Integer.toString(Ssn);
            mockedIndividual.setSsn(z);
            mockedIndividual.setState("italy");
            listIndividuals.add(mockedIndividual);
        }
        //create mock user thridparty
        String role2 = Role.ROLE_THIRD_PARTY.toString();
        User mockedUser2 = new User("Username", "Password", "AA@AA.com", role2);
        ThirdParty mockedThirdParty = new ThirdParty();
        mockedThirdParty.setUser(mockedUser2);
        mockedThirdParty.setVat("11111111111");
        mockedThirdParty.setOrganizationName("topolino");
        //create group requests
        GroupRequest firstGroupRequest = createMockGroupRequest("state=italy;");
        firstGroupRequest.setSubscription(false);
        //add request to a collection
        Collection<GroupRequest> groupRequests = new ArrayList<>();
        groupRequests.add(firstGroupRequest);
        //save it in thirdparty
        mockedThirdParty.setGroupRequests(groupRequests);


        // TEST STARTS HERE

        mockThirdPartyAuthorized(mockedUser2, mockedThirdParty);

        Mockito.when(mockThirdPartyRepository.existsByUser(mockedUser2))
                .thenReturn(true);
        Mockito.when(mockThirdPartyRepository.findByUser(mockedUser2))
                .thenReturn(mockedThirdParty);


       // Specification<Individual> specification  = mockIndividualSpecification.findByCriteriaSpecification(firstGroupRequest.getCriteria().split(";"));
       // Mockito.when(mockIndividualSpecification.findByCriteriaSpecification(firstGroupRequest.getCriteria().split(";")))
       //           .thenReturn(mockIndividualSpecification.inState("italy"));

       Specification<Individual>  specification =mockIndividualSpecification.findByCriteriaSpecification(firstGroupRequest.getCriteria().split(";"));
        Mockito.when(mockIndividualRepository.findAll(specification))
                .thenReturn(listIndividuals);

        Mockito.when(mockGroupRequestRepository.save(firstGroupRequest)).thenReturn(firstGroupRequest);


        String result = requestService.groupRequest(firstGroupRequest);

        Assert.assertEquals(Message.REQUEST_SUCCESS.toString(), result);

    }

    */

    @Test
    public void showSentIndividualRequestTest() {
        //create a mock user individual
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create mock user thridparty
        String role2 = Role.ROLE_THIRD_PARTY.toString();
        User mockedUser2 = new User("Username", "Password", "AA@AA.com", role);
        ThirdParty mockedThirdParty = new ThirdParty();
        mockedThirdParty.setUser(mockedUser2);
        mockedThirdParty.setVat("11111111111");
        mockedThirdParty.setOrganizationName("topolino");
        //create individual requests
        IndividualRequest firstIndRequest = createMockIndRequest(mockedIndividual.getSsn());
        //add request to a collection
        Collection<IndividualRequest> indRequests = new ArrayList<>();
        indRequests.add(firstIndRequest);
        //save it in thirdparty
        mockedThirdParty.setIndividualRequests(indRequests);


        /* TEST STARTS HERE */
        mockThirdPartyAuthorized(mockedUser2, mockedThirdParty);


        Mockito.when(mockThirdPartyRepository.findByUser(mockedUser2))
                .thenReturn(mockedThirdParty);
        Mockito.when(mockIndividualRequestRepository.findByThirdParty(mockedThirdParty))
                .thenReturn(indRequests);


        Collection<IndividualRequest> result = requestService.showSentIndividualRequest();

        Assert.assertEquals(indRequests, result);


    }

    @Test
    public void groupRequestTest() {

        //create a mock user individual
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create mock user thridparty
        String role2 = Role.ROLE_THIRD_PARTY.toString();
        User mockedUser2 = new User("Username", "Password", "AA@AA.com", role2);
        ThirdParty mockedThirdParty = new ThirdParty();
        mockedThirdParty.setUser(mockedUser2);
        mockedThirdParty.setVat("11111111111");
        mockedThirdParty.setOrganizationName("topolino");
        //create group requests
        GroupRequest firstGroupRequest = createMockGroupRequest("state=italy;");
        firstGroupRequest.setSubscription(false);
        //add request to a collection
        Collection<GroupRequest> groupRequests = new ArrayList<>();
        groupRequests.add(firstGroupRequest);
        //save it in thirdparty
        mockedThirdParty.setGroupRequests(groupRequests);

        /* TEST STARTS HERE */
        mockThirdPartyAuthorized(mockedUser2, mockedThirdParty);


        Mockito.when(mockThirdPartyRepository.findByUser(mockedUser2))
                .thenReturn(mockedThirdParty);
        Mockito.when(mockGroupRequestRepository.findByThirdParty(mockedThirdParty)).thenReturn(groupRequests);


        Collection<GroupRequest> result = requestService.showSentGroupRequest();

        Assert.assertEquals(groupRequests, result);

    }

    @Test
    public void showSentRequestTest() {

        //create a mock user individual
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create mock user thridparty
        String role2 = Role.ROLE_THIRD_PARTY.toString();
        User mockedUser2 = new User("Username", "Password", "AA@AA.com", role2);
        ThirdParty mockedThirdParty = new ThirdParty();
        mockedThirdParty.setUser(mockedUser2);
        mockedThirdParty.setVat("11111111111");
        mockedThirdParty.setOrganizationName("topolino");
        //create group requests
        GroupRequest firstGroupRequest = createMockGroupRequest("state=italy;");
        firstGroupRequest.setSubscription(false);
        //add request to a collection
        Collection<GroupRequest> groupRequests = new ArrayList<>();
        groupRequests.add(firstGroupRequest);
        //save it in thirdparty
        mockedThirdParty.setGroupRequests(groupRequests);
        //create individual requests
        IndividualRequest firstIndRequest = createMockIndRequest(mockedIndividual.getSsn());
        //add request to a collection
        Collection<IndividualRequest> indRequests = new ArrayList<>();
        indRequests.add(firstIndRequest);
        //save it in thirdparty
        mockedThirdParty.setIndividualRequests(indRequests);
        //create sent request DTO
        ModelMapper modelMapper = new ModelMapper();
        SentRequestDTO sentRequestDTO = new SentRequestDTO();
        //group request DTO
        Type groupType = new TypeToken<Collection<GroupRequest>>() {
        }.getType();
        Collection<GroupRequestDTO> groupRequestDTOS = modelMapper.map(groupRequests, groupType);
        //individual request DTO
        Type individualType = new TypeToken<Collection<IndividualRequest>>() {
        }.getType();
        Collection<IndividualRequestDTO> individualRequestDTOS = modelMapper.map(indRequests, individualType);
        //set them
        sentRequestDTO.setIndividualRequestDTOS(individualRequestDTOS);
        sentRequestDTO.setGroupRequestDTOS(groupRequestDTOS);

        /* TEST STARTS HERE */
        mockThirdPartyAuthorized(mockedUser2, mockedThirdParty);


        Mockito.when(mockThirdPartyRepository.findByUser(mockedUser2))
                .thenReturn(mockedThirdParty);
        Mockito.when(mockGroupRequestRepository.findByThirdParty(mockedThirdParty))
                .thenReturn(groupRequests);
        Mockito.when(mockIndividualRequestRepository.findByThirdParty(mockedThirdParty))
                .thenReturn(indRequests);

        SentRequestDTO result = requestService.showSentRequest();

        Assert.assertEquals(sentRequestDTO.getGroupRequestDTOS(), result.getGroupRequestDTOS());
        Assert.assertEquals(sentRequestDTO.getIndividualRequestDTOS(), result.getIndividualRequestDTOS());

    }

    @Test
    public void showIncomingRequestTest() {
        //create a mock user individual
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create mock user thridparty
        String role2 = Role.ROLE_THIRD_PARTY.toString();
        User mockedUser2 = new User("Username", "Password", "AA@AA.com", role);
        ThirdParty mockedThirdParty = new ThirdParty();
        mockedThirdParty.setUser(mockedUser2);
        mockedThirdParty.setVat("11111111111");
        mockedThirdParty.setOrganizationName("topolino");
        //create individual requests
        IndividualRequest firstIndRequest = createMockIndRequest(mockedIndividual.getSsn());
        firstIndRequest.setAccepted(true);
        //add request to a collection
        Collection<IndividualRequest> indRequests = new ArrayList<>();
        indRequests.add(firstIndRequest);
        //save it in thirdparty
        mockedThirdParty.setIndividualRequests(indRequests);
        //create collection of request DTO
        Collection<ReceivedRequestDTO> receivedRequestDTOS = new HashSet<>();
        for (IndividualRequest r : indRequests) {
            if (r.isAccepted() != null) continue; //only not accepted/rejected requests
            ReceivedRequestDTO receivedRequestDTO = new ReceivedRequestDTO();
            String thirdPartyName = r.getThirdParty().getOrganizationName();

            receivedRequestDTO.setId(r.getId());
            receivedRequestDTO.setDate(r.getDate());
            receivedRequestDTO.setTime(r.getTime());
            receivedRequestDTO.setThirdParty(thirdPartyName);
            receivedRequestDTOS.add(receivedRequestDTO);
        }



        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);


        Mockito.when(mockIndividualRepository.findByUser(mockedUser2))
                .thenReturn(mockedIndividual);

        Mockito.when(mockIndividualRequestRepository.findBySsn(mockedIndividual.getSsn()))
                .thenReturn(indRequests);


        Collection<ReceivedRequestDTO> result = requestService.showIncomingRequest();

        Assert.assertEquals(receivedRequestDTOS, result);


    }

    @Test
    public void handleRequestTest() {
        //create a mock user individual
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create mock user thridparty
        String role2 = Role.ROLE_THIRD_PARTY.toString();
        User mockedUser2 = new User("Username", "Password", "AA@AA.com", role);
        ThirdParty mockedThirdParty = new ThirdParty();
        mockedThirdParty.setUser(mockedUser2);
        mockedThirdParty.setVat("11111111111");
        mockedThirdParty.setOrganizationName("topolino");
        //create individual requests
        IndividualRequest firstIndRequest = createMockIndRequest(mockedIndividual.getSsn());
        firstIndRequest.setId(0L);
        firstIndRequest.setThirdParty(mockedThirdParty);
        //add request to a collection
        Collection<IndividualRequest> indRequests = new ArrayList<>();
        indRequests.add(firstIndRequest);
        //save it in thirdparty
        mockedThirdParty.setIndividualRequests(indRequests);



        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);


        Mockito.when(mockIndividualRepository.findBySsn(mockedIndividual.getSsn()))
                .thenReturn(mockedIndividual);
        Mockito.when(mockIndividualRequestRepository.findById(0L))
                .thenReturn(Optional.of(firstIndRequest));
        Mockito.when(mockIndividualRequestRepository.findByThirdParty(mockedThirdParty))
                .thenReturn(indRequests);


        String result = requestService.handleRequest(0L, true);

        Assert.assertEquals(Message.REQUEST_ACCEPTED.toString(), result);
    }
}