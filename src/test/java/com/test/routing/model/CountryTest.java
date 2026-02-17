package com.test.routing.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class CountryTest {

  @Test
  void shouldCreateCountryWithValidData() {
    List<String> borders = List.of("AUT", "DEU", "POL", "SVK");
    Country country = new Country("CZE", borders);

    assertEquals("CZE", country.cca3());
    assertEquals(4, country.borders().size());
    assertTrue(country.borders().contains("AUT"));
  }

  @Test
  void shouldCreateImmutableBordersList() {
    List<String> borders = new ArrayList<>();
    borders.add("AUT");
    borders.add("DEU");

    Country country = new Country("CZE", borders);

    // Modifying original list should not affect the country's borders
    borders.add("POL");

    assertEquals(2, country.borders().size());
    assertFalse(country.borders().contains("POL"));
  }

  @Test
  void shouldNotAllowModificationOfBordersList() {
    List<String> borders = List.of("AUT", "DEU");
    Country country = new Country("CZE", borders);

    assertThrows(UnsupportedOperationException.class, () -> country.borders().add("POL"));
  }

  @Test
  void shouldHandleNullBorders() {
    Country country = new Country("JPN", null);

    assertEquals("JPN", country.cca3());
    assertNotNull(country.borders());
    assertTrue(country.borders().isEmpty());
  }

  @Test
  void shouldHandleEmptyBorders() {
    Country country = new Country("AUS", List.of());

    assertEquals("AUS", country.cca3());
    assertNotNull(country.borders());
    assertTrue(country.borders().isEmpty());
  }

  @Test
  void shouldCreateCountryWithNullCca3() {
    Country country = new Country(null, List.of("USA"));

    assertNull(country.cca3());
    assertEquals(1, country.borders().size());
  }

  @Test
  void shouldBeEqualWhenSameData() {
    Country country1 = new Country("CZE", List.of("AUT", "DEU"));
    Country country2 = new Country("CZE", List.of("AUT", "DEU"));

    assertEquals(country1, country2);
    assertEquals(country1.hashCode(), country2.hashCode());
  }

  @Test
  void shouldNotBeEqualWhenDifferentData() {
    Country country1 = new Country("CZE", List.of("AUT", "DEU"));
    Country country2 = new Country("ITA", List.of("AUT", "FRA"));

    assertNotEquals(country1, country2);
  }

  @Test
  void shouldReturnImmutableEmptyListForNullBorders() {
    Country country = new Country("ISL", null);

    assertThrows(UnsupportedOperationException.class, () -> country.borders().add("XXX"));
  }
}
