package com.homebanking.domain.util;

public final class DomainErrorMessages {

    private DomainErrorMessages() {
        throw new IllegalStateException("Utility class");
    }

    // --- ACCOUNT MESSAGES ---

    // ACCOUNT -> Balance
    public static final String ACCOUNT_BALANCE_NEGATIVE = "El saldo inicial no puede ser negativo";

    // ACCOUNT -> Format validations
    public static final String CBU_INVALID_LENGTH = "El CBU debe tener exactamente 22 dígitos";
    public static final String CBU_ONLY_NUMBERS = "El CBU solo debe contener números";
    public static final String ALIAS_INVALID_FORMAT = "El alias debe tener entre 6 y 20 caracteres y solo puede contener letras, números y puntos";

    // ACCOUNT -> Required fields
    public static final String USER_ID_REQUIRED = "El ID de usuario es obligatorio";
    public static final String CBU_REQUIRED = "El CBU es obligatorio";
    public static final String ALIAS_REQUIRED = "El alias es obligatorio";
    public static final String BALANCE_REQUIRED = "El saldo es obligatorio";
    public static final String ACCOUNT_MANDATORY_FIELDS = "Todos los campos de la cuenta son obligatorios (Usuario, CBU, Alias, Saldo)";

    // ACCOUNT -> Amount validations
    public static final String DEPOSIT_AMOUNT_MUST_BE_POSITIVE = "El monto a depositar debe ser mayor a cero";
    public static final String DEBIT_AMOUNT_MUST_BE_POSITIVE = "El monto a debitar debe ser mayor a cero";

    // --- USER MESSAGES ---

    // USER -> Age validation
    public static final String USER_UNDERAGE = "El usuario debe ser mayor de 18 años";
    public static final String USER_OVER_MAX_AGE = "La edad del usuario no es válida (máximo 130 años)";
    public static final String BIRTHDATE_REQUIRED = "La fecha de nacimiento es obligatoria";

    // USER -> Format validations
    public static final String INVALID_EMAIL = "El formato del email no es válido";
    public static final String INVALID_DNI_FORMAT = "El DNI debe contener solo números";
    public static final String INVALID_NAME_FORMAT = "El nombre y apellido solo pueden contener letras";
    public static final String DNI_INVALID = "El DNI debe tener al menos 7 dígitos";
    public static final String DNI_TOO_LONG = "El DNI no puede exceder 20 dígitos";
    public static final String PASSWORD_FORMAT = "La contraseña debe tener al menos 8 caracteres";

    // USER -> Required fields
    public static final String MANDATORY_FIELDS = "Todos los campos obligatorios deben completarse (Nombre, Apellido, DNI, Email, Password, Dirección)";
    public static final String PASSWORD_REQUIRED = "La contraseña es obligatoria";
    public static final String ADDRESS_REQUIRED = "El domicilio es obligatorio";

    // USER -> Uniqueness constraints
    public static final String DNI_ALREADY_EXISTS = "El DNI ingresado ya existe en el sistema";
    public static final String EMAIL_ALREADY_EXISTS = "El email ingresado ya existe en el sistema";

    // USER -> Reconstitution Validations ---
    public static final String ID_REQUIRED = "La identidad (ID) es obligatoria al reconstruir el usuario";
    public static final String CREATED_AT_REQUIRED = "La fecha de creación es obligatoria";

    // USER -> Authentication
    public static final String INVALID_CREDENTIALS = "Credenciales inválidas";
    public static final String USER_NOT_FOUND = "Usuario no encontrado";


    // --- TRANSFER MESSAGES ---

    public static final String TRANSFER_ALREADY_FINALIZED = "La transferencia ya ha sido finalizada y no puede modificarse";

    // TRANSFER -> Amount validations
    public static final String TRANSFER_AMOUNT_INVALID = "El monto de la transferencia debe ser mayor a cero";

    // TRANSFER -> Business rules
    public static final String TRANSFER_SAME_ACCOUNT = "No puedes transferir dinero a la misma cuenta de origen";
    public static final String INSUFFICIENT_FUNDS = "Fondos insuficientes para realizar la operación";

    // TRANSFER -> Required fields
    public static final String TRANSFER_DESC_REQUIRED = "Debes indicar una descripción o concepto";
    public static final String ORIGIN_ACCOUNT_ID_INVALID = "El ID de la cuenta de origen es inválido";

    // --- CARD MESSAGES ---

    // CARD -> Format validations
    public static final String CARD_NUMBER_INVALID = "El número de tarjeta es inválido";
    public static final String CARD_CVV_INVALID = "El código de seguridad (CVV) es inválido";
    public static final String CARD_INVALID_DATES = "La fecha de vencimiento debe ser posterior a la de emisión";

    // CARD -> Required fields
    public static final String CARD_HOLDER_REQUIRED = "El nombre del titular es obligatorio";
    public static final String CARD_ACCOUNT_REQUIRED = "La tarjeta debe estar asociada a una cuenta";
    public static final String CARD_DATES_REQUIRED = "Las fechas de la tarjeta son obligatorias";

    // CARD -> Expiration and state validation
    public static final String CARD_EXPIRED = "La tarjeta está vencida";
    public static final String CARD_EXPIRED_CANNOT_ACTIVATE = "No se puede activar una tarjeta vencida";

    // CARD -> State validation
    public static final String CARD_ALREADY_ACTIVE = "La tarjeta ya está activa";
    public static final String CARD_ALREADY_INACTIVE = "La tarjeta ya está inactiva";


}