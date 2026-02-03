package com.homebanking.domain.util;

/**
 * DomainErrorMessages: Mensajes de error centralizados del dominio

 * Todos los textos de error están aquí, facilitando:
 * - Cambios de textos sin tocar la lógica
 * - Internacionalización (i18n)
 * - Consistencia en toda la aplicación
 * - Testing sobre strings específicos

 * Organización: Por entidad/concepto
 */
public final class DomainErrorMessages {

    private DomainErrorMessages() {
        throw new IllegalStateException("Utility class");
    }

    // ============================================
    // ACCOUNT MESSAGES
    // ============================================

    // ACCOUNT -> Balance validations
    public static final String ACCOUNT_BALANCE_NEGATIVE =
            "El saldo inicial no puede ser negativo";

    // ACCOUNT -> Format validations
    public static final String CBU_ONLY_NUMBERS =
            "El CBU solo debe contener números";

    public static final String ALIAS_INVALID_FORMAT =
            "El alias debe tener entre 6 y 20 caracteres y solo puede contener letras, números y puntos";

    // ACCOUNT -> Required fields
    public static final String USER_ID_REQUIRED =
            "El ID de usuario es obligatorio";

    public static final String CBU_REQUIRED =
            "El CBU es obligatorio";

    public static final String ALIAS_REQUIRED =
            "El alias es obligatorio";

    public static final String BALANCE_REQUIRED =
            "El saldo es obligatorio";

    public static final String ACCOUNT_MANDATORY_FIELDS =
            "Todos los campos de la cuenta son obligatorios (Usuario, CBU, Alias, Saldo)";

    public static final String USER_ID_INVALID =
            "El ID de usuario debe ser mayor a cero";

    // ACCOUNT -> Amount validations
    public static final String DEPOSIT_AMOUNT_MUST_BE_POSITIVE =
            "El monto a depositar debe ser mayor a cero";

    public static final String DEBIT_AMOUNT_MUST_BE_POSITIVE =
            "El monto a debitar debe ser mayor a cero";

    // ============================================
    // USER MESSAGES
    // ============================================

    // USER -> Age validation
    public static final String USER_UNDERAGE =
            "El usuario debe ser mayor de 18 años";

    public static final String USER_OVER_MAX_AGE =
            "La edad del usuario no es válida (máximo 130 años)";

    public static final String BIRTHDATE_REQUIRED =
            "La fecha de nacimiento es obligatoria";

    // USER -> Format validations
    public static final String INVALID_EMAIL =
            "El formato del email no es válido";

    public static final String INVALID_DNI_FORMAT =
            "El DNI debe contener solo números";

    public static final String INVALID_NAME_FORMAT =
            "El nombre y apellido solo pueden contener letras";

    public static final String DNI_INVALID =
            "El DNI debe tener al menos 7 dígitos";

    public static final String DNI_TOO_LONG =
            "El DNI no puede exceder 20 dígitos";

    public static final String PASSWORD_FORMAT =
            "La contraseña debe tener al menos 8 caracteres";

    // USER -> Required fields
    public static final String MANDATORY_FIELDS =
            "Todos los campos obligatorios deben completarse (Nombre, Apellido, DNI, Email, Password, Dirección)";

    public static final String PASSWORD_REQUIRED =
            "La contraseña es obligatoria";

    public static final String ADDRESS_REQUIRED =
            "El domicilio es obligatorio";

    // USER -> Uniqueness constraints
    public static final String DNI_ALREADY_EXISTS =
            "El DNI ingresado ya existe en el sistema";

    public static final String EMAIL_ALREADY_EXISTS =
            "El email ingresado ya existe en el sistema";

    // USER -> Reconstitution validations
    public static final String ID_REQUIRED =
            "La identidad (ID) es obligatoria al reconstruir";

    public static final String CREATED_AT_REQUIRED =
            "La fecha de creación es obligatoria";

    // USER -> Authentication
    public static final String INVALID_CREDENTIALS =
            "Credenciales inválidas";

    public static final String USER_NOT_FOUND =
            "Usuario no encontrado";

    public static final String INVALID_REFRESH_TOKEN =
            "Refresh token inválido";

    public static final String TOTP_SECRET_REQUIRED =
            "El secreto TOTP es obligatorio";

    public static final String TOTP_SECRET_INVALID =
            "El secreto TOTP no tiene un formato válido";

    public static final String TOTP_CODE_REQUIRED =
            "El código TOTP es obligatorio";

    public static final String TOTP_CODE_INVALID =
            "El código TOTP es inválido";

    // USER -> Authorization
    public static final String ACCESS_DENIED =
            "No tiene permiso para acceder a este recurso";

    // ============================================
    // TRANSFER MESSAGES
    // ============================================

