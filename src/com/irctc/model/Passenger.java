package com.irctc.model;

import java.util.Objects;

/**
 * Encapsulated Passenger Model with validation rules and senior-citizen detection.
 */
public class Passenger {
    private final String name;
    private final int age;
    private final String gender; // M, F, O
    private final BerthType preferredBerth;
    private final boolean isSeniorCitizen;

    public Passenger(String name, int age, String gender, BerthType preferredBerth) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Passenger name cannot be empty");
        }
        if (age <= 0 || age > 120) {
            throw new IllegalArgumentException("Invalid passenger age: " + age);
        }
        this.name = name.trim();
        this.age = age;
        this.gender = (gender != null && !gender.trim().isEmpty()) ? gender.trim().toUpperCase() : "M";
        this.preferredBerth = (preferredBerth != null) ? preferredBerth : BerthType.LOWER;
        this.isSeniorCitizen = (this.age >= 60);
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public BerthType getPreferredBerth() {
        return preferredBerth;
    }

    public boolean isSeniorCitizen() {
        return isSeniorCitizen;
    }

    public boolean isFemale() {
        return "F".equalsIgnoreCase(gender);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Passenger that)) return false;
        return age == that.age && Objects.equals(name, that.name) && Objects.equals(gender, that.gender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, gender);
    }

    @Override
    public String toString() {
        return String.format("%s (%d/%s%s, Pref: %s)",
            name, age, gender, isSeniorCitizen ? ", SR. CITIZEN" : "", preferredBerth.getCode());
    }
}
