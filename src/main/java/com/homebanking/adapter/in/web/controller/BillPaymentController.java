package com.homebanking.adapter.in.web.controller;

import com.homebanking.adapter.in.web.annotation.Auditable;
import com.homebanking.adapter.in.web.mapper.BillPaymentWebMapper;
import com.homebanking.adapter.in.web.request.PayBillRequest;
import com.homebanking.adapter.in.web.response.BillPaymentResponse;
import com.homebanking.application.dto.payment.response.BillPaymentOutputResponse;
import com.homebanking.port.in.payment.GetBillPaymentInputPort;
import com.homebanking.port.in.payment.PayBillInputPort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Validated
public class BillPaymentController {

    private final PayBillInputPort payBillInputPort;
    private final GetBillPaymentInputPort getBillPaymentInputPort;
    private final BillPaymentWebMapper mapper;

    @PostMapping("/pay")
    @Auditable(action = "bill.pay")
    public ResponseEntity<BillPaymentResponse> pay(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PayBillRequest request) {
        BillPaymentOutputResponse output = payBillInputPort.pay(
                mapper.toInput(request, idempotencyKey, userDetails.getUsername())
        );
        return ResponseEntity
                .created(URI.create("/api/bills/" + output.id()))
                .body(mapper.toResponse(output));
    }

    @GetMapping("/{id}")
    @Auditable(action = "bill.get")
    public ResponseEntity<BillPaymentResponse> get(
            @PathVariable("id") UUID paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        BillPaymentOutputResponse output = getBillPaymentInputPort.getById(paymentId, userDetails.getUsername());
        return ResponseEntity.ok(mapper.toResponse(output));
    }
}