    // TRANSFER -> State transitions
    public static final String INVALID_PROCESSING_TRANSITION =
            "Solo transferencias PENDING o FAILED retryable pueden marcarse como PROCESSING. Estado actual: %s";

    public static final String ONLY_PROCESSING_CAN_COMPLETE =
            "Solo transferencias en PROCESSING pueden completarse. Estado actual: %s";

    public static final String ONLY_PROCESSING_CAN_FAIL =
            "Solo transferencias en PROCESSING pueden marcarse como FAILED. Estado actual: %s";

    public static final String CANNOT_REJECT_TRANSFER =
            "No se puede rechazar una transferencia en estado %s. Solo PROCESSING o FAILED (no reintentable)";

    public static final String ONLY_FAILED_CAN_RETRY =
            "Solo transferencias FAILED pueden prepararse para reintento. Estado actual: %s";

    public static final String MAX_RETRIES_EXCEEDED =
            "Máximo de reintentos (%d) excedido. RetryCount actual: %d";

    public static final String TRANSFER_STATUS_REQUIRED =
            "El estado de la transferencia es obligatorio";

    public static final String TRANSFER_INCONSISTENT_STATE =
            "Estado inconsistente para transferencia: %s";

    public static final String TRANSFER_COMPLETED_REQUIRES_EXECUTED_AT =
            "Transferencias COMPLETED requieren executedAt";

    public static final String TRANSFER_FAILURE_DATA_REQUIRED =
            "Transferencias %s requieren failedAt y failureReason";

    public static final String TRANSFER_NON_TERMINAL_HAS_FAILURE_DATA =
            "Transferencias %s no deben tener failedAt ni failureReason";

    public static final String TRANSFER_RETRYCOUNT_NEGATIVE =
            "El retryCount no puede ser negativo";

    public static final String TRANSFER_LAST_RETRY_REQUIRED =
            "lastRetryAt es obligatorio cuando retryCount > 0";

    // TRANSFER -> Amount validations
    public static final String TRANSFER_AMOUNT_OUT_OF_RANGE =
            "Monto debe estar entre $0.01 y $1,000,000.00";

    public static final String TRANSFER_AMOUNT_INVALID =
            "El monto de la transferencia debe ser mayor a cero";

    // TRANSFER -> Business rules
    public static final String TRANSFER_SAME_ACCOUNT =
            "No puedes transferir dinero a la misma cuenta de origen";

    public static final String INSUFFICIENT_FUNDS =
            "Fondos insuficientes para realizar la operación";

    // TRANSFER -> Required fields
    public static final String IDEMPOTENCY_KEY_REQUIRED =
            "La idempotency key es obligatoria";

    public static final String TRANSFER_DESC_REQUIRED =
            "Debes indicar una descripción o concepto";

    public static final String ORIGIN_ACCOUNT_ID_INVALID =
            "El ID de la cuenta de origen es inválido";

    public static final String ACCOUNT_NOT_FOUND =
            "La cuenta especificada no existe";

    public static final String TRANSFER_NOT_FOUND =
            "La transferencia especificada no existe";

    public static final String TRANSFER_FAILURE_REASON_REQUIRED =
            "La razón del fallo es obligatoria";

    public static final String TRANSFER_REJECTION_REASON_REQUIRED =
            "La razón del rechazo es obligatoria";

    // ============================================
    // CLEARING ACCOUNT MESSAGES
    // ============================================

    public static final String CLEARING_ACCOUNT_NOT_FOUND =
            "Clearing account no encontrado para el usuario";

    // ============================================
    // CARD MESSAGES
    // ============================================

    // CARD -> Format validations
    public static final String CARD_NUMBER_INVALID =
            "El número de tarjeta es inválido";

    public static final String CARD_CVV_INVALID =
            "El código de seguridad (CVV) es inválido";

    public static final String CARD_INVALID_DATES =
            "La fecha de vencimiento debe ser posterior a la de emisión";

    // CARD -> Required fields
    public static final String CARD_HOLDER_REQUIRED =
            "El nombre del titular es obligatorio";

    public static final String CARD_ACCOUNT_REQUIRED =
            "La tarjeta debe estar asociada a una cuenta";

    public static final String CARD_DATES_REQUIRED =
            "Las fechas de la tarjeta son obligatorias";

    // CARD -> Expiration and state validation
    public static final String CARD_EXPIRED =
            "La tarjeta está vencida";

    public static final String CARD_EXPIRED_CANNOT_ACTIVATE =
            "No se puede activar una tarjeta vencida";

    // CARD -> State validation
    public static final String CARD_ALREADY_ACTIVE =
            "La tarjeta ya está activa";

    public static final String CARD_ALREADY_INACTIVE =
            "La tarjeta ya está inactiva";
}


