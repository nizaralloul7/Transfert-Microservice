package com.example.transfereservice.salesforce;

import com.example.transfereservice.enums.StatusTransfere;
import com.example.transfereservice.model.Transfere;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import org.json.simple.JSONObject;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class SalesforceApiConnect
{
    public AuthenticationResponse login()
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        //add org infos
        params.add("username", "abderrahim@atlasproject.com");
        params.add("password", "atlas12345zouIten0LMJrka7emGtcuSe59");
        params.add("client_secret", "8828A98FD4FEAB128B5CE1807159D2C49E9B113061D43BBB0CB49DD000046990");
        params.add("client_id", "3MVG9DREgiBqN9WlOMOR8_txnmN3lOj47_tEdf_wJc8hNEu.lOXHgIv1njB_n3sJRGZ3kg_Bz5B753wyc8id5");
        params.add("grant_type", "password");

        String requestBody = "grant_type=password&client_id=3MVG9DREgiBqN9WlOMOR8_txnmN3lOj47_tEdf_wJc8hNEu.lOXHgIv1njB_n3sJRGZ3kg_Bz5B753wyc8id5" +
                "&client_secret=8828A98FD4FEAB128B5CE1807159D2C49E9B113061D43BBB0CB49DD000046990" +
                "&username=abderrahim@atlasproject.com" +
                "&password=atlas12345zouIten0LMJrka7emGtcuSe59";

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity response = restTemplate.postForEntity("https://login.salesforce.com/services/oauth2/token", request, AuthenticationResponse.class);

        return (AuthenticationResponse) response.getBody();
    }


    @SneakyThrows
    public String addTransfere(String accessToken, String instanceUrl, Transfere trs)
    {
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        ObjectMapper objectMapper = new ObjectMapper();

        String transfereJSON = objectMapper.writeValueAsString(trs);
        HttpEntity<String> request = new HttpEntity<>(transfereJSON, headers);
        ResponseEntity<String> salesforceTestData = restTemplate.exchange(instanceUrl + "/services/apexrest/transferService", HttpMethod.POST, request, String.class);
        System.out.println("TOKEN DETAILS :: " + salesforceTestData.getBody());

        return salesforceTestData.getBody();
    }

    @SneakyThrows
    public String updateTransfere(String accessToken, String instanceUrl, Transfere trs)
    {
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        ObjectMapper objectMapper = new ObjectMapper();

        String transfereJSON = objectMapper.writeValueAsString(trs);
        HttpEntity<String> request = new HttpEntity<>(transfereJSON, headers);
        ResponseEntity<String> salesforceTestData = restTemplate.exchange(instanceUrl + "/services/apexrest/transferService", HttpMethod.PUT, request, String.class);
        System.out.println("TOKEN DETAILS :: " + salesforceTestData.getBody());

        return salesforceTestData.getBody();
    }

}