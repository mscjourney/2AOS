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
  private DtoMapper() {
  }

  /**
   * Converts a {@link Client} domain entity to a {@link ClientDto}.
   *
   * @param client client entity; may be null
   * @return a ClientDto or null when input is null
   */
  public static ClientDto toClientDto(Client client) {
    if (client == null) {
      return null;
    }
    return new ClientDto(
        client.getClientId(),
        client.getName(),
        client.getEmail(),
        client.getRateLimitPerMinute(),
        client.getMaxConcurrentRequests()
    );
  }

  /**
   * Converts a {@link TarsUser} domain entity to a {@link TarsUserDto}.
   *
   * @param user user entity; may be null
   * @return a TarsUserDto or null when input is null
   */
  public static TarsUserDto toTarsUserDto(TarsUser user) {
    if (user == null) {
      return null;
    }
    return new TarsUserDto(
        user.getUserId(),
        user.getClientId(),
        user.getUsername(),
        user.getEmail(),
        user.getRole(),
        user.getActive(),
        user.getSignUpDate(),
        user.getLastLogin()
    );
  }

  /**
   * Converts a {@link UserPreference} domain entity to a {@link UserPreferenceDto}.
   *
   * @param preference preference entity; may be null
   * @return a UserPreferenceDto or null when input is null
   */
  public static UserPreferenceDto toUserPreferenceDto(UserPreference preference) {
    if (preference == null) {
      return null;
    }
    return new UserPreferenceDto(
        preference.getId(),
        preference.getWeatherPreferences(),
        preference.getTemperaturePreferences(),
        preference.getCityPreferences()
    );
  }

  /**
   * Maps a list of {@link Client} entities to {@link ClientDto}s.
   *
   * @param clients input list; may be null
   * @return empty list when input null; otherwise mapped list
   */
  public static List<ClientDto> toClientDtos(List<Client> clients) {
    return clients == null
        ? List.of()
        : clients.stream()
            .map(DtoMapper::toClientDto)
            .collect(Collectors.toList());
  }

  /**
   * Maps a list of {@link TarsUser} entities to {@link TarsUserDto}s.
   *
   * @param users input list; may be null
   * @return empty list when input null; otherwise mapped list
   */
  public static List<TarsUserDto> toTarsUserDtos(List<TarsUser> users) {
    return users == null
        ? List.of()
        : users.stream()
            .map(DtoMapper::toTarsUserDto)
            .collect(Collectors.toList());
  }

  /**
   * Maps a list of {@link UserPreference} entities to {@link UserPreferenceDto}s.
   *
   * @param prefs input list; may be null
   * @return empty list when input null; otherwise mapped list
   */
  public static List<UserPreferenceDto> toUserPreferenceDtos(List<UserPreference> prefs) {
    return prefs == null
        ? List.of()
        : prefs.stream()
            .map(DtoMapper::toUserPreferenceDto)
            .collect(Collectors.toList());
  }
}
