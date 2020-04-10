package com.zuhlke.deliveries;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

public class UserService {
    private final PhoneNumberUtil phoneNumberUtil;
    private final String defaultRegion;
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("objectdb:$objectdb/db/users.odb");
    byte[] encryptionKey;
    private final SecretKeyFactory fac;

    public UserService() {
        this(System.getProperty("key"), System.getProperty("region"));
    }

    public UserService(String encryptionKey, String defaultRegion) {
        if (encryptionKey == null) throw new RuntimeException("Please specify system property \"key\" for the password encryption key");
        this.encryptionKey = encryptionKey.getBytes();

        phoneNumberUtil = PhoneNumberUtil.getInstance();
        if (defaultRegion == null) throw new RuntimeException("Please specify system property \"region\" for default region");
        if (phoneNumberUtil.getCountryCodeForRegion(defaultRegion) == 0) throw new RuntimeException("Unsupported region " + defaultRegion + " please use 2 letter country code.");
        this.defaultRegion = defaultRegion;

        try {
            fac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public Long registerUser(String name, String phoneNumber, String password) {
        if (password == null) throw new RuntimeException("you must specify a password");
        if (password.length() < 8) throw new RuntimeException("The password is too short, minimum 8 characters");

        EntityManager em = emf.createEntityManager();
        try {
            String formattedPhone = formatPhone(phoneNumber);

            EntityTransaction tx = em.getTransaction();
            tx.begin();
            Long count = em.createQuery("select count(u) from User u where u.phoneNumber = :phone", Long.class).setParameter("phone", formattedPhone).getSingleResult();
            if (count > 0) throw new RuntimeException("Number " + phoneNumber + " already registered");

            User user = new User();
            user.name = name;
            user.phoneNumber = formattedPhone;
            user.encryptedPassword = encrypt(password);
            em.persist(user);
            tx.commit();
            return user.id;
        } catch (NumberParseException e) {
            throw new RuntimeException("Phone number " + phoneNumber + " is not valid");
        } finally {
            em.close();
        }
    }

    String formatPhone(String phoneNumber) throws NumberParseException {
        return phoneNumberUtil.format(phoneNumberUtil.parse(phoneNumber, defaultRegion), PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }

    private String encrypt(String password) {
        long start = System.currentTimeMillis();
        char[] chars = password.toCharArray();
        byte[] bytes = encryptionKey;

        PBEKeySpec spec = new PBEKeySpec(chars, bytes, 65536, 512);

        Arrays.fill(chars, Character.MIN_VALUE);

        try {
            byte[] securePassword = fac.generateSecret(spec).getEncoded();
            System.out.println("encryption took " + (System.currentTimeMillis() - start));
            return Base64.getEncoder().encodeToString(securePassword);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * If the password is matching returns the user. Returns null if password is wrong or user doesn't exist.
     * We return null for non-existant user to avoid to be able to check whether a phone number is registered.
     */
    public User authenticate(String phoneNumber, String password) throws NumberParseException {
        EntityManager em = emf.createEntityManager();
        User user = em.createQuery("select u from User u where u.phoneNumber = :phone", User.class).setParameter("phone", formatPhone(phoneNumber)).getSingleResult();
        if (user == null) return null;
        if (!encrypt(password).equals(user.encryptedPassword)) return null;
        return user;
    }

    protected void finalize() throws Throwable {
        emf.close();
    }

    public void wipeAllData() {
        EntityManager em = emf.createEntityManager();
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.createQuery("delete from User u").executeUpdate();
            tx.commit();
        } finally {
            em.close();
        }
    }
}
