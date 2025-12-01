package org.coms4156.tars.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.coms4156.tars.dto.ClientDto;
import org.coms4156.tars.dto.TarsUserDto;
import org.coms4156.tars.dto.UserPreferenceDto;
import org.coms4156.tars.model.Client;
import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.model.UserPreference;

/**
 * Utility class for converting domain entities to DTOs.
 */
public final class DtoMapper {
  private DtoMapper() { }

  public static ClientDto toClientDto(Client c) {
    if (c == null) { return null; }
    return new ClientDto(
        c.getClientId(),
        c.getName(),
        c.getEmail(),
        c.getRateLimitPerMinute(),
        c.getMaxConcurrentRequests()
    );
  }

  public static TarsUserDto toTarsUserDto(TarsUser u) {
    if (u == null) { return null; }
    return new TarsUserDto(
        u.getUserId(),
        u.getClientId(),
        u.getUsername(),
        u.getEmail(),
        u.getRole(),
        u.getActive(),
        u.getSignUpDate(),
        u.getLastLogin()
    );
  }

  public static UserPreferenceDto toUserPreferenceDto(UserPreference p) {
    if (p == null) { return null; }
    return new UserPreferenceDto(
        p.getId(),
        p.getWeatherPreferences(),
        p.getTemperaturePreferences(),
        p.getCityPreferences()
    );
  }

  public static List<ClientDto> toClientDtos(List<Client> clients) {
    return clients == null ? List.of() : clients.stream().map(DtoMapper::toClientDto).collect(Collectors.toList());
  }

  public static List<TarsUserDto> toTarsUserDtos(List<TarsUser> users) {
    return users == null ? List.of() : users.stream().map(DtoMapper::toTarsUserDto).collect(Collectors.toList());
  }

  public static List<UserPreferenceDto> toUserPreferenceDtos(List<UserPreference> prefs) {
    return prefs == null ? List.of() : prefs.stream().map(DtoMapper::toUserPreferenceDto).collect(Collectors.toList());
  }
}
