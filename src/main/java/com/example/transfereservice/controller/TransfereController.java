package com.example.transfereservice.controller;

import com.example.transfereservice.dto.TransfereRequest;
import com.example.transfereservice.dto.TransfereResponse;
import com.example.transfereservice.model.Transfere;
import com.example.transfereservice.repository.TransfereRepository;
import com.example.transfereservice.service.TransfereService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path ="/api/transfere")
public class TransfereController
{
    private final TransfereService transfereService;
    private final TransfereRepository transfereRepository;

    @Autowired
    public TransfereController(TransfereService transfereService, TransfereRepository transfereRepository)
    {
        this.transfereService = transfereService;
        this.transfereRepository = transfereRepository;
    }

    @GetMapping
    public List<Transfere> getAllTransferes()
    {
        return transfereRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void placeTransfere(@RequestBody TransfereRequest transfereRequest)
    {
        transfereService.placeTransfere(transfereRequest);
    }

    @GetMapping(path = "/{transferRef}")
    public Transfere getTransfereByReference(@PathVariable String transferRef)
    {
        return transfereRepository.findTransfereByReference(transferRef).get();
    }

    @PostMapping(path ="/transfereMultiple")
    @ResponseStatus(HttpStatus.CREATED)
    public void transfereMultiple(@RequestBody List<TransfereRequest> listTransferes)
    {
        transfereService.transfereMultiple(listTransferes);
    }

    @PostMapping(path ="/payerTransfere")
    public void payerTransfere(@RequestParam("reference") String reference, @RequestParam("cin") String cin)
    {
        transfereService.payerTransfere(reference, cin);
    }

    @PostMapping(path ="/bloquer/{reference}")
    public void bloquerTansfere(@PathVariable String reference)
    {
        transfereService.bloquerTansfere(reference);
    }

    @PostMapping(path ="/debloquer/{reference}")
    public void debloquerTransfere(@PathVariable String reference)
    {
        transfereService.debloquerTransfere(reference);
    }

    @PostMapping(path ="/extourner")
    public void extournerTransfere(@RequestParam String reference, @RequestParam String motif)
    {
        transfereService.extournerTransfere(reference, motif);
    }

    @GetMapping(path ="/AllTransfer/{cinDonneur}")
    public List<Transfere> getAllTransferesByClient(@PathVariable String cinDonneur)
    {
        return transfereService.getAllTransferesByClient(cinDonneur);
    }

    @PostMapping(path="/payerGab")
    public TransfereResponse payerTransfereGab(@RequestParam("reference") String reference, @RequestParam("pin") String transferePin)
    {
        return transfereService.payerTransfereGab(reference, transferePin);
    }

}
