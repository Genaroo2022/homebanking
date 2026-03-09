package com.homebanking.adapter.in.web.controller;

import com.homebanking.adapter.in.web.annotation.Auditable;
import com.homebanking.adapter.in.web.mapper.CardWebMapper;
import com.homebanking.adapter.in.web.request.IssueCardRequest;
import com.homebanking.adapter.in.web.response.CardResponse;
import com.homebanking.application.dto.card.response.CardOutputResponse;
import com.homebanking.port.in.card.ActivateCardInputPort;
import com.homebanking.port.in.card.DeactivateCardInputPort;
import com.homebanking.port.in.card.GetCardsInputPort;
import com.homebanking.port.in.card.IssueCardInputPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final IssueCardInputPort issueCardInputPort;
    private final GetCardsInputPort getCardsInputPort;
    private final ActivateCardInputPort activateCardInputPort;
    private final DeactivateCardInputPort deactivateCardInputPort;
    private final CardWebMapper mapper;

    @PostMapping
    @Auditable(action = "card.issue")
    public ResponseEntity<CardResponse> issue(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody IssueCardRequest request) {
        CardOutputResponse output = issueCardInputPort.issue(mapper.toInput(request, userDetails.getUsername()));
        return ResponseEntity
                .created(URI.create("/cards/" + output.id()))
                .body(mapper.toResponse(output));
    }

    @GetMapping("/account/{accountId}")
    @Auditable(action = "card.list")
    public ResponseEntity<List<CardResponse>> list(
            @PathVariable("accountId") UUID accountId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CardResponse> response = getCardsInputPort.getByAccount(accountId, userDetails.getUsername())
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{cardId}/activate")
    @Auditable(action = "card.activate")
    public ResponseEntity<CardResponse> activate(
            @PathVariable("cardId") UUID cardId,
            @AuthenticationPrincipal UserDetails userDetails) {
        CardOutputResponse output = activateCardInputPort.activate(cardId, userDetails.getUsername());
        return ResponseEntity.ok(mapper.toResponse(output));
    }

    @PatchMapping("/{cardId}/deactivate")
    @Auditable(action = "card.deactivate")
    public ResponseEntity<CardResponse> deactivate(
            @PathVariable("cardId") UUID cardId,
            @AuthenticationPrincipal UserDetails userDetails) {
        CardOutputResponse output = deactivateCardInputPort.deactivate(cardId, userDetails.getUsername());
        return ResponseEntity.ok(mapper.toResponse(output));
    }
}

