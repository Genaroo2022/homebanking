package com.homebanking.domain.util;


public final class DomainErrorMessages {

    private DomainErrorMessages() {
        throw new IllegalStateException("Utility class");
    }

    // --- ACCOUNT MESSAGES ---
    public static final String ACCOUNT_BALANCE_NEGATIVE = "El saldo inicial no puede ser negativo";
    public static final String ACCOUNT_MANDATORY_FIELDS = "Todos los campos de la cuenta son obligatorios (Usuario, CBU, Alias, Saldo)";
    public static final String CBU_INVALID_LENGTH = "El CBU debe tener exactamente 22 dígitos";
    public static final String CBU_ONLY_NUMBERS = "El CBU solo debe contener números";
    public static final String ALIAS_INVALID_FORMAT = "El alias debe tener entre 6 y 20 caracteres y solo puede contener letras, números y puntos";

    // --- USER MESSAGES ---
    public static final String USER_UNDERAGE = "El usuario debe ser mayor de 18 años";
    public static final String BIRTHDATE_REQUIRED = "La fecha de nacimiento es obligatoria";
    public static final String INVALID_EMAIL = "El formato del email no es válido";
    public static final String DNI_INVALID = "El DNI debe tener al menos 7 dígitos";
    public static final String MANDATORY_FIELDS = "Todos los campos obligatorios deben completarse (Nombre, Apellido, DNI, Email, Password, Dirección)";
    public static final String DNI_ALREADY_EXISTS = "El DNI ingresado ya existe en el sistema";
    public static final String PASSWORD_FORMAT = "La contraseña debe tener al menos 8 caracteres";
    public static final String INVALID_DNI_FORMAT = "El DNI debe contener solo números";
    public static final String INVALID_NAME_FORMAT = "El nombre y apellido solo pueden contener letras";

}
