package com.les.jakebooks.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Utilitário para operações de criptografia.
 * 
 * Fornece métodos para:
 * - Criptografar senhas
 * - Validar senhas contra hashes
 * - Gerar senhas temporárias
 */
public class CriptografiaUtil {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    /**
     * Criptografa uma senha usando BCrypt.
     * 
     * @param senha senha em texto plano
     * @return senha criptografada
     */
    public static String criptografar(String senha) {
        if (senha == null || senha.isEmpty()) {
            throw new IllegalArgumentException("Senha não pode estar vazia");
        }
        return passwordEncoder.encode(senha);
    }

    /**
     * Valida uma senha contra um hash BCrypt.
     * 
     * @param senhaTextoPlano senha em texto plano
     * @param senhaHash hash da senha armazenada no banco
     * @return true se a senha coincide com o hash, false caso contrário
     */
    public static boolean validar(String senhaTextoPlano, String senhaHash) {
        if (senhaTextoPlano == null || senhaHash == null) {
            return false;
        }
        return passwordEncoder.matches(senhaTextoPlano, senhaHash);
    }

    /**
     * Gera uma senha temporária aleatória.
     * Formato: 8 caracteres alfanuméricos + 2 caracteres especiais
     * 
     * @return senha temporária
     */
    public static String gerarSenhaTemporaria() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String especiais = "!@#$%&*";
        StringBuilder senha = new StringBuilder();

        // 8 caracteres aleatórios
        for (int i = 0; i < 8; i++) {
            int indice = (int) (Math.random() * caracteres.length());
            senha.append(caracteres.charAt(indice));
        }

        // 2 caracteres especiais
        for (int i = 0; i < 2; i++) {
            int indice = (int) (Math.random() * especiais.length());
            senha.append(especiais.charAt(indice));
        }

        // Embaralhar
        String senhaGerada = senha.toString();
        char[] chars = senhaGerada.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }
}
