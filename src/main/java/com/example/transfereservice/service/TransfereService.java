package com.example.transfereservice.service;

import com.example.transfereservice.dto.Client;
import com.example.transfereservice.dto.TransfereResponse;
import com.example.transfereservice.enums.StatusTransfere;
import com.example.transfereservice.dto.Agent;
import com.example.transfereservice.dto.TransfereRequest;
import com.example.transfereservice.model.Transfere;
import com.example.transfereservice.repository.TransfereRepository;
import com.example.transfereservice.salesforce.AuthenticationResponse;
import com.example.transfereservice.salesforce.SalesforceApiConnect;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class TransfereService
{
    private final TransfereRepository transfereRepository;
    private final RestTemplate restTemplate;
    private final SalesforceApiConnect salesforceApiConnect;


    public TransfereService(TransfereRepository transfereRepository, RestTemplate restTemplate, SalesforceApiConnect salesforceApiConnect)
    {
        this.transfereRepository = transfereRepository;
        this.restTemplate = restTemplate;
        this.salesforceApiConnect = salesforceApiConnect;
    }

    public void placeTransfere(TransfereRequest transfereRequest)
    {
        Agent agent = restTemplate.getForObject("http://agent-service/api/agent/ref/"+transfereRequest.getReferenceAgent(), Agent.class);
        //Agent agent1 = new Agent("nizar", "alloul", "ee865477", "0667848465", new Date(), 1500);

        //Verifications fonctionnelles
        if(transfereRequest.getMontant() > 2000)
        {
            throw new IllegalArgumentException("Montant par transfere dépassé !");
        }

        else if(transfereRequest.getMontant() > agent.getPlafond())
        {
            throw new IllegalArgumentException("Plafond agent dépassé !");
        }

       /*
        Client clientDonneur = restTemplate.getForObject("http://client-service/clients/ref/"+transfereRequest.getReferenceClientDonneur(), Client.class);
        Client clientBeneficiaire = restTemplate.getForObject("http://client-service/clients/ref/"+transfereRequest.getReferenceClientBeneficiaire(), Client.class);
        */

        Random random = new Random();
        int i = random.nextInt(9999999);
        String pin = String.valueOf(1000 + random.nextInt(9000));
        String reference = "EDP"+i;

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 10);
        Date dateExpiration = c.getTime();


        Transfere transfere = Transfere.builder()
                .montant(transfereRequest.getMontant())
                .reference(reference)
                .referenceAgent(transfereRequest.getReferenceAgent())
                .referenceClientBeneficiaire(transfereRequest.getReferenceClientBeneficiaire())
                .referenceClientDonneur(transfereRequest.getReferenceClientDonneur())
                .dateExpiration(dateExpiration)
                .status(StatusTransfere.A_SERVIR)
                .codePinTransfere(pin)
                .moyenDeTransfert(transfereRequest.getModePayement())
                .build();

        agent.setPlafond(agent.getPlafond() - transfereRequest.getMontant());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Double> request = new HttpEntity<>(agent.getPlafond(), headers);
        restTemplate.postForObject("http://agent-service/api/agent/"+agent.getCin(), request, Void.class);

        //todo : Envoi SMS au client donneur avec la reference de la transaction et le code pin using Twilio ou Vonage API

        VonageClient vonageClient = VonageClient.builder()
                .apiSecret("V06izjgFYQ2LTK2Q")
                .apiKey("45604df1")
                .build();

        String SMSBody = "Cher client, vote réference de transfert est : " + reference  + " d'un montant de "+ transfereRequest.getMontant() +
                " MAD que vous pouvez récuperer dans l'ensemble des agences SPEED CASH et les GAB SANS CARTE. \nMerci d'avoir utilisé SPEED CASH Service.\n";

        if(transfereRequest.getModePayement().equalsIgnoreCase("gab"))
        {
            SMSBody += "Votre PIN du transfert est : " + pin + ".";
        }

        TextMessage message = new TextMessage("SPEED CASH", "+212632938333", SMSBody);
        //SmsSubmissionResponse response = vonageClient.getSmsClient().submitMessage(message);

        /*if (response.getMessages().get(0).getStatus() == MessageStatus.OK)
        {
            System.out.println("Message sent successfully.");
        }
        else
        {
            System.out.println("Message failed with error: " + response.getMessages().get(0).getErrorText());
        }
         */

        transfereRepository.save(transfere);
        AuthenticationResponse authenticationResponse = salesforceApiConnect.login();
        salesforceApiConnect.addTransfere(authenticationResponse.getAccess_token(), authenticationResponse.getInstance_url(),transfere);
    }

    public void transfereMultiple(List<TransfereRequest> listTransfers)
    {
        for(TransfereRequest transfere : listTransfers)
        {
            this.placeTransfere(transfere);
        }
    }

    public void payerTransfere(String reference, String cin)
    {
        Transfere transfere = transfereRepository.findTransfereByReference(reference).get();

        if(transfere == null)
            throw new IllegalStateException("Tranfere introuvable!");

        if(transfere.getDateExpiration().after(new Date()) && cin.equalsIgnoreCase(transfere.getReferenceClientBeneficiaire()) && transfere.getStatus() == StatusTransfere.A_SERVIR)
        {
            transfere.setStatus(StatusTransfere.PAYE);
            transfereRepository.save(transfere);

            //Update salesforce
            AuthenticationResponse authenticationResponse = salesforceApiConnect.login();
            salesforceApiConnect.updateTransfere(authenticationResponse.getAccess_token(), authenticationResponse.getInstance_url(),transfere);
        }

        if(!transfere.getDateExpiration().after(new Date()))
            throw new IllegalStateException("Transfere expiré");

        if(!cin.equalsIgnoreCase(transfere.getReferenceClientBeneficiaire()))
            throw new IllegalStateException("Cin fourni invalide");

        if(transfere.getStatus() == StatusTransfere.PAYE)
            throw new IllegalStateException("Transfere déjà payé");

        if(transfere.getStatus() == StatusTransfere.BLOQUE)
            throw new IllegalStateException("Transfere bloqué");

        if(transfere.getStatus() == StatusTransfere.EXTOURNE)
            throw new IllegalStateException("Transfere extourné");
    }

    public TransfereResponse payerTransfereGab(String reference, String pinTransfere)
    {
        Transfere transfere = transfereRepository.findTransfereByReference(reference).get();

        if(pinTransfere != transfere.getCodePinTransfere())
            throw new IllegalStateException("Pin incorrect");

        if(transfere.getDateExpiration().after(new Date()) && transfere.getStatus() == StatusTransfere.A_SERVIR && pinTransfere == transfere.getCodePinTransfere())
        {
            transfere.setStatus(StatusTransfere.PAYE);
            transfereRepository.save(transfere);
            TransfereResponse transfereResponse = new TransfereResponse();
            Client clientBeneficiaire = restTemplate.getForObject("http://client-service/clients/ref/"+transfere.getReferenceClientBeneficiaire(), Client.class);

            transfereResponse.setAmount(transfere.getMontant());
            transfereResponse.setClientBeneficiare(clientBeneficiaire.getNom() + " " + clientBeneficiaire.getPrenom());

            //Update salesforce
            AuthenticationResponse authenticationResponse = salesforceApiConnect.login();
            salesforceApiConnect.updateTransfere(authenticationResponse.getAccess_token(), authenticationResponse.getInstance_url(),transfere);

            return transfereResponse;
        }

        if(!transfere.getDateExpiration().after(new Date()))
            throw new IllegalStateException("Transfere expiré");

        if(transfere.getStatus() == StatusTransfere.PAYE)
            throw new IllegalStateException("Transfere déjà payé");

        if(transfere.getStatus() == StatusTransfere.BLOQUE)
            throw new IllegalStateException("Transfere bloqué");

        if(transfere.getStatus() == StatusTransfere.EXTOURNE)
            throw new IllegalStateException("Transfere extourné");

        return null;
    }

    public void bloquerTansfere(String reference)
    {
        Transfere transfere = transfereRepository.findTransfereByReference(reference).get();
        if(transfere != null)
        {
            if(transfere.getStatus() != StatusTransfere.BLOQUE)
            {
                transfere.setStatus(StatusTransfere.BLOQUE);
                transfereRepository.save(transfere);

                //update salesforce
                AuthenticationResponse authenticationResponse = salesforceApiConnect.login();
                salesforceApiConnect.updateTransfere(authenticationResponse.getAccess_token(), authenticationResponse.getInstance_url(),transfere);
            }
            else
                throw new IllegalStateException("error blocking the transfer");
        }
        else
            throw new IllegalStateException("error, transfere introuvable");


    }

    public void debloquerTransfere(String reference)
    {
        Transfere transfere = transfereRepository.findTransfereByReference(reference).get();

        if(transfere != null)
        {
            if(transfere.getStatus() == StatusTransfere.BLOQUE)
            {
                transfere.setStatus(StatusTransfere.DEBLOQUE_A_SERVIR);
                transfereRepository.save(transfere);
                //update salesforce
                AuthenticationResponse authenticationResponse = salesforceApiConnect.login();
                salesforceApiConnect.updateTransfere(authenticationResponse.getAccess_token(), authenticationResponse.getInstance_url(),transfere);
            }
            else
                throw new IllegalStateException("error, transfere n'est pas bloqué");
        }
        else
            throw new IllegalStateException("error, transfere introuvable");
    }

    public void extournerTransfere(String reference, String motif)
    {
        Transfere transfere = transfereRepository.findTransfereByReference(reference).get();

        Agent agent = restTemplate.getForObject("http://agent-service/agents/ref/"+transfere.getReferenceAgent(), Agent.class);

        if(transfere == null)
            throw new IllegalStateException("Transfere introuvable !");
        if(transfere.getStatus() == StatusTransfere.EXTOURNE)
            throw new IllegalStateException("Transfere déjà extourné !");
        if(transfere.getStatus() == StatusTransfere.BLOQUE)
            throw new IllegalStateException("Transfere bloqué !");
        if(transfere.getStatus() == StatusTransfere.PAYE)
            throw new IllegalStateException("Transfere déjà payé !");

        transfere.setStatus(StatusTransfere.EXTOURNE);
        agent.setPlafond(agent.getPlafond() + transfere.getMontant());

        restTemplate.postForObject("http://agent-service/api/agent/"+agent.getCin(), agent.getPlafond(), Void.class);

        //update salesforce
        AuthenticationResponse authenticationResponse = salesforceApiConnect.login();
        salesforceApiConnect.updateTransfere(authenticationResponse.getAccess_token(), authenticationResponse.getInstance_url(),transfere);
    }

    public List<Transfere> getAllTransferesByClient(String cin)
    {
        return transfereRepository.findTransfereByReferenceClientDonneur(cin);
    }

    public List<Transfere> getAllTransferesByAgent(String agentCin)
    {
        return transfereRepository.findTransfereByReferenceAgent(agentCin);
    }

}
